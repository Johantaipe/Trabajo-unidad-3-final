package com.vetexpert.sistema_veterinaria.mascotas.controller;

import com.vetexpert.sistema_veterinaria.mascotas.dto.MascotaDTO;
import com.vetexpert.sistema_veterinaria.mascotas.model.Especie;
import com.vetexpert.sistema_veterinaria.mascotas.model.Mascota;
import com.vetexpert.sistema_veterinaria.mascotas.model.Sexo;
import com.vetexpert.sistema_veterinaria.mascotas.service.MascotaService;
import com.vetexpert.sistema_veterinaria.propietarios.service.PropietarioService;
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
 * Controlador MVC para el módulo de Mascotas.
 * Gestiona vistas Thymeleaf y operaciones CRUD.
 */
@Controller
@RequestMapping("/mascotas")
public class MascotaController {

    private final MascotaService mascotaService;
    private final PropietarioService propietarioService;

    public MascotaController(MascotaService mascotaService, PropietarioService propietarioService) {
        this.mascotaService = mascotaService;
        this.propietarioService = propietarioService;
    }

    /**
     * Lista las mascotas con soporte para búsqueda dinámica, filtro por especie y paginación.
     */
    @GetMapping
    public String listar(@RequestParam(value = "nombre", required = false) String nombre,
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
                // Especie inválida, ignorar filtro
            }
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Mascota> mascotaPage = mascotaService.listarMascotasPaginado(nombre, especie, pageable);

        model.addAttribute("mascotasPage", mascotaPage);
        model.addAttribute("nombre", nombre);
        model.addAttribute("especieSeleccionada", especieStr != null ? especieStr : "TODAS");
        model.addAttribute("especies", Especie.values());
        model.addAttribute("activePage", "mascotas");

        return "mascotas/lista";
    }

    /**
     * Muestra el formulario para registrar una nueva mascota.
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model, HttpSession session) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/auth/login";
        }

        model.addAttribute("mascota", new MascotaDTO());
        model.addAttribute("propietarios", propietarioService.listarTodos());
        model.addAttribute("especies", Especie.values());
        model.addAttribute("sexos", Sexo.values());
        model.addAttribute("activePage", "mascotas");

        return "mascotas/formulario";
    }

    /**
     * Procesa el guardado de una nueva mascota.
     */
    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("mascota") MascotaDTO dto,
                          BindingResult result,
                          Model model,
                          RedirectAttributes redirectAttributes,
                          HttpSession session) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/auth/login";
        }

        if (result.hasErrors()) {
            model.addAttribute("propietarios", propietarioService.listarTodos());
            model.addAttribute("especies", Especie.values());
            model.addAttribute("sexos", Sexo.values());
            model.addAttribute("activePage", "mascotas");
            return "mascotas/formulario";
        }

        try {
            mascotaService.registrarMascota(dto);
            redirectAttributes.addFlashAttribute("mensajeExito", "Mascota registrada exitosamente");
            return "redirect:/mascotas";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorPropietario", e.getMessage());
            model.addAttribute("propietarios", propietarioService.listarTodos());
            model.addAttribute("especies", Especie.values());
            model.addAttribute("sexos", Sexo.values());
            model.addAttribute("activePage", "mascotas");
            return "mascotas/formulario";
        }
    }

    /**
     * Muestra el formulario para editar una mascota existente.
     */
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id,
                                          Model model,
                                          RedirectAttributes redirectAttributes,
                                          HttpSession session) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/auth/login";
        }

        Optional<Mascota> mascotaOpt = mascotaService.obtenerMascotaPorId(id);
        if (mascotaOpt.isPresent()) {
            MascotaDTO dto = convertirADTO(mascotaOpt.get());
            model.addAttribute("mascota", dto);
            model.addAttribute("propietarios", propietarioService.listarTodos());
            model.addAttribute("especies", Especie.values());
            model.addAttribute("sexos", Sexo.values());
            model.addAttribute("activePage", "mascotas");
            return "mascotas/formulario";
        } else {
            redirectAttributes.addFlashAttribute("mensajeError", "Mascota no encontrada");
            return "redirect:/mascotas";
        }
    }

    /**
     * Procesa la actualización de una mascota existente.
     */
    @PostMapping("/actualizar/{id}")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("mascota") MascotaDTO dto,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttributes,
                             HttpSession session) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/auth/login";
        }

        if (result.hasErrors()) {
            model.addAttribute("propietarios", propietarioService.listarTodos());
            model.addAttribute("especies", Especie.values());
            model.addAttribute("sexos", Sexo.values());
            model.addAttribute("activePage", "mascotas");
            return "mascotas/formulario";
        }

        try {
            mascotaService.actualizarMascota(id, dto);
            redirectAttributes.addFlashAttribute("mensajeExito", "Mascota actualizada exitosamente");
            return "redirect:/mascotas";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorPropietario", e.getMessage());
            model.addAttribute("propietarios", propietarioService.listarTodos());
            model.addAttribute("especies", Especie.values());
            model.addAttribute("sexos", Sexo.values());
            model.addAttribute("activePage", "mascotas");
            return "mascotas/formulario";
        }
    }

    /**
     * Elimina físicamente una mascota.
     */
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id,
                           RedirectAttributes redirectAttributes,
                           HttpSession session) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/auth/login";
        }

        try {
            mascotaService.eliminarMascota(id);
            redirectAttributes.addFlashAttribute("mensajeExito", "Mascota eliminada exitosamente");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/mascotas";
    }

    /**
     * Muestra la vista de detalle de una mascota.
     */
    @GetMapping("/detalle/{id}")
    public String detalle(@PathVariable Long id,
                          Model model,
                          RedirectAttributes redirectAttributes,
                          HttpSession session) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/auth/login";
        }

        Optional<Mascota> mascotaOpt = mascotaService.obtenerMascotaPorId(id);
        if (mascotaOpt.isPresent()) {
            model.addAttribute("mascota", mascotaOpt.get());
            model.addAttribute("activePage", "mascotas");
            return "mascotas/detalle";
        } else {
            redirectAttributes.addFlashAttribute("mensajeError", "Mascota no encontrada");
            return "redirect:/mascotas";
        }
    }

    /**
     * Redirecciona la búsqueda al listado general.
     */
    @GetMapping("/buscar")
    public String buscar(@RequestParam(value = "nombre", required = false) String nombre,
                         @RequestParam(value = "especie", required = false) String especie,
                         RedirectAttributes redirectAttributes) {
        redirectAttributes.addAttribute("nombre", nombre);
        redirectAttributes.addAttribute("especie", especie);
        return "redirect:/mascotas";
    }

    /**
     * Convierte una entidad Mascota a un MascotaDTO.
     */
    private MascotaDTO convertirADTO(Mascota mascota) {
        MascotaDTO dto = new MascotaDTO();
        dto.setId(mascota.getId());
        dto.setCodigoMascota(mascota.getCodigoMascota());
        dto.setNombre(mascota.getNombre());
        dto.setEspecie(mascota.getEspecie());
        dto.setRaza(mascota.getRaza());
        dto.setSexo(mascota.getSexo());
        dto.setFechaNacimiento(mascota.getFechaNacimiento());
        dto.setPeso(mascota.getPeso());
        dto.setColor(mascota.getColor());
        dto.setEsterilizado(mascota.isEsterilizado());
        dto.setObservaciones(mascota.getObservaciones());
        dto.setFotoUrl(mascota.getFotoUrl());
        dto.setPropietarioId(mascota.getPropietario().getId());
        return dto;
    }
}
