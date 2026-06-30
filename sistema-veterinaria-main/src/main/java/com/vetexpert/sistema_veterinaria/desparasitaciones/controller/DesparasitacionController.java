package com.vetexpert.sistema_veterinaria.desparasitaciones.controller;

import com.vetexpert.sistema_veterinaria.desparasitaciones.dto.DesparasitacionDTO;
import com.vetexpert.sistema_veterinaria.desparasitaciones.entity.Desparasitacion;
import com.vetexpert.sistema_veterinaria.vacunas.entity.EstadoVacuna;
import com.vetexpert.sistema_veterinaria.desparasitaciones.entity.TipoDesparasitacion;
import com.vetexpert.sistema_veterinaria.desparasitaciones.service.DesparasitacionService;
import com.vetexpert.sistema_veterinaria.mascotas.entity.Especie;
import com.vetexpert.sistema_veterinaria.mascotas.service.MascotaService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Optional;

/**
 * Controlador MVC para la gestión de Desparasitaciones.
 */
@Controller
@RequestMapping("/desparasitaciones")
public class DesparasitacionController {

    private final DesparasitacionService desparasitacionService;
    private final MascotaService mascotaService;

    public DesparasitacionController(DesparasitacionService desparasitacionService, MascotaService mascotaService) {
        this.desparasitacionService = desparasitacionService;
        this.mascotaService = mascotaService;
    }

