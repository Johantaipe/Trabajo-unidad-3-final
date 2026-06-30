package com.vetexpert.usuarios.validator;

import com.vetexpert.usuarios.exception.InvalidPasswordException;
import org.springframework.stereotype.Component;

/**
 * Validaciones de negocio reutilizables para passwords y usuarios.
 */
@Component
public class UsuarioValidator {

    /**
     * Verifica que las contraseñas coincidan.
     */
    public void validarPasswordsCoinciden(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            throw new InvalidPasswordException("Las contraseñas no coinciden");
        }
    }

    /**
     * Verifica que el rol sea válido.
     */
    public void validarRol(String rol) {
        if (rol == null) {
            throw new IllegalArgumentException("El rol no puede ser nulo");
        }
        switch (rol.toUpperCase()) {
            case "ADMIN", "VETERINARIO", "SECRETARIA", "CLIENTE" -> { /* válido */ }
            default -> throw new IllegalArgumentException(
                    "Rol inválido: " + rol + ". Roles válidos: ADMIN, VETERINARIO, SECRETARIA, CLIENTE");
        }
    }

    /**
     * Verifica que el username no contenga caracteres inválidos.
     */
    public void validarUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("El username no puede estar vacío");
        }
        if (!username.matches("^[a-zA-Z0-9._-]{3,50}$")) {
            throw new IllegalArgumentException(
                    "El username solo puede contener letras, números, puntos, guiones y guiones bajos (3-50 caracteres)");
        }
    }
}
