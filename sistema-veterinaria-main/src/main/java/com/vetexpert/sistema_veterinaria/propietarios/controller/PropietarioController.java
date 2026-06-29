package com.vetexpert.sistema_veterinaria.propietarios.controller;

import com.vetexpert.sistema_veterinaria.propietarios.model.Propietario;
import com.vetexpert.sistema_veterinaria.propietarios.service.PropietarioService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 * Controlador MVC para el módulo de Propietarios.
 * Gestiona las vistas y operaciones CRUD.
 */
@Controller
@RequestMapping("/propietarios")
public class PropietarioController {

    private final PropietarioService propietarioService;

    public PropietarioController(PropietarioService propietarioService) {
        this.propietarioService = propietarioService;
    }

    /**
     * Lista todos los propietarios.
     */
    @GetMapping
    public String listar(Model model, HttpSession session) {
        // Verificar sesión activa
        if (session.getAttribute("usuario") == null) {
            return "redirect:/auth/login";
        }
        model.addAttribute("propietarios", propietarioService.listarTodos());
        model.addAttribute("activePage", "propietarios");
        return "propietarios/lista";
    }

    /**
     * Muestra el formulario para registrar un nuevo propietario.
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model, HttpSession session) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/auth/login";
        }
        model.addAttribute("propietario", new Propietario());
        model.addAttribute("activePage", "propietarios");
        return "propietarios/formulario";
    }

    /**
     * Procesa el formulario de registro de un nuevo propietario.
     * Aplica validación con @Valid y BindingResult.
     */
    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("propietario") Propietario propietario,
                          BindingResult result,
                          Model model,
                          RedirectAttributes redirectAttributes,
                          HttpSession session) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/auth/login";
        }

        // Validar errores de formulario
        if (result.hasErrors()) {
            model.addAttribute("activePage", "propietarios");
            return "propietarios/formulario";
        }

        try {
            propietarioService.guardar(propietario);
            redirectAttributes.addFlashAttribute("mensajeExito", "Propietario registrado exitosamente");
            return "redirect:/propietarios";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorDni", e.getMessage());
            model.addAttribute("activePage", "propietarios");
            return "propietarios/formulario";
        }
    }

    /**
     * Muestra el formulario para editar un propietario existente.
     */
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model,
                                          RedirectAttributes redirectAttributes,
                                          HttpSession session) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/auth/login";
        }

        Optional<Propietario> propietario = propietarioService.buscarPorId(id);
        if (propietario.isPresent()) {
            model.addAttribute("propietario", propietario.get());
            model.addAttribute("activePage", "propietarios");
            return "propietarios/formulario";
        } else {
            redirectAttributes.addFlashAttribute("mensajeError", "Propietario no encontrado");
            return "redirect:/propietarios";
        }
    }

    /**
     * Procesa la actualización de un propietario existente.
     */
    @PostMapping("/actualizar/{id}")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("propietario") Propietario propietario,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttributes,
                             HttpSession session) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/auth/login";
        }

        if (result.hasErrors()) {
            model.addAttribute("activePage", "propietarios");
            return "propietarios/formulario";
        }

        try {
            propietarioService.actualizar(id, propietario);
            redirectAttributes.addFlashAttribute("mensajeExito", "Propietario actualizado exitosamente");
            return "redirect:/propietarios";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorDni", e.getMessage());
            model.addAttribute("activePage", "propietarios");
            return "propietarios/formulario";
        }
    }

    /**
     * Elimina un propietario por su ID.
     */
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes,
                           HttpSession session) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/auth/login";
        }

        try {
            propietarioService.eliminar(id);
            redirectAttributes.addFlashAttribute("mensajeExito", "Propietario eliminado exitosamente");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/propietarios";
    }
}
