package com.vetexpert.sistema_veterinaria.vacunacion.controller;

import com.vetexpert.sistema_veterinaria.vacunacion.service.AlertaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.Map;

/**
 * Controlador MVC para el panel consolidado de Alertas y Dashboard del módulo.
 */
@Controller
@RequestMapping("/alertas")
public class AlertasController {

    private final AlertaService alertaService;

    public AlertasController(AlertaService alertaService) {
        this.alertaService = alertaService;
    }

    /**
     * Muestra el Dashboard de Alertas consolidado.
     */
    @GetMapping
    public String mostrarDashboardAlertas(Model model, HttpSession session) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/auth/login";
        }

        // Ejecutar proceso automático al cargar la vista para garantizar datos frescos
        alertaService.procesarAlertasAutomaticas();

        // Cargar métricas del dashboard
        Map<String, Object> metricas = alertaService.obtenerMetricasDashboard();
        model.addAllAttributes(metricas);

        // Cargar listas de alertas para cada sección del panel
        model.addAttribute("vacunasProximas", alertaService.obtenerVacunasProximas());
        model.addAttribute("vacunasVencidas", alertaService.obtenerVacunasVencidas());
        model.addAttribute("desparasitacionesProximas", alertaService.obtenerDesparasitacionesProximas());
        model.addAttribute("desparasitacionesVencidas", alertaService.obtenerDesparasitacionesVencidas());

        model.addAttribute("activePage", "alertas");

        return "alertas/dashboard-alertas";
    }
}
