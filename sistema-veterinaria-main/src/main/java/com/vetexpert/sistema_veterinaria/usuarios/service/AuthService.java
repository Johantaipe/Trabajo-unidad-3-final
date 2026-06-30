package com.vetexpert.sistema_veterinaria.usuarios.service;

import com.vetexpert.sistema_veterinaria.usuarios.entity.Usuario;

import java.util.Optional;

/**
 * Interfaz del servicio de Autenticación.
 * Define el contrato para las operaciones de login.
 * Preparada para migración futura a Spring Security + JWT.
 */
public interface AuthService {

    /**
     * Autentica un usuario por username y password.
     * @return Optional con el usuario si las credenciales son correctas, vacío en caso contrario.
     */
    Optional<Usuario> autenticar(String username, String password);

    /**
     * Busca un usuario por su nombre de usuario.
     */
    Optional<Usuario> buscarPorUsername(String username);

    /**
     * Verifica si existe un usuario con el nombre de usuario dado.
     */
    boolean existePorUsername(String username);

    /**
     * Registra un nuevo cliente con estado deshabilitado y genera un código OTP de 6 dígitos.
     */
    Usuario registrarCliente(Usuario nuevoUsuario, String dni, String direccion);

    /**
     * Verifica si existe un propietario con el DNI proporcionado.
     */
    boolean existePropietarioPorDni(String dni);

    /**
     * Verifica el código OTP para habilitar la cuenta de un cliente.
     */
    boolean verificarOtp(String email, String otp);

    /**
     * Genera un token temporal para la recuperación de contraseña y envía el enlace.
     */
    boolean generarTokenRecuperacion(String email);

    /**
     * Cambia la contraseña usando el token temporal y lo inhabilita.
     */
    boolean restablecerPassword(String token, String nuevaPassword);

    /**
     * Registra o loguea un cliente usando el correo y perfil proveniente de Google OAuth2.
     */
    Usuario loginOauthGoogle(String email, String nombre, String apellido);
}
