package com.vetexpert.sistema_veterinaria.auth.controller;

import com.vetexpert.sistema_veterinaria.auth.model.Usuario;
import com.vetexpert.sistema_veterinaria.auth.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 * Controlador MVC para el módulo de Autenticación.
 * Gestiona el inicio y cierre de sesión.
 * Preparado para migración futura a Spring Security.
 */
@Controller
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Muestra la página de inicio de sesión.
     */
    @GetMapping("/login")
    public String mostrarLogin(Model model, HttpSession session) {
        // Si ya tiene sesión activa, redirigir al dashboard
        if (session.getAttribute("usuario") != null) {
            return "redirect:/dashboard";
        }
        return "auth/login";
    }

    /**
     * Procesa el formulario de inicio de sesión.
     * Valida campos vacíos, existencia del usuario y contraseña.
     */
    @PostMapping("/login")
    public String procesarLogin(@RequestParam(required = false) String username,
                                @RequestParam(required = false) String password,
                                Model model,
                                HttpSession session) {
        // Validar campos vacíos
        if (username == null || username.trim().isEmpty()) {
            model.addAttribute("errorUsername", "El nombre de usuario es obligatorio");
            model.addAttribute("username", username);
            return "auth/login";
        }

        if (password == null || password.trim().isEmpty()) {
            model.addAttribute("errorPassword", "La contraseña es obligatoria");
            model.addAttribute("username", username);
            return "auth/login";
        }

        // Verificar si el usuario existe
        if (!authService.existePorUsername(username.trim())) {
            model.addAttribute("errorUsername", "El usuario no existe");
            model.addAttribute("username", username);
            return "auth/login";
        }

        // Intentar autenticar
        Optional<Usuario> usuario = authService.autenticar(username.trim(), password);
        if (usuario.isPresent()) {
            // Guardar datos en sesión
            session.setAttribute("usuario", usuario.get());
            session.setAttribute("username", usuario.get().getUsername());
            session.setAttribute("rol", usuario.get().getRol());
            session.setMaxInactiveInterval(1800); // 30 minutos
            return "redirect:/dashboard";
        } else {
            model.addAttribute("errorPassword", "Contraseña incorrecta");
            model.addAttribute("username", username);
            return "auth/login";
        }
    }

    /**
     * Cierra la sesión del usuario y redirige al login administrativo.
     */
    @GetMapping("/logout")
    public String cerrarSesion(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("mensajeExito", "Sesión cerrada correctamente");
        return "redirect:/";
    }
}
