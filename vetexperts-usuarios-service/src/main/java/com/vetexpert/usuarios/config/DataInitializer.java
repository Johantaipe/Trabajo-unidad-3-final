package com.vetexpert.usuarios.config;

import com.vetexpert.usuarios.entity.Usuario;
import com.vetexpert.usuarios.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Inicializador de datos: crea el usuario admin por defecto si no existe.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!usuarioRepository.existsByUsername("admin")) {
            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRol("ADMIN");
            admin.setNombre("Administrador");
            admin.setApellido("Sistema");
            admin.setCorreo("admin@vetexperts.com");
            admin.setEnabled(true);

            usuarioRepository.save(admin);
            log.info("✅ Usuario ADMIN creado por defecto (admin / admin123)");
        } else {
            log.info("✅ Usuario ADMIN ya existe, omitiendo creación");
        }
    }
}
