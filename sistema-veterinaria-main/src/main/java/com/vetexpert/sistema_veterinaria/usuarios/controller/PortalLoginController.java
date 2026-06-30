package com.vetexpert.sistema_veterinaria.usuarios.controller;

import com.vetexpert.sistema_veterinaria.usuarios.entity.Usuario;
import com.vetexpert.sistema_veterinaria.usuarios.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class PortalLoginController {

    private final AuthService authService;
    private final com.vetexpert.sistema_veterinaria.usuarios.repository.UsuarioRepository usuarioRepository;

    public PortalLoginController(AuthService authService, com.vetexpert.sistema_veterinaria.usuarios.repository.UsuarioRepository usuarioRepository) {
        this.authService = authService;
        this.usuarioRepository = usuarioRepository;
    }

    // ==========================================
    // LOGIN PERSONAL (MODAL CENTRALIZADO)
    // ==========================================

    // ==========================================
    // PORTAL DEL PERSONAL - ACCESO UNIFICADO
    // ==========================================

    /**
     * Muestra la pagina dedicada de login del personal.
     * No requiere seleccion de rol: el sistema detecta automaticamente el rol
     * desde la BD y redirige al dashboard correspondiente.
     */
    @GetMapping({"/staff/login", "/login"})
    public String staffLogin(Model model, HttpSession session) {
        // Si ya hay sesion activa del personal, redirigir al dashboard
        Object usuario = session.getAttribute("usuario");
        String rol = (String) session.getAttribute("rol");
        if (usuario != null && rol != null && !"CLIENTE".equals(rol)) {
            return "redirect:/dashboard";
        }
        return "auth/staff-login";
    }

    @PostMapping("/login-personal")
    public String procesarLoginPersonal(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String password,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorLoginPersonal", "El correo o usuario y la contraseña son obligatorios");
            return "redirect:/?errorLoginPersonal=true";
        }

        Optional<Usuario> usuarioOpt = authService.autenticar(username.trim(), password);
        if (usuarioOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorLoginPersonal", "Credenciales incorrectas o cuenta inactiva");
            return "redirect:/?errorLoginPersonal=true";
        }

        Usuario usuario = usuarioOpt.get();
        String rol = usuario.getRol();

        // Validar que no sea un CLIENTE
        if ("CLIENTE".equals(rol)) {
            redirectAttributes.addFlashAttribute("errorLoginPersonal", "Acceso no autorizado para clientes en este portal");
            return "redirect:/?errorLoginPersonal=true";
        }

        // Registrar último acceso
        usuario.setUltimoAcceso(java.time.LocalDateTime.now());
        usuarioRepository.save(usuario);

        session.setAttribute("usuario", usuario);
        session.setAttribute("username", usuario.getUsername());
        session.setAttribute("rol", usuario.getRol());
        session.setMaxInactiveInterval(3600); // 1 hora

        return "redirect:/dashboard";
    }

    // ==========================================
    // LOGIN ADMINISTRADOR
    // ==========================================

    @GetMapping("/login-admin")
    public String loginAdmin(Model model, HttpSession session) {
        if (session.getAttribute("usuario") != null && "ADMIN".equals(session.getAttribute("rol"))) {
            return "redirect:/dashboard";
        }
        return "auth/login-admin";
    }

    @PostMapping("/login-admin")
    public String procesarLoginAdmin(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String password,
            Model model,
            HttpSession session) {

        if (username == null || username.trim().isEmpty()) {
            model.addAttribute("errorUsername", "El usuario o correo es obligatorio");
            return "auth/login-admin";
        }
        if (password == null || password.trim().isEmpty()) {
            model.addAttribute("username", username);
            model.addAttribute("errorPassword", "La contraseña es obligatoria");
            return "auth/login-admin";
        }

        Optional<Usuario> usuarioOpt = authService.autenticar(username.trim(), password);
        if (usuarioOpt.isEmpty()) {
            model.addAttribute("username", username);
            model.addAttribute("errorPassword", "Credenciales incorrectas o cuenta inactiva");
            return "auth/login-admin";
        }

        Usuario usuario = usuarioOpt.get();
        if (!"ADMIN".equals(usuario.getRol())) {
            model.addAttribute("username", username);
            model.addAttribute("errorAcceso", "No tiene permisos para acceder a este portal");
            return "auth/login-admin";
        }

        // Registrar último acceso
        usuario.setUltimoAcceso(java.time.LocalDateTime.now());
        usuarioRepository.save(usuario);

        session.setAttribute("usuario", usuario);
        session.setAttribute("username", usuario.getUsername());
        session.setAttribute("rol", usuario.getRol());
        session.setMaxInactiveInterval(3600); // 1 hora
        return "redirect:/dashboard";
    }

    // ==========================================
    // LOGIN SECRETARIA
    // ==========================================

    @GetMapping("/login-secretaria")
    public String loginSecretaria(Model model, HttpSession session) {
        if (session.getAttribute("usuario") != null && "SECRETARIA".equals(session.getAttribute("rol"))) {
            return "redirect:/dashboard";
        }
        return "auth/login-secretaria";
    }

    @PostMapping("/login-secretaria")
    public String procesarLoginSecretaria(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String password,
            Model model,
            HttpSession session) {

        if (username == null || username.trim().isEmpty()) {
            model.addAttribute("errorUsername", "El usuario o correo es obligatorio");
            return "auth/login-secretaria";
        }
        if (password == null || password.trim().isEmpty()) {
            model.addAttribute("username", username);
            model.addAttribute("errorPassword", "La contraseña es obligatoria");
            return "auth/login-secretaria";
        }

        Optional<Usuario> usuarioOpt = authService.autenticar(username.trim(), password);
        if (usuarioOpt.isEmpty()) {
            model.addAttribute("username", username);
            model.addAttribute("errorPassword", "Credenciales incorrectas o cuenta inactiva");
            return "auth/login-secretaria";
        }

        Usuario usuario = usuarioOpt.get();
        if (!"SECRETARIA".equals(usuario.getRol())) {
            model.addAttribute("username", username);
            model.addAttribute("errorAcceso", "No tiene permisos para acceder a este portal");
            return "auth/login-secretaria";
        }

        // Registrar último acceso
        usuario.setUltimoAcceso(java.time.LocalDateTime.now());
        usuarioRepository.save(usuario);

        session.setAttribute("usuario", usuario);
        session.setAttribute("username", usuario.getUsername());
        session.setAttribute("rol", usuario.getRol());
        session.setMaxInactiveInterval(3600);
        return "redirect:/dashboard";
    }

    // ==========================================
    // LOGIN VETERINARIO
    // ==========================================

    @GetMapping("/login-veterinario")
    public String loginVeterinario(Model model, HttpSession session) {
        if (session.getAttribute("usuario") != null && "VETERINARIO".equals(session.getAttribute("rol"))) {
            return "redirect:/dashboard";
        }
        return "auth/login-veterinario";
    }

    @PostMapping("/login-veterinario")
    public String procesarLoginVeterinario(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String password,
            Model model,
            HttpSession session) {

        if (username == null || username.trim().isEmpty()) {
            model.addAttribute("errorUsername", "El usuario o correo es obligatorio");
            return "auth/login-veterinario";
        }
        if (password == null || password.trim().isEmpty()) {
            model.addAttribute("username", username);
            model.addAttribute("errorPassword", "La contraseña es obligatoria");
            return "auth/login-veterinario";
        }

        Optional<Usuario> usuarioOpt = authService.autenticar(username.trim(), password);
        if (usuarioOpt.isEmpty()) {
            model.addAttribute("username", username);
            model.addAttribute("errorPassword", "Credenciales incorrectas o cuenta inactiva");
            return "auth/login-veterinario";
        }

        Usuario usuario = usuarioOpt.get();
        if (!"VETERINARIO".equals(usuario.getRol())) {
            model.addAttribute("username", username);
            model.addAttribute("errorAcceso", "No tiene permisos para acceder a este portal");
            return "auth/login-veterinario";
        }

        // Registrar último acceso
        usuario.setUltimoAcceso(java.time.LocalDateTime.now());
        usuarioRepository.save(usuario);

        session.setAttribute("usuario", usuario);
        session.setAttribute("username", usuario.getUsername());
        session.setAttribute("rol", usuario.getRol());
        session.setMaxInactiveInterval(3600);
        return "redirect:/dashboard";
    }
}
