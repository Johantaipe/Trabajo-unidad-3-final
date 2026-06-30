package com.vetexpert.sistema_veterinaria.usuarios.service.impl;

import com.vetexpert.sistema_veterinaria.usuarios.entity.Usuario;
import com.vetexpert.sistema_veterinaria.usuarios.repository.UsuarioRepository;
import com.vetexpert.sistema_veterinaria.usuarios.service.AuthService;
import com.vetexpert.sistema_veterinaria.usuarios.service.EmailService;
import com.vetexpert.sistema_veterinaria.usuarios.service.WhatsAppOtpService;
import com.vetexpert.sistema_veterinaria.propietarios.entity.Propietario;
import com.vetexpert.sistema_veterinaria.propietarios.repository.PropietarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PropietarioRepository propietarioRepository;
    private final EmailService emailService;
    private final WhatsAppOtpService whatsAppOtpService;

    public AuthServiceImpl(UsuarioRepository usuarioRepository,
                           PropietarioRepository propietarioRepository,
                           EmailService emailService,
                           WhatsAppOtpService whatsAppOtpService) {
        this.usuarioRepository = usuarioRepository;
        this.propietarioRepository = propietarioRepository;
        this.emailService = emailService;
        this.whatsAppOtpService = whatsAppOtpService;
    }

    @Override
    public Optional<Usuario> autenticar(String username, String password) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            if (usuario.isEnabled()) {
                String dbPassword = usuario.getPassword();
                boolean match = false;
                if (dbPassword.startsWith("$2a$") || dbPassword.startsWith("$2b$")) {
                    match = org.mindrot.jbcrypt.BCrypt.checkpw(password, dbPassword);
                } else {
                    match = dbPassword.equals(password);
                }
                if (match) {
                    return Optional.of(usuario);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Usuario> buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    @Override
    public boolean existePorUsername(String username) {
        return usuarioRepository.existsByUsername(username);
    }

    @Override
    @Transactional
    public Usuario registrarCliente(Usuario nuevoUsuario, String dni, String direccion) {
        if (usuarioRepository.existsByUsername(nuevoUsuario.getUsername())) {
            throw new IllegalArgumentException("El correo ya se encuentra registrado.");
        }

        // Habilitar directamente (OTP validado por AJAX)
        nuevoUsuario.setEnabled(true);
        nuevoUsuario.setOtpCode(null);

        // Encriptar contraseña con BCrypt
        String hashedPassword = org.mindrot.jbcrypt.BCrypt.hashpw(nuevoUsuario.getPassword(), org.mindrot.jbcrypt.BCrypt.gensalt());
        nuevoUsuario.setPassword(hashedPassword);

        // 2. Guardar el usuario
        Usuario savedUser = usuarioRepository.save(nuevoUsuario);

        // 3. Crear el Propietario correspondiente con un DNI aleatorio y único, o usar el provisto
        String finalDni = dni;
        if (finalDni == null || finalDni.trim().isEmpty()) {
            do {
                finalDni = String.format("%08d", (int) (Math.random() * 90000000) + 10000000);
            } while (propietarioRepository.existsByDni(finalDni));
        } else {
            finalDni = finalDni.trim();
            if (propietarioRepository.existsByDni(finalDni)) {
                throw new IllegalArgumentException("El DNI ya se encuentra registrado.");
            }
        }

        Propietario propietario = new Propietario(
                nuevoUsuario.getNombre(),
                nuevoUsuario.getApellido(),
                finalDni,
                nuevoUsuario.getTelefono(),
                nuevoUsuario.getUsername()
        );
        
        if (direccion != null && !direccion.trim().isEmpty()) {
            propietario.setDireccion(direccion.trim() + " - " + nuevoUsuario.getDistrito());
        } else {
            propietario.setDireccion(nuevoUsuario.getDistrito());
        }
        propietarioRepository.save(propietario);

        return savedUser;
    }

    @Override
    public boolean existePropietarioPorDni(String dni) {
        if (dni == null || dni.trim().isEmpty()) {
            return false;
        }
        return propietarioRepository.existsByDni(dni.trim());
    }

    @Override
    @Transactional
    public boolean verificarOtp(String email, String otp) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(email);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            if (usuario.getOtpCode() != null && usuario.getOtpCode().equals(otp)) {
                usuario.setEnabled(true);
                usuario.setOtpCode(null); // Limpiar OTP
                usuarioRepository.save(usuario);
                return true;
            }
        }
        return false;
    }

    @Override
    @Transactional
    public boolean generarTokenRecuperacion(String email) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(email);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            String token = UUID.randomUUID().toString();
            usuario.setResetToken(token);
            usuarioRepository.save(usuario);

            String enlace = "http://localhost:8080/restablecer-contrasena?token=" + token;
            emailService.enviarRecuperacion(email, enlace);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean restablecerPassword(String token, String nuevaPassword) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByResetToken(token);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            String hashedPassword = org.mindrot.jbcrypt.BCrypt.hashpw(nuevaPassword, org.mindrot.jbcrypt.BCrypt.gensalt());
            usuario.setPassword(hashedPassword);
            usuario.setResetToken(null); // Limpiar token de uso único
            usuarioRepository.save(usuario);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public Usuario loginOauthGoogle(String email, String nombre, String apellido) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(email);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            // Asegurar que si inicia con Google, su cuenta esté activa
            if (!usuario.isEnabled()) {
                usuario.setEnabled(true);
                usuario.setOtpCode(null);
                usuarioRepository.save(usuario);
            }
            return usuario;
        } else {
            // Registro automático de usuario Google
            Usuario nuevo = new Usuario();
            nuevo.setUsername(email);
            // Contraseña aleatoria por defecto ya que usa Google OAuth
            String randomPass = java.util.UUID.randomUUID().toString();
            nuevo.setPassword(org.mindrot.jbcrypt.BCrypt.hashpw(randomPass, org.mindrot.jbcrypt.BCrypt.gensalt()));
            nuevo.setRol("CLIENTE");
            nuevo.setNombre(nombre);
            nuevo.setApellido(apellido);
            nuevo.setTelefono("");
            nuevo.setDistrito("");
            nuevo.setEnabled(true);
            
            Usuario savedUser = usuarioRepository.save(nuevo);

            // Crear propietario correspondiente
            String dni;
            do {
                dni = String.format("%08d", (int) (Math.random() * 90000000) + 10000000);
            } while (propietarioRepository.existsByDni(dni));

            Propietario propietario = new Propietario(nombre, apellido, dni, "", email);
            propietarioRepository.save(propietario);

            return savedUser;
        }
    }
}
