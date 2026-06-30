package com.vetexpert.sistema_veterinaria.inventario.controller;

import com.vetexpert.sistema_veterinaria.inventario.entity.Proveedor;
import com.vetexpert.sistema_veterinaria.inventario.service.ProveedorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/inventario/proveedores")
public class ProveedorController {

    private final ProveedorService proveedorService;

    public ProveedorController(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

    @GetMapping
    public String listar(Model model, @RequestParam(value = "query", required = false) String query) {
        model.addAttribute("activePage", "inventario");
        model.addAttribute("activeSubPage", "proveedores");
        if (query != null && !query.trim().isEmpty()) {
            model.addAttribute("lista", proveedorService.buscarProveedores(query));
            model.addAttribute("query", query);
        } else {
            model.addAttribute("lista", proveedorService.listarProveedores());
        }
        return "inventario/proveedores/lista";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("activePage", "inventario");
        model.addAttribute("activeSubPage", "proveedores");
        model.addAttribute("proveedor", new Proveedor());
        return "inventario/proveedores/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("proveedor") Proveedor proveedor,
                          BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("activePage", "inventario");
            model.addAttribute("activeSubPage", "proveedores");
            return "inventario/proveedores/formulario";
        }
        proveedorService.registrarProveedor(proveedor);
        return "redirect:/inventario/proveedores";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable("id") Long id, Model model) {
        Proveedor proveedor = proveedorService.obtenerProveedorPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado con ID: " + id));
        model.addAttribute("activePage", "inventario");
        model.addAttribute("activeSubPage", "proveedores");
        model.addAttribute("proveedor", proveedor);
        return "inventario/proveedores/formulario";
    }

    @PostMapping("/actualizar/{id}")
    public String actualizar(@PathVariable("id") Long id,
                             @Valid @ModelAttribute("proveedor") Proveedor proveedor,
                             BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("activePage", "inventario");
            model.addAttribute("activeSubPage", "proveedores");
            return "inventario/proveedores/formulario";
        }
        proveedorService.actualizarProveedor(id, proveedor);
        return "redirect:/inventario/proveedores";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable("id") Long id) {
        proveedorService.eliminarProveedor(id);
        return "redirect:/inventario/proveedores";
    }
}
