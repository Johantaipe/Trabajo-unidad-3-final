package com.vetexpert.sistema_veterinaria.config;

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

    /**
     * Muestra el dashboard principal del sistema.
     * Solo accesible por personal autorizado (ADMIN, VETERINARIO, SECRETARIA).
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/";
        }
        // Si es CLIENTE, redirigir a su portal
        String rol = (String) session.getAttribute("rol");
        if ("CLIENTE".equals(rol)) {
            return "redirect:/portal-cliente/dashboard";
        }
        model.addAttribute("activePage", "dashboard");
        return "dashboard";
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
}
