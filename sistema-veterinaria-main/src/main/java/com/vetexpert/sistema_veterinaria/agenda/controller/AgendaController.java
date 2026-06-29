package com.vetexpert.sistema_veterinaria.agenda.controller;

import com.vetexpert.sistema_veterinaria.agenda.dto.CitaDTO;
import com.vetexpert.sistema_veterinaria.agenda.model.Cita;
import com.vetexpert.sistema_veterinaria.agenda.model.EstadoCita;
import com.vetexpert.sistema_veterinaria.agenda.model.PrioridadCita;
import com.vetexpert.sistema_veterinaria.agenda.model.TipoCita;
import com.vetexpert.sistema_veterinaria.agenda.service.CitaService;
import com.vetexpert.sistema_veterinaria.mascotas.service.MascotaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/agenda")
public class AgendaController {

    private final CitaService citaService;
    private final MascotaService mascotaService;

    public AgendaController(CitaService citaService, MascotaService mascotaService) {
        this.citaService = citaService;
        this.mascotaService = mascotaService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("activePage", "agenda");
        java.util.List<Cita> list = citaService.listarCitas();
        model.addAttribute("lista", list);
        
        long countProgramadas = list.stream().filter(c -> c.getEstado() == EstadoCita.PROGRAMADA).count();
        long countConfirmadas = list.stream().filter(c -> c.getEstado() == EstadoCita.CONFIRMADA).count();
        long countUrgentes = list.stream().filter(c -> c.getPrioridad() == PrioridadCita.URGENTE).count();
        
        model.addAttribute("countProgramadas", countProgramadas);
        model.addAttribute("countConfirmadas", countConfirmadas);
        model.addAttribute("countUrgentes", countUrgentes);
        return "agenda/lista";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("activePage", "agenda");
        
        CitaDTO dto = new CitaDTO();
        dto.setDuracionMinutos(30); // por defecto
        
        model.addAttribute("cita", dto);
        model.addAttribute("mascotas", mascotaService.listarMascotas());
        model.addAttribute("prioridades", PrioridadCita.values());
        model.addAttribute("tipos", TipoCita.values());
        return "agenda/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("cita") CitaDTO dto,
                          BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("activePage", "agenda");
            model.addAttribute("mascotas", mascotaService.listarMascotas());
            model.addAttribute("prioridades", PrioridadCita.values());
            model.addAttribute("tipos", TipoCita.values());
            return "agenda/formulario";
        }
        try {
            citaService.registrarCita(dto);
        } catch (IllegalArgumentException e) {
            model.addAttribute("activePage", "agenda");
            model.addAttribute("errorCollision", e.getMessage());
            model.addAttribute("mascotas", mascotaService.listarMascotas());
            model.addAttribute("prioridades", PrioridadCita.values());
            model.addAttribute("tipos", TipoCita.values());
            return "agenda/formulario";
        }
        return "redirect:/agenda";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable("id") Long id, Model model) {
        Cita c = citaService.obtenerPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada"));
        
        CitaDTO dto = new CitaDTO();
        dto.setId(c.getId());
        dto.setCodigoCita(c.getCodigoCita());
        dto.setMascotaId(c.getMascota().getId());
        dto.setPropietarioId(c.getPropietario().getId());
        dto.setVeterinario(c.getVeterinario());
        dto.setFecha(c.getFecha());
        dto.setHora(c.getHora());
        dto.setDuracionMinutos(c.getDuracionMinutos());
        dto.setTipoCita(c.getTipoCita());
        dto.setMotivoConsulta(c.getMotivoConsulta());
        dto.setEstado(c.getEstado());
        dto.setPrioridad(c.getPrioridad());
        dto.setObservaciones(c.getObservaciones());
        dto.setRecordatorioProgramado(c.isRecordatorioProgramado());

        model.addAttribute("activePage", "agenda");
        model.addAttribute("cita", dto);
        model.addAttribute("mascotas", mascotaService.listarMascotas());
        model.addAttribute("prioridades", PrioridadCita.values());
        model.addAttribute("tipos", TipoCita.values());
        model.addAttribute("estados", EstadoCita.values());
        return "agenda/formulario";
    }

    @PostMapping("/actualizar/{id}")
    public String actualizar(@PathVariable("id") Long id,
                             @Valid @ModelAttribute("cita") CitaDTO dto,
                             BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("activePage", "agenda");
            model.addAttribute("mascotas", mascotaService.listarMascotas());
            model.addAttribute("prioridades", PrioridadCita.values());
            model.addAttribute("tipos", TipoCita.values());
            model.addAttribute("estados", EstadoCita.values());
            return "agenda/formulario";
        }
        try {
            citaService.actualizarCita(id, dto);
        } catch (IllegalArgumentException e) {
            model.addAttribute("activePage", "agenda");
            model.addAttribute("errorCollision", e.getMessage());
            model.addAttribute("mascotas", mascotaService.listarMascotas());
            model.addAttribute("prioridades", PrioridadCita.values());
            model.addAttribute("tipos", TipoCita.values());
            model.addAttribute("estados", EstadoCita.values());
            return "agenda/formulario";
        }
        return "redirect:/agenda";
    }

    @GetMapping("/cancelar/{id}")
    public String cancelar(@PathVariable("id") Long id) {
        citaService.eliminarCita(id);
        return "redirect:/agenda";
    }
}
