package com.vetexpert.usuarios.repository;

import com.vetexpert.usuarios.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    Optional<Usuario> findByCorreo(String correo);

    Optional<Usuario> findByResetToken(String resetToken);

    boolean existsByUsername(String username);

    boolean existsByCorreo(String correo);
}
