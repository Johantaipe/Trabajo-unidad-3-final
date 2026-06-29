package com.vetexpert.sistema_veterinaria.auth.service;

import com.vetexpert.sistema_veterinaria.auth.model.OtpVerification;
import com.vetexpert.sistema_veterinaria.auth.repository.OtpVerificationRepository;
import jakarta.servlet.http.HttpSession;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EmailVerificationService {

    private final EmailService emailService;
    private final OtpVerificationRepository otpVerificationRepository;

    @org.springframework.beans.factory.annotation.Value("${app.email.dev-mode:false}")
    private boolean devMode;

    public boolean isDevMode() {
        return devMode;
    }

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(EmailVerificationService.class);

    public EmailVerificationService(EmailService emailService, OtpVerificationRepository otpVerificationRepository) {
        this.emailService = emailService;
        this.otpVerificationRepository = otpVerificationRepository;
    }

    /**
     * Genera un código OTP aleatorio de 6 dígitos.
     */
    public String generarCodigoVerificacion() {
        return String.format("%06d", (int) (Math.random() * 900000) + 100000);
    }

    /**
     * Envía el código de verificación por correo real y lo registra en base de datos.
     * Aplica el límite anti-spam de máximo 3 envíos cada 15 minutos.
     */
    public void enviarCodigoVerificacion(String email, String codigo, HttpSession session) {
        String emailTrimmed = email.trim().toLowerCase();
        LocalDateTime now = LocalDateTime.now();

        // 1. Control de spam (Fase 10): Máximo 3 envíos cada 15 minutos
        LocalDateTime fifteenMinutesAgo = now.minusMinutes(15);
        List<OtpVerification> recentAttempts = otpVerificationRepository.findByCorreoAndFechaCreacionAfter(emailTrimmed, fifteenMinutesAgo);
        if (recentAttempts.size() >= 3) {
            throw new RuntimeException("Límite de envíos excedido. Máximo 3 envíos cada 15 minutos.");
        }

        // 2. Cifrar código con BCrypt para almacenarlo
        String codigoCifrado = BCrypt.hashpw(codigo, BCrypt.gensalt());

        // 3. Crear y guardar registro de OTP en BD
        LocalDateTime expiracion = now.plusMinutes(10); // Expira en 10 minutos
        OtpVerification verification = new OtpVerification(
                emailTrimmed,
                codigoCifrado,
                now,
                expiracion,
                "PENDIENTE"
        );
        otpVerificationRepository.save(verification);

        // 4. Guardar variables de estado iniciales en sesión
        session.setAttribute("regEmailOtpTime", System.currentTimeMillis());
        session.setAttribute("regEmail", emailTrimmed);
        session.setAttribute("regEmailVerified", false);

        if (devMode) {
            logger.info(">>> [MODO DESARROLLO] Código OTP generado para {}: {}", emailTrimmed, codigo);
            return;
        }

        // 5. Cuerpo HTML profesional de correo
        String asunto = "Verificación de cuenta - VET Experts";
        String contenidoHtml = "<div style='font-family: \"Plus Jakarta Sans\", Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 25px; border: 1px solid #e2e8f0; border-radius: 24px; background-color: #ffffff;'>" +
                "  <div style='text-align: center; margin-bottom: 25px;'>" +
                "    <h2 style='color: #2d6a4f; font-weight: 800; font-size: 28px; margin: 0;'>VET EXPERTS</h2>" +
                "    <p style='color: #f7b032; font-weight: 600; font-size: 14px; margin: 5px 0 0 0; text-transform: uppercase; letter-spacing: 1px;'>Cuidamos a quienes más amas</p>" +
                "  </div>" +
                "  <div style='border-bottom: 2px solid #f4f7f5; margin-bottom: 25px;'></div>" +
                "  <p style='font-size: 16px; color: #1d2d24; line-height: 1.6;'>Hola:</p>" +
                "  <p style='font-size: 16px; color: #1d2d24; line-height: 1.6;'>Gracias por registrarte en nuestra plataforma. Para completar tu registro, por favor ingresa el siguiente código de verificación:</p>" +
                "  <div style='text-align: center; margin: 35px 0;'>" +
                "    <span style='font-size: 34px; font-weight: 800; color: #2d6a4f; letter-spacing: 6px; padding: 12px 30px; background-color: #f4f7f5; border-radius: 16px; border: 2px dashed #52b788; display: inline-block;'>" + codigo + "</span>" +
                "  </div>" +
                "  <p style='font-size: 14px; color: #dc3545; font-weight: 600; margin-bottom: 25px;'>* Este código vence en 10 minutos y es de uso único.</p>" +
                "  <p style='font-size: 14px; color: #62756a; line-height: 1.5;'>" +
                "    Si no has solicitado este registro en VET Experts, puedes ignorar este mensaje con seguridad." +
                "  </p>" +
                "  <div style='border-bottom: 2px solid #f4f7f5; margin: 30px 0 20px 0;'></div>" +
                "  <p style='font-size: 12px; color: #62756a; text-align: center; line-height: 1.4;'>" +
                "    Veterinaria VET Experts &copy; 2026. Todos los derechos reservados.<br>" +
                "    Huancayo, Junín, Perú" +
                "  </p>" +
                "</div>";

        emailService.enviarHtml(emailTrimmed, asunto, contenidoHtml);
    }

    /**
     * Valida el código de verificación ingresado por el usuario.
     */
    public boolean verificarCodigo(String email, String codigoIngresado) {
        String emailTrimmed = email.trim().toLowerCase();
        Optional<OtpVerification> optVerification = otpVerificationRepository.findFirstByCorreoOrderByFechaCreacionDesc(emailTrimmed);

        if (optVerification.isEmpty()) {
            return false;
        }

        OtpVerification verification = optVerification.get();

        // 1. Validar que siga PENDIENTE
        if (!"PENDIENTE".equals(verification.getEstado())) {
            return false;
        }

        // 2. Validar que no haya expirado (10 minutos)
        if (verification.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            verification.setEstado("EXPIRADO");
            otpVerificationRepository.save(verification);
            return false;
        }

        // 3. Comparar código ingresado con el cifrado (BCrypt)
        if (BCrypt.checkpw(codigoIngresado.trim(), verification.getCodigoCifrado())) {
            verification.setEstado("VALIDADO");
            otpVerificationRepository.save(verification);
            return true;
        }

        return false;
    }
}
