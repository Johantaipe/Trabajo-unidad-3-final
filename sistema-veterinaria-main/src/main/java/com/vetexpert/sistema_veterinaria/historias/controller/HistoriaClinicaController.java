package com.vetexpert.sistema_veterinaria.historias.controller;

import com.vetexpert.sistema_veterinaria.historias.dto.HistoriaClinicaDTO;
import com.vetexpert.sistema_veterinaria.historias.model.EstadoConsulta;
import com.vetexpert.sistema_veterinaria.historias.model.HistoriaClinica;
import com.vetexpert.sistema_veterinaria.historias.service.HistoriaClinicaService;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Controlador MVC para el módulo de Historias Clínicas.
 * Gestiona vistas Thymeleaf y operaciones clínicas de consulta.
 */
@Controller
@RequestMapping("/historias")
public class HistoriaClinicaController {

    private final HistoriaClinicaService historiaClinicaService;
    private final MascotaService mascotaService;

    public HistoriaClinicaController(HistoriaClinicaService historiaClinicaService, MascotaService mascotaService) {
        this.historiaClinicaService = historiaClinicaService;
        this.mascotaService = mascotaService;
    }

    /**
     * Lista todas las consultas con soporte para búsqueda por nombre de mascota, estado clínico y paginación.
     */
    @GetMapping
    public String listar(@RequestParam(value = "nombreMascota", required = false) String nombreMascota,
                         @RequestParam(value = "estado", required = false) String estadoStr,
                         @RequestParam(value = "page", defaultValue = "0") int page,
                         @RequestParam(value = "size", defaultValue = "5") int size,
                         Model model,
                         HttpSession session) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/auth/login";
        }

        EstadoConsulta estado = null;
        if (estadoStr != null && !estadoStr.trim().isEmpty() && !estadoStr.equalsIgnoreCase("TODOS")) {
            try {
                estado = EstadoConsulta.valueOf(estadoStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Estado inválido, ignorar filtro
            }
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<HistoriaClinica> consultasPage = historiaClinicaService.listarConsultasPaginado(nombreMascota, estado, pageable);

        model.addAttribute("consultasPage", consultasPage);
        model.addAttribute("nombreMascota", nombreMascota);
        model.addAttribute("estadoSeleccionado", estadoStr != null ? estadoStr : "TODOS");
        model.addAttribute("estados", EstadoConsulta.values());
        model.addAttribute("activePage", "historias");

        return "historias/lista";
    }

    /**
     * Muestra el formulario para registrar una nueva consulta clínica.
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(@RequestParam(value = "mascotaId", required = false) Long mascotaId,
                                          Model model,
                                          HttpSession session) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/auth/login";
        }

        HistoriaClinicaDTO dto = new HistoriaClinicaDTO();
        dto.setFechaConsulta(LocalDate.now());
        dto.setEstadoConsulta(EstadoConsulta.PENDIENTE);

        // Intentar autocompletar el veterinario con el usuario activo en la sesión
        String username = (String) session.getAttribute("username");
        if (username != null) {
            dto.setVeterinario(username);
        }

        if (mascotaId != null) {
            dto.setMascotaId(mascotaId);
            Optional<Mascota> mascotaOpt = mascotaService.obtenerMascotaPorId(mascotaId);
            mascotaOpt.ifPresent(mascota -> {
                if (mascota.getPeso() != null) {
                    dto.setPesoActual(mascota.getPeso());
                }
            });
        }

        model.addAttribute("consulta", dto);
        model.addAttribute("mascotas", mascotaService.listarMascotas());
        model.addAttribute("estados", EstadoConsulta.values());
        model.addAttribute("activePage", "historias");

        return "historias/formulario";
    }

    /**
     * Procesa el guardado de una nueva consulta clínica.
     */
    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("consulta") HistoriaClinicaDTO dto,
                          BindingResult result,
                          Model model,
                          RedirectAttributes redirectAttributes,
                          HttpSession session) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/auth/login";
        }

        if (result.hasErrors()) {
            model.addAttribute("mascotas", mascotaService.listarMascotas());
            model.addAttribute("estados", EstadoConsulta.values());
            model.addAttribute("activePage", "historias");
            return "historias/formulario";
        }

        try {
            historiaClinicaService.registrarConsulta(dto);
            redirectAttributes.addFlashAttribute("mensajeExito", "Consulta registrada exitosamente en el historial médico");
            return "redirect:/historias";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorNegocio", e.getMessage());
            model.addAttribute("mascotas", mascotaService.listarMascotas());
            model.addAttribute("estados", EstadoConsulta.values());
            model.addAttribute("activePage", "historias");
            return "historias/formulario";
        }
    }

    /**
     * Muestra el formulario para editar una consulta clínica existente.
     */
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id,
                                          Model model,
                                          RedirectAttributes redirectAttributes,
                                          HttpSession session) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/auth/login";
        }

        Optional<HistoriaClinica> consultaOpt = historiaClinicaService.obtenerDetalleConsulta(id);
        if (consultaOpt.isPresent()) {
            HistoriaClinicaDTO dto = convertirADTO(consultaOpt.get());
            model.addAttribute("consulta", dto);
            model.addAttribute("mascotas", mascotaService.listarMascotas());
            model.addAttribute("estados", EstadoConsulta.values());
            model.addAttribute("activePage", "historias");
            return "historias/formulario";
        } else {
            redirectAttributes.addFlashAttribute("mensajeError", "Consulta clínica no encontrada");
            return "redirect:/historias";
        }
    }

    /**
     * Procesa la actualización de una consulta clínica existente.
     */
    @PostMapping("/actualizar/{id}")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("consulta") HistoriaClinicaDTO dto,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttributes,
                             HttpSession session) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/auth/login";
        }

        if (result.hasErrors()) {
            model.addAttribute("mascotas", mascotaService.listarMascotas());
            model.addAttribute("estados", EstadoConsulta.values());
            model.addAttribute("activePage", "historias");
            return "historias/formulario";
        }

        try {
            historiaClinicaService.editarConsulta(id, dto);
            redirectAttributes.addFlashAttribute("mensajeExito", "Consulta clínica actualizada exitosamente");
            return "redirect:/historias";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorNegocio", e.getMessage());
            model.addAttribute("mascotas", mascotaService.listarMascotas());
            model.addAttribute("estados", EstadoConsulta.values());
            model.addAttribute("activePage", "historias");
            return "historias/formulario";
        }
    }

    /**
     * Elimina una consulta clínica del historial.
     */
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id,
                           RedirectAttributes redirectAttributes,
                           HttpSession session) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/auth/login";
        }

        try {
            historiaClinicaService.eliminarConsulta(id);
            redirectAttributes.addFlashAttribute("mensajeExito", "Consulta clínica eliminada del historial médico");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/historias";
    }

    /**
     * Muestra la vista detallada de una consulta clínica.
     */
    @GetMapping("/detalle/{id}")
    public String detalle(@PathVariable Long id,
                          Model model,
                          RedirectAttributes redirectAttributes,
                          HttpSession session) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/auth/login";
        }

        Optional<HistoriaClinica> consultaOpt = historiaClinicaService.obtenerDetalleConsulta(id);
        if (consultaOpt.isPresent()) {
            model.addAttribute("consulta", consultaOpt.get());
            model.addAttribute("activePage", "historias");
            return "historias/detalle";
        } else {
            redirectAttributes.addFlashAttribute("mensajeError", "Consulta clínica no encontrada");
            return "redirect:/historias";
        }
    }

    /**
     * Muestra la línea de tiempo e historial completo de consultas de una mascota en específico.
     */
    @GetMapping("/mascota/{id}")
    public String historialMascota(@PathVariable("id") Long mascotaId,
                                   Model model,
                                   RedirectAttributes redirectAttributes,
                                   HttpSession session) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/auth/login";
        }

        Optional<Mascota> mascotaOpt = mascotaService.obtenerMascotaPorId(mascotaId);
        if (mascotaOpt.isPresent()) {
            List<HistoriaClinica> consultas = historiaClinicaService.buscarPorMascota(mascotaId);
            model.addAttribute("mascota", mascotaOpt.get());
            model.addAttribute("consultas", consultas);
            model.addAttribute("totalConsultas", consultas.size());
            model.addAttribute("ultimaConsulta", consultas.isEmpty() ? null : consultas.get(0)); // Ya ordenadas por fecha DESC
            model.addAttribute("activePage", "historias");
            return "historias/historial-mascota";
        } else {
            redirectAttributes.addFlashAttribute("mensajeError", "Mascota no encontrada");
            return "redirect:/mascotas";
        }
    }

    /**
     * Redirecciona búsquedas hacia la ruta de listado general.
     */
    @GetMapping("/buscar")
    public String buscar(@RequestParam(value = "nombreMascota", required = false) String nombreMascota,
                         @RequestParam(value = "estado", required = false) String estado,
                         RedirectAttributes redirectAttributes) {
        redirectAttributes.addAttribute("nombreMascota", nombreMascota);
        redirectAttributes.addAttribute("estado", estado);
        return "redirect:/historias";
    }

    /**
     * Convierte una entidad HistoriaClinica en un HistoriaClinicaDTO.
     */
    private HistoriaClinicaDTO convertirADTO(HistoriaClinica consulta) {
        HistoriaClinicaDTO dto = new HistoriaClinicaDTO();
        dto.setId(consulta.getId());
        dto.setCodigoConsulta(consulta.getCodigoConsulta());
        dto.setFechaConsulta(consulta.getFechaConsulta());
        dto.setMotivoConsulta(consulta.getMotivoConsulta());
        dto.setAnamnesis(consulta.getAnamnesis());
        dto.setTemperatura(consulta.getTemperatura());
        dto.setPesoActual(consulta.getPesoActual());
        dto.setFrecuenciaCardiaca(consulta.getFrecuenciaCardiaca());
        dto.setFrecuenciaRespiratoria(consulta.getFrecuenciaRespiratoria());
        dto.setDiagnostico(consulta.getDiagnostico());
        dto.setTratamiento(consulta.getTratamiento());
        dto.setMedicamentos(consulta.getMedicamentos());
        dto.setRecomendaciones(consulta.getRecomendaciones());
        dto.setProximaVisita(consulta.getProximaVisita());
        dto.setEstadoConsulta(consulta.getEstadoConsulta());
        dto.setVeterinario(consulta.getVeterinario());
        dto.setMascotaId(consulta.getMascota().getId());
        return dto;
    }
}
