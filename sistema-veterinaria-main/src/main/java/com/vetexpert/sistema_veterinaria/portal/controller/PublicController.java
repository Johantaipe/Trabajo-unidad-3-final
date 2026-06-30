package com.vetexpert.sistema_veterinaria.portal.controller;

import com.vetexpert.sistema_veterinaria.portal.entity.Promocion;
import com.vetexpert.sistema_veterinaria.portal.service.PromocionService;
import com.vetexpert.sistema_veterinaria.portal.entity.ContactoMensaje;
import com.vetexpert.sistema_veterinaria.portal.service.ContactoMensajeService;
import com.vetexpert.sistema_veterinaria.usuarios.entity.Usuario;
import com.vetexpert.sistema_veterinaria.usuarios.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class PublicController {

    private final PromocionService promocionService;
    private final ContactoMensajeService contactoMensajeService;
    private final UsuarioRepository usuarioRepository;

    public PublicController(PromocionService promocionService,
                            ContactoMensajeService contactoMensajeService,
                            UsuarioRepository usuarioRepository) {
        this.promocionService = promocionService;
        this.contactoMensajeService = contactoMensajeService;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/")
    public String index(Model model) {
        List<Promocion> promos = promocionService.listarPromocionesVigentes();
        model.addAttribute("promociones", promos);
        return "index";
    }

    @PostMapping("/contacto/enviar")
    public String enviarContacto(@ModelAttribute ContactoMensaje mensaje, RedirectAttributes redirectAttributes) {
        contactoMensajeService.guardar(mensaje);
        redirectAttributes.addFlashAttribute("mensajeExito", "¡Gracias! Tu mensaje ha sido enviado correctamente. Nos pondremos en contacto contigo pronto.");
        return "redirect:/#contacto";
    }

    @GetMapping("/auth/registro")
    public String mostrarRegistro(Model model, HttpSession session) {
        return "redirect:/registro-cliente";
    }

    @PostMapping("/auth/registro")
    public String registrarCliente(RedirectAttributes redirectAttributes) {
        return "redirect:/registro-cliente";
    }

    @GetMapping("/terminos-de-uso")
    public String terminosDeUso() {
        return "legal/terminos-de-uso";
    }

    @GetMapping("/politica-privacidad")
    public String politicaPrivacidad() {
        return "legal/politica-privacidad";
    }
}
