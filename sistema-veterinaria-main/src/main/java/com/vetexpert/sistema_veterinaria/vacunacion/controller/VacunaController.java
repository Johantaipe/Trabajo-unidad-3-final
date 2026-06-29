package com.vetexpert.sistema_veterinaria.vacunacion.controller;

import com.vetexpert.sistema_veterinaria.vacunacion.dto.VacunaDTO;
import com.vetexpert.sistema_veterinaria.vacunacion.model.EstadoVacuna;
import com.vetexpert.sistema_veterinaria.vacunacion.model.Vacuna;
import com.vetexpert.sistema_veterinaria.vacunacion.model.VacunaAplicada;
import com.vetexpert.sistema_veterinaria.vacunacion.service.VacunaService;
import com.vetexpert.sistema_veterinaria.vacunacion.service.VacunaAplicadaService;
import com.vetexpert.sistema_veterinaria.mascotas.model.Especie;
import com.vetexpert.sistema_veterinaria.mascotas.model.Mascota;
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
 * Controlador MVC para la gestión de Vacunas Aplicadas (Historial de Inmunización).
 */
@Controller
@RequestMapping("/vacunas")
public class VacunaController {

    private final VacunaAplicadaService vacunaAplicadaService;
    private final VacunaService vacunaService; // Para consultar el catálogo
    private final MascotaService mascotaService;

    public VacunaController(VacunaAplicadaService vacunaAplicadaService,
                            VacunaService vacunaService,
                            MascotaService mascotaService) {
        this.vacunaAplicadaService = vacunaAplicadaService;
        this.vacunaService = vacunaService;
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
        Page<VacunaAplicada> vacunasPage = vacunaAplicadaService.listarVacunasAplicadasPaginado(nombreMascota, especie, pageable);

        model.addAttribute("vacunasPage", vacunasPage);
        model.addAttribute("nombreMascota", nombreMascota);
        model.addAttribute("especieSeleccionada", especieStr != null ? especieStr : "TODAS");
        model.addAttribute("especies", Especie.values());
        model.addAttribute("activePage", "vacunas");

        return "vacunas/lista";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(@RequestParam(value = "mascotaId", required = false) Long mascotaId, 
                                         Model model, 
                                         HttpSession session) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/auth/login";
        }

        VacunaDTO dto = new VacunaDTO();
        if (mascotaId != null) {
            dto.setMascotaId(mascotaId);
        }

        model.addAttribute("vacuna", dto);
        model.addAttribute("mascotas", mascotaService.listarMascotas());
        model.addAttribute("vacunasCatalog", vacunaService.listarVacunasActivas());
        model.addAttribute("estados", EstadoVacuna.values());
        model.addAttribute("activePage", "vacunas");

        return "vacunas/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("vacuna") VacunaDTO dto,
                          BindingResult result,
                          Model model,
                          RedirectAttributes redirectAttributes,
                          HttpSession session) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/auth/login";
        }

        if (result.hasErrors()) {
            model.addAttribute("mascotas", mascotaService.listarMascotas());
            model.addAttribute("vacunasCatalog", vacunaService.listarVacunasActivas());
            model.addAttribute("estados", EstadoVacuna.values());
            model.addAttribute("activePage", "vacunas");
            return "vacunas/formulario";
        }

        try {
            vacunaAplicadaService.registrarVacunaAplicada(dto);
            redirectAttributes.addFlashAttribute("mensajeExito", "Vacuna registrada exitosamente");
            return "redirect:/vacunas";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorNegocio", e.getMessage());
            model.addAttribute("mascotas", mascotaService.listarMascotas());
            model.addAttribute("vacunasCatalog", vacunaService.listarVacunasActivas());
            model.addAttribute("estados", EstadoVacuna.values());
            model.addAttribute("activePage", "vacunas");
            return "vacunas/formulario";
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

        Optional<VacunaAplicada> vacunaOpt = vacunaAplicadaService.obtenerVacunaAplicadaPorId(id);
        if (vacunaOpt.isPresent()) {
            model.addAttribute("vacuna", convertirADTO(vacunaOpt.get()));
            model.addAttribute("mascotas", mascotaService.listarMascotas());
            model.addAttribute("vacunasCatalog", vacunaService.listarVacunasActivas());
            model.addAttribute("estados", EstadoVacuna.values());
            model.addAttribute("activePage", "vacunas");
            return "vacunas/formulario";
        } else {
            redirectAttributes.addFlashAttribute("mensajeError", "Registro de vacuna no encontrado");
            return "redirect:/vacunas";
        }
    }

    @PostMapping("/actualizar/{id}")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("vacuna") VacunaDTO dto,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttributes,
                             HttpSession session) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/auth/login";
        }

        if (result.hasErrors()) {
            model.addAttribute("mascotas", mascotaService.listarMascotas());
            model.addAttribute("vacunasCatalog", vacunaService.listarVacunasActivas());
            model.addAttribute("estados", EstadoVacuna.values());
            model.addAttribute("activePage", "vacunas");
            return "vacunas/formulario";
        }

        try {
            vacunaAplicadaService.actualizarVacunaAplicada(id, dto);
            redirectAttributes.addFlashAttribute("mensajeExito", "Registro de vacuna actualizado exitosamente");
            return "redirect:/vacunas";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorNegocio", e.getMessage());
            model.addAttribute("mascotas", mascotaService.listarMascotas());
            model.addAttribute("vacunasCatalog", vacunaService.listarVacunasActivas());
            model.addAttribute("estados", EstadoVacuna.values());
            model.addAttribute("activePage", "vacunas");
            return "vacunas/formulario";
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
            vacunaAplicadaService.eliminarVacunaAplicada(id);
            redirectAttributes.addFlashAttribute("mensajeExito", "Registro de vacuna eliminado exitosamente");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/vacunas";
    }

    @GetMapping("/detalle/{id}")
    public String detalle(@PathVariable Long id,
                          Model model,
                          RedirectAttributes redirectAttributes,
                          HttpSession session) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/auth/login";
        }

        Optional<VacunaAplicada> vacunaOpt = vacunaAplicadaService.obtenerVacunaAplicadaPorId(id);
        if (vacunaOpt.isPresent()) {
            model.addAttribute("vacuna", vacunaOpt.get());
            model.addAttribute("activePage", "vacunas");
            return "vacunas/detalle";
        } else {
            redirectAttributes.addFlashAttribute("mensajeError", "Registro de vacuna no encontrado");
            return "redirect:/vacunas";
        }
    }

    private VacunaDTO convertirADTO(VacunaAplicada vacuna) {
        VacunaDTO dto = new VacunaDTO();
        dto.setId(vacuna.getId());
        dto.setCodigoVacunaAplicada(vacuna.getCodigoVacunaAplicada());
        dto.setMascotaId(vacuna.getMascota().getId());
        dto.setVacunaId(vacuna.getVacuna().getId());
        dto.setFechaAplicacion(vacuna.getFechaAplicacion());
        dto.setFechaProximaDosis(vacuna.getFechaProximaDosis());
        dto.setEstado(vacuna.getEstado());
        dto.setLote(vacuna.getLote());
        dto.setLaboratorio(vacuna.getLaboratorio());
        dto.setVeterinario(vacuna.getVeterinario());
        dto.setObservaciones(vacuna.getObservaciones());
        dto.setPrecioCobrado(vacuna.getPrecioCobrado());
        return dto;
    }
}
