package com.vetexpert.sistema_veterinaria.usuarios.repository;

import com.vetexpert.sistema_veterinaria.usuarios.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA para la entidad Usuario.
 * Proporciona operaciones CRUD y consultas personalizadas.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por su nombre de usuario.
     * Utilizado en el proceso de autenticación.
     */
    Optional<Usuario> findByUsername(String username);

    /**
     * Verifica si existe un usuario con el nombre de usuario dado.
     */
    boolean existsByUsername(String username);

    /**
     * Busca un usuario por su token de recuperación de contraseña.
     */
    Optional<Usuario> findByResetToken(String resetToken);

    /**
     * Verifica si existe un usuario con el correo dado.
     */
    boolean existsByCorreo(String correo);

    /**
     * Busca un usuario por su correo.
     */
    Optional<Usuario> findByCorreo(String correo);

    /**
     * Verifica si existe un usuario con el DNI dado.
     */
    boolean existsByDni(String dni);

    /**
     * Busca un usuario por su DNI.
     */
    Optional<Usuario> findByDni(String dni);

    /**
     * Busca usuarios activos por su rol.
     */
    java.util.List<Usuario> findByRolAndEnabledTrue(String rol);
}