    @GetMapping
    public String listar(@RequestParam(value = "nombreMascota", required = false) String nombreMascota,
                         @RequestParam(value = "especie", required = false) String especieStr,
                         @RequestParam(value = "page", defaultValue = "0") int page,
                         @RequestParam(value = "size", defaultValue = "5") int size,
                         Model model,
                         HttpSession session) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/auth/login";
        }

        Especie especie = null;
        if (especieStr != null && !especieStr.trim().isEmpty() && !especieStr.equalsIgnoreCase("TODAS")) {
            try {
                especie = Especie.valueOf(especieStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Especie inválida
            }
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Desparasitacion> desparasitacionesPage = desparasitacionService.listarDesparasitacionesPaginado(nombreMascota, especie, pageable);

        model.addAttribute("desparasitacionesPage", desparasitacionesPage);
        model.addAttribute("nombreMascota", nombreMascota);
        model.addAttribute("especieSeleccionada", especieStr != null ? especieStr : "TODAS");
        model.addAttribute("especies", Especie.values());
        model.addAttribute("activePage", "desparasitaciones");

        return "desparasitacion/lista";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(@RequestParam(value = "mascotaId", required = false) Long mascotaId, 
                                         Model model, 
                                         HttpSession session) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/auth/login";
        }

        DesparasitacionDTO dto = new DesparasitacionDTO();
        if (mascotaId != null) {
            dto.setMascotaId(mascotaId);
        }

        model.addAttribute("desparasitacion", dto);
        model.addAttribute("mascotas", mascotaService.listarMascotas());
        model.addAttribute("tipos", TipoDesparasitacion.values());
        model.addAttribute("estados", EstadoVacuna.values());
        model.addAttribute("activePage", "desparasitaciones");

        return "desparasitacion/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("desparasitacion") DesparasitacionDTO dto,
                          BindingResult result,
                          Model model,
                          RedirectAttributes redirectAttributes,
                          HttpSession session) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/auth/login";
        }

        if (result.hasErrors()) {
            model.addAttribute("mascotas", mascotaService.listarMascotas());
            model.addAttribute("tipos", TipoDesparasitacion.values());
            model.addAttribute("estados", EstadoVacuna.values());
            model.addAttribute("activePage", "desparasitaciones");
            return "desparasitacion/formulario";
        }

        try {
            desparasitacionService.registrarDesparasitacion(dto);
            redirectAttributes.addFlashAttribute("mensajeExito", "Desparasitación registrada exitosamente");
            return "redirect:/desparasitaciones";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorNegocio", e.getMessage());
            model.addAttribute("mascotas", mascotaService.listarMascotas());
            model.addAttribute("tipos", TipoDesparasitacion.values());
            model.addAttribute("estados", EstadoVacuna.values());
            model.addAttribute("activePage", "desparasitaciones");
            return "desparasitacion/formulario";
        }
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id,
                                          Model model,
                                          RedirectAttributes redirectAttributes,
                                          HttpSession session) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/auth/login";
        }

        Optional<Desparasitacion> despOpt = desparasitacionService.obtenerDesparasitacionPorId(id);
        if (despOpt.isPresent()) {
            model.addAttribute("desparasitacion", convertirADTO(despOpt.get()));
            model.addAttribute("mascotas", mascotaService.listarMascotas());
            model.addAttribute("tipos", TipoDesparasitacion.values());
            model.addAttribute("estados", EstadoVacuna.values());
            model.addAttribute("activePage", "desparasitaciones");
            return "desparasitacion/formulario";
        } else {
            redirectAttributes.addFlashAttribute("mensajeError", "Desparasitación no encontrada");
            return "redirect:/desparasitaciones";
        }
    }

    @PostMapping("/actualizar/{id}")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("desparasitacion") DesparasitacionDTO dto,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttributes,
                             HttpSession session) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/auth/login";
        }

        if (result.hasErrors()) {
            model.addAttribute("mascotas", mascotaService.listarMascotas());
            model.addAttribute("tipos", TipoDesparasitacion.values());
            model.addAttribute("estados", EstadoVacuna.values());
            model.addAttribute("activePage", "desparasitaciones");
            return "desparasitacion/formulario";
        }

        try {
            desparasitacionService.actualizarDesparasitacion(id, dto);
            redirectAttributes.addFlashAttribute("mensajeExito", "Desparasitación actualizada exitosamente");
            return "redirect:/desparasitaciones";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorNegocio", e.getMessage());
            model.addAttribute("mascotas", mascotaService.listarMascotas());
            model.addAttribute("tipos", TipoDesparasitacion.values());
            model.addAttribute("estados", EstadoVacuna.values());
            model.addAttribute("activePage", "desparasitaciones");
            return "desparasitacion/formulario";
        }
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id,
                           RedirectAttributes redirectAttributes,
                           HttpSession session) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/auth/login";
        }

        try {
            desparasitacionService.eliminarDesparasitacion(id);
            redirectAttributes.addFlashAttribute("mensajeExito", "Desparasitación eliminada exitosamente");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/desparasitaciones";
    }

    @GetMapping("/detalle/{id}")
    public String detalle(@PathVariable Long id,
                          Model model,
                          RedirectAttributes redirectAttributes,
                          HttpSession session) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/auth/login";
        }

        Optional<Desparasitacion> despOpt = desparasitacionService.obtenerDesparasitacionPorId(id);
        if (despOpt.isPresent()) {
            model.addAttribute("desparasitacion", despOpt.get());
            model.addAttribute("activePage", "desparasitaciones");
            return "desparasitacion/detalle";
        } else {
            redirectAttributes.addFlashAttribute("mensajeError", "Desparasitación no encontrada");
            return "redirect:/desparasitaciones";
        }
    }

    private DesparasitacionDTO convertirADTO(Desparasitacion desp) {
        DesparasitacionDTO dto = new DesparasitacionDTO();
        dto.setId(desp.getId());
        dto.setCodigoDesparasitacion(desp.getCodigoDesparasitacion());
        dto.setMascotaId(desp.getMascota().getId());
        dto.setTipo(desp.getTipo());
        dto.setProducto(desp.getProducto());
        dto.setFechaAplicacion(desp.getFechaAplicacion());
        dto.setFechaProximaAplicacion(desp.getFechaProximaAplicacion());
        dto.setVeterinario(desp.getVeterinario());
        dto.setObservaciones(desp.getObservaciones());
        dto.setEstado(desp.getEstado());
        dto.setPrecio(desp.getPrecio());
        return dto;
    }
}
