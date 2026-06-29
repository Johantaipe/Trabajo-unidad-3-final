package com.vetexpert.sistema_veterinaria.hospitalizacion.controller;

import com.vetexpert.sistema_veterinaria.hospitalizacion.model.Hospitalizacion;
import com.vetexpert.sistema_veterinaria.hospitalizacion.service.HospitalizacionService;
import com.vetexpert.sistema_veterinaria.mascotas.service.MascotaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/hospitalizacion")
public class HospitalizacionController {

    private final HospitalizacionService hospitalizacionService;
    private final MascotaService mascotaService;

    public HospitalizacionController(HospitalizacionService hospitalizacionService, MascotaService mascotaService) {
        this.hospitalizacionService = hospitalizacionService;
        this.mascotaService = mascotaService;
    }

    @GetMapping
    public String listar(Model model, @RequestParam(value = "query", required = false) String query) {
        model.addAttribute("activePage", "hospitalizacion");
        if (query != null && !query.trim().isEmpty()) {
            model.addAttribute("lista", hospitalizacionService.buscar(query));
            model.addAttribute("query", query);
        } else {
            model.addAttribute("lista", hospitalizacionService.listarTodas());
        }
        return "hospitalizacion/lista";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("activePage", "hospitalizacion");
        model.addAttribute("hospitalizacion", new Hospitalizacion());
        model.addAttribute("mascotas", mascotaService.listarMascotas());
        return "hospitalizacion/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("hospitalizacion") Hospitalizacion hospitalizacion,
                          BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("activePage", "hospitalizacion");
            model.addAttribute("mascotas", mascotaService.listarMascotas());
            return "hospitalizacion/formulario";
        }
        hospitalizacionService.registrarHospitalizacion(hospitalizacion);
        return "redirect:/hospitalizacion";
    }

    @GetMapping("/alta/{id}")
    public String mostrarFormularioAlta(@PathVariable("id") Long id, Model model) {
        Hospitalizacion hosp = hospitalizacionService.obtenerPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Hospitalización no encontrada"));
        model.addAttribute("activePage", "hospitalizacion");
        model.addAttribute("hospitalizacion", hosp);
        return "hospitalizacion/alta";
    }

    @PostMapping("/alta/{id}")
    public String procesarAlta(@PathVariable("id") Long id,
                               @ModelAttribute("hospitalizacion") Hospitalizacion datosAlta) {
        hospitalizacionService.darAlta(id, datosAlta);
        return "redirect:/hospitalizacion";
    }

    @GetMapping("/detalle/{id}")
    public String verDetalle(@PathVariable("id") Long id, Model model) {
        Hospitalizacion hosp = hospitalizacionService.obtenerPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Hospitalización no encontrada"));
        model.addAttribute("activePage", "hospitalizacion");
        model.addAttribute("h", hosp);
        return "hospitalizacion/detalle";
    }
}
