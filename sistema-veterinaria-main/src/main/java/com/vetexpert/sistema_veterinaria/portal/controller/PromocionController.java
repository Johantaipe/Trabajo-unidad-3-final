package com.vetexpert.sistema_veterinaria.portal.controller;

import com.vetexpert.sistema_veterinaria.portal.entity.Promocion;
import com.vetexpert.sistema_veterinaria.portal.service.PromocionService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/promociones")
public class PromocionController {

    private final PromocionService promocionService;
    private static final String UPLOAD_DIR = "src/main/resources/static/uploads/";

    public PromocionController(PromocionService promocionService) {
        this.promocionService = promocionService;
    }

    @GetMapping
    public String listar(Model model, HttpSession session) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/auth/login";
        }
        model.addAttribute("lista", promocionService.listarTodas());
        model.addAttribute("activePage", "promociones");
        return "promociones/lista";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model, HttpSession session) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/auth/login";
        }
        model.addAttribute("promocion", new Promocion());
        model.addAttribute("activePage", "promociones");
        return "promociones/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("promocion") Promocion promocion,
                          BindingResult bindingResult,
                          @RequestParam(value = "imagenFile", required = false) MultipartFile file,
                          Model model,
                          RedirectAttributes redirectAttributes,
                          HttpSession session) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/auth/login";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("activePage", "promociones");
            return "promociones/formulario";
        }

        if (file != null && !file.isEmpty()) {
            try {
                String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                Path uploadPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(file.getInputStream(), filePath);
                promocion.setImagenUrl("/uploads/" + fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        promocionService.guardar(promocion);
        redirectAttributes.addFlashAttribute("mensajeExito", "Promoción creada exitosamente");
        return "redirect:/promociones";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes, HttpSession session) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/auth/login";
        }

        Optional<Promocion> promoOpt = promocionService.obtenerPorId(id);
        if (promoOpt.isPresent()) {
            model.addAttribute("promocion", promoOpt.get());
            model.addAttribute("activePage", "promociones");
            return "promociones/formulario";
        } else {
            redirectAttributes.addFlashAttribute("mensajeError", "Promoción no encontrada");
            return "redirect:/promociones";
        }
    }

    @PostMapping("/actualizar/{id}")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("promocion") Promocion promocion,
                             BindingResult bindingResult,
                             @RequestParam(value = "imagenFile", required = false) MultipartFile file,
                             Model model,
                             RedirectAttributes redirectAttributes,
                             HttpSession session) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/auth/login";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("activePage", "promociones");
            return "promociones/formulario";
        }

        Optional<Promocion> existenteOpt = promocionService.obtenerPorId(id);
        if (!existenteOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("mensajeError", "Promoción no encontrada");
            return "redirect:/promociones";
        }

        Promocion existente = existenteOpt.get();
        if (file != null && !file.isEmpty()) {
            try {
                String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                Path uploadPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(file.getInputStream(), filePath);
                promocion.setImagenUrl("/uploads/" + fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            promocion.setImagenUrl(existente.getImagenUrl());
        }

        promocionService.actualizar(id, promocion);
        redirectAttributes.addFlashAttribute("mensajeExito", "Promoción actualizada exitosamente");
        return "redirect:/promociones";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpSession session) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/auth/login";
        }

        try {
            promocionService.eliminar(id);
            redirectAttributes.addFlashAttribute("mensajeExito", "Promoción eliminada exitosamente");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/promociones";
    }
}
