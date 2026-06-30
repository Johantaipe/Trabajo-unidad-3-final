package com.vetexpert.sistema_veterinaria.dashboard.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador para la página principal y el dashboard.
 */
@Controller
public class HomeController {

    /**
     * Redirige /admin al login del sistema interno.
     */
    @GetMapping("/admin")
    public String adminRedirect() {
        return "redirect:/login-admin";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/";
        }
        // Redirigir según el rol del personal
        String rol = (String) session.getAttribute("rol");
        if ("CLIENTE".equals(rol)) {
            return "redirect:/portal-cliente/dashboard";
        }
        if ("ADMIN".equals(rol)) {
            return "redirect:/usuarios";
        }
        if ("VETERINARIO".equals(rol) || "PRACTICANTE".equals(rol)) {
            return "redirect:/agenda";
        }
        if ("SECRETARIA".equals(rol) || "RECEPCIONISTA".equals(rol)) {
            return "redirect:/agenda";
        }
        return "redirect:/";
    }

    @GetMapping("/configuracion")
    public String configuracion(Model model, HttpSession session) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/";
        }
        // Solo personal autorizado puede acceder a configuración
        String rol = (String) session.getAttribute("rol");
        if ("CLIENTE".equals(rol)) {
            return "redirect:/portal-cliente/dashboard";
        }
        model.addAttribute("activePage", "configuracion");
        return "configuracion";
    }

    @GetMapping("/reportes")
    public String reportes(HttpSession session) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/";
        }
        // Solo personal autorizado puede ver reportes
        String rol = (String) session.getAttribute("rol");
        if ("CLIENTE".equals(rol)) {
            return "redirect:/portal-cliente/dashboard";
        }
        return "redirect:/caja/reportes";
    }

    @GetMapping("/403")
    public String accessDenied() {
        return "error/403";
    }
}
