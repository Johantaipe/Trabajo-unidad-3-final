package com.vetexpert.usuarios.service.impl;

import com.vetexpert.usuarios.dto.*;
import com.vetexpert.usuarios.entity.OtpVerification;
import com.vetexpert.usuarios.entity.Usuario;
import com.vetexpert.usuarios.exception.DuplicateResourceException;
import com.vetexpert.usuarios.exception.InvalidPasswordException;
import com.vetexpert.usuarios.exception.OtpRateLimitException;
import com.vetexpert.usuarios.exception.ResourceNotFoundException;
import com.vetexpert.usuarios.repository.OtpVerificationRepository;
import com.vetexpert.usuarios.repository.UsuarioRepository;
import com.vetexpert.usuarios.security.JwtTokenProvider;
import com.vetexpert.usuarios.service.AuthService;
import com.vetexpert.usuarios.validator.UsuarioValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

/**
 * Implementación del servicio de autenticación.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private static final int MAX_OTP_POR_HORA = 5;
    private static final int OTP_EXPIRATION_MINUTES = 10;

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final OtpVerificationRepository otpRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioValidator validator;
    private final JavaMailSender mailSender;

    @Value("${app.email.dev-mode:true}")
    private boolean emailDevMode;

    @Value("${mail.sender:noreply@vetexperts.com}")
    private String emailSender;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           UsuarioRepository usuarioRepository,
                           OtpVerificationRepository otpRepository,
                           JwtTokenProvider jwtTokenProvider,
                           PasswordEncoder passwordEncoder,
                           UsuarioValidator validator,
                           JavaMailSender mailSender) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.otpRepository = otpRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.validator = validator;
        this.mailSender = mailSender;
    }

    @Override
    public LoginResponseDTO login(LoginRequestDTO request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        Usuario usuario = usuarioRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        String token = jwtTokenProvider.generateToken(
                usuario.getUsername(), usuario.getRol(), usuario.getId());

        return new LoginResponseDTO(
                token,
                usuario.getId(),
                usuario.getUsername(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getRol(),
                usuario.getCorreo(),
                usuario.getFotoUrl()
        );
    }

    @Override
    @Transactional
    public ApiResponseDTO registrarCliente(RegistroClienteRequestDTO request) {
        // Validar que no exista el correo
        if (usuarioRepository.existsByCorreo(request.getCorreo())) {
            throw new DuplicateResourceException("Ya existe un usuario con el correo: " + request.getCorreo());
        }

        // Generar username desde correo
        String username = request.getCorreo().split("@")[0];
        int counter = 1;
        String originalUsername = username;
        while (usuarioRepository.existsByUsername(username)) {
            username = originalUsername + counter++;
        }

        // Crear usuario con rol CLIENTE
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setRol("CLIENTE");
        usuario.setCorreo(request.getCorreo());
        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setTelefono(request.getTelefono());
        usuario.setDistrito(request.getDistrito());
        usuario.setEnabled(true);

        usuarioRepository.save(usuario);

        log.info("Cliente registrado exitosamente: {}", username);

        return ApiResponseDTO.ok("Registro exitoso. Su usuario es: " + username);
    }

    @Override
    @Transactional
    public ApiResponseDTO solicitarRecuperacionPassword(OlvidePasswordRequestDTO request) {
        Usuario usuario = usuarioRepository.findByCorreo(request.getCorreo())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró un usuario con el correo: " + request.getCorreo()));

        // Anti-spam: limitar OTPs por hora
        long otpsEnUltimaHora = otpRepository.countByCorreoAndFechaCreacionAfter(
                request.getCorreo(), LocalDateTime.now().minusHours(1));
        if (otpsEnUltimaHora >= MAX_OTP_POR_HORA) {
            throw new OtpRateLimitException(
                    "Se alcanzó el límite de solicitudes OTP. Intente nuevamente en 1 hora.");
        }

        // Invalidar OTPs anteriores
        otpRepository.invalidarOtpsPendientesPorCorreo(request.getCorreo());

        // Generar código OTP de 6 dígitos
        String codigoPlano = String.format("%06d", new Random().nextInt(999999));
        String codigoCifrado = passwordEncoder.encode(codigoPlano);

        OtpVerification otp = new OtpVerification(
                request.getCorreo(),
                codigoCifrado,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES),
                "PENDIENTE"
        );
        otpRepository.save(otp);

        // Generar token de reset
        String resetToken = UUID.randomUUID().toString();
        usuario.setResetToken(resetToken);
        usuario.setOtpCode(codigoCifrado);
        usuarioRepository.save(usuario);

        // Enviar correo (o log en dev mode)
        if (emailDevMode) {
            log.info("=== DEV MODE === OTP para {}: {} | Reset Token: {}", request.getCorreo(), codigoPlano, resetToken);
        } else {
            enviarCorreoOtp(request.getCorreo(), codigoPlano);
        }

        return ApiResponseDTO.ok("Se envió un código de verificación a su correo.");
    }

    @Override
    @Transactional
    public ApiResponseDTO resetearPassword(ResetPasswordDTO request) {
        validator.validarPasswordsCoinciden(request.getPasswordNueva(), request.getConfirmPasswordNueva());

        Usuario usuario = usuarioRepository.findByResetToken(request.getToken())
                .orElseThrow(() -> new InvalidPasswordException("Token de recuperación inválido o expirado"));

        usuario.setPassword(passwordEncoder.encode(request.getPasswordNueva()));
        usuario.setResetToken(null);
        usuario.setOtpCode(null);
        usuarioRepository.save(usuario);

        log.info("Contraseña restablecida para usuario: {}", usuario.getUsername());

        return ApiResponseDTO.ok("Contraseña restablecida exitosamente");
    }

    @Override
    @Transactional
    public ApiResponseDTO verificarOtp(VerificarOtpDTO request) {
        OtpVerification otp = otpRepository
                .findTopByCorreoAndEstadoOrderByFechaCreacionDesc(request.getCorreo(), "PENDIENTE")
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró un código OTP pendiente para este correo"));

        // Verificar expiración
        if (LocalDateTime.now().isAfter(otp.getFechaExpiracion())) {
            otp.setEstado("EXPIRADO");
            otpRepository.save(otp);
            throw new InvalidPasswordException("El código OTP ha expirado. Solicite uno nuevo.");
        }

        // Verificar código
        if (!passwordEncoder.matches(request.getCodigo(), otp.getCodigoCifrado())) {
            throw new InvalidPasswordException("El código OTP es incorrecto");
        }

        otp.setEstado("VALIDADO");
        otpRepository.save(otp);

        // Retornar el resetToken para que el frontend lo use en el siguiente paso
        Usuario usuario = usuarioRepository.findByCorreo(request.getCorreo())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        return ApiResponseDTO.ok("Código OTP verificado correctamente", usuario.getResetToken());
    }

    private void enviarCorreoOtp(String correo, String codigo) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailSender);
            message.setTo(correo);
            message.setSubject("VET EXPERTS - Código de Verificación");
            message.setText("Su código de verificación es: " + codigo +
                    "\n\nEste código expira en " + OTP_EXPIRATION_MINUTES + " minutos." +
                    "\n\nSi usted no solicitó este código, ignore este correo.");
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Error al enviar correo OTP a {}: {}", correo, e.getMessage());
        }
    }
}
