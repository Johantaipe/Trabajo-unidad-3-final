package com.vetexpert.sistema_veterinaria.agenda.controller.api;

import com.vetexpert.sistema_veterinaria.agenda.dto.ResumenMascotaDTO;
import com.vetexpert.sistema_veterinaria.agenda.entity.Cita;
import com.vetexpert.sistema_veterinaria.agenda.service.CitaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.vetexpert.sistema_veterinaria.usuarios.repository.UsuarioRepository;
import com.vetexpert.sistema_veterinaria.agenda.config.AgendaConfig;
import com.vetexpert.sistema_veterinaria.usuarios.entity.Usuario;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/agenda")
public class CitaRestController {

    private final CitaService citaService;
    private final UsuarioRepository usuarioRepository;
    private final AgendaConfig agendaConfig;

    public CitaRestController(CitaService citaService,
                              UsuarioRepository usuarioRepository,
                              AgendaConfig agendaConfig) {
        this.citaService = citaService;
        this.usuarioRepository = usuarioRepository;
        this.agendaConfig = agendaConfig;
    }

    @GetMapping("/mascota/{id}/resumen")
    public ResponseEntity<ResumenMascotaDTO> obtenerResumen(@PathVariable("id") Long id) {
        try {
            ResumenMascotaDTO dto = citaService.obtenerResumenMascota(id);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/citas")
    public ResponseEntity<List<Map<String, Object>>> obtenerCitasEventos() {
        List<Cita> list = citaService.listarCitas();
        List<Map<String, Object>> events = list.stream().map(c -> {
            Map<String, Object> event = new HashMap<>();
            event.put("id", c.getId());
            event.put("title", c.getMascota().getNombre() + " - " + c.getTipoCita().name());
            
            // Format times (ISO 8601 string)
            String startStr = c.getFecha().toString() + "T" + c.getHora().toString();
            event.put("start", startStr);
            
            String endStr = c.getFecha().toString() + "T" + c.getHora().plusMinutes(c.getDuracionMinutos()).toString();
            event.put("end", endStr);
            
            // Color according to state or priority
            String color = "#3b82f6"; // default blue
            if (c.getEstado() != null) {
                color = switch (c.getEstado()) {
                    case PROGRAMADA -> "#f59e0b"; // yellow
                    case CONFIRMADA -> "#10b981"; // green
                    case EN_CURSO -> "#06b6d4"; // cyan
                    case FINALIZADA -> "#3b82f6"; // blue
                    case CANCELADA -> "#6b7280"; // gray
                    case NO_ASISTIO -> "#ef4444"; // red
                    default -> "#6366f1"; // indigo
                };
            }
            event.put("backgroundColor", color);
            event.put("borderColor", color);
            event.put("textColor", "#ffffff");
            
            // Extended properties for the modal detail view
            Map<String, Object> props = new HashMap<>();
            props.put("mascotaNombre", c.getMascota().getNombre());
            props.put("propietarioNombre", c.getPropietario().getNombre() + " " + c.getPropietario().getApellido());
            props.put("propietarioTelefono", c.getPropietario().getTelefono());
            props.put("especie", c.getMascota().getEspecie().name());
            props.put("servicio", c.getTipoCita().name());
            props.put("estado", c.getEstado().name());
            props.put("veterinario", c.getVeterinario());
            props.put("prioridad", c.getPrioridad().name());
            props.put("motivo", c.getMotivoConsulta());
            props.put("observaciones", c.getObservaciones() != null ? c.getObservaciones() : "");
            
            event.put("extendedProps", props);
            return event;
        }).toList();
        
        return ResponseEntity.ok(events);
    }

    @GetMapping("/check-collision")
    public ResponseEntity<Map<String, Object>> checkCollision(
            @RequestParam("fecha") String fechaStr,
            @RequestParam("hora") String horaStr,
            @RequestParam("duracion") int duracion,
            @RequestParam("veterinario") String veterinario,
            @RequestParam(value = "excludeId", required = false) Long excludeId) {
        
        try {
            LocalDate fecha = LocalDate.parse(fechaStr);
            LocalTime hora = LocalTime.parse(horaStr);
            citaService.validarColision(fecha, hora, duracion, veterinario, excludeId);
            return ResponseEntity.ok(Map.of("collision", false));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(Map.of("collision", true, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/horarios-disponibles")
    public ResponseEntity<List<String>> getHorariosDisponibles(
            @RequestParam("fecha") String fechaStr,
            @RequestParam("veterinario") String veterinario) {
        try {
            LocalDate fecha = LocalDate.parse(fechaStr);
            
            // 1. Validar fechas pasadas
            if (fecha.isBefore(LocalDate.now())) {
                return ResponseEntity.ok(List.of());
            }

            // 2. Validar días laborables
            if (!agendaConfig.getDiasLaborables().contains(fecha.getDayOfWeek())) {
                return ResponseEntity.ok(List.of());
            }

            // 3. Validar veterinario activo
            List<Usuario> activeVets = usuarioRepository.findByRolAndEnabledTrue("VETERINARIO");
            boolean isValidVet = activeVets.stream().anyMatch(v -> {
                String nombre = v.getNombre() != null ? v.getNombre() : "";
                String apellido = v.getApellido() != null ? v.getApellido() : "";
                String fullName = (nombre + " " + apellido).trim();
                if (fullName.isEmpty()) {
                    fullName = v.getUsername();
                }
                if (!fullName.toLowerCase().startsWith("dr.") && !fullName.toLowerCase().startsWith("dra.")) {
                    fullName = "Dr. " + fullName;
                }
                return fullName.equalsIgnoreCase(veterinario);
            });
            
            if (!isValidVet) {
                return ResponseEntity.ok(List.of());
            }

            // 4. Calcular los slots dinámicamente según horario laboral
            LocalTime apertura = agendaConfig.getHoraApertura();
            LocalTime cierre = agendaConfig.getHoraCierre();
            int duracion = agendaConfig.getDuracionDefecto();

            List<LocalTime> todosLosHorarios = new ArrayList<>();
            LocalTime current = apertura;
            while (current.plusMinutes(duracion).isBefore(cierre) || current.plusMinutes(duracion).equals(cierre)) {
                todosLosHorarios.add(current);
                current = current.plusMinutes(duracion);
            }

            List<Cita> citasExistentes = citaService.listarPorFecha(fecha);

            List<String> disponibles = todosLosHorarios.stream().filter(time -> {
                LocalTime start = time;
                LocalTime end = time.plusMinutes(duracion);

                // Si es hoy, evitar horarios que ya pasaron
                if (fecha.equals(LocalDate.now()) && start.isBefore(LocalTime.now())) {
                    return false;
                }

                for (Cita c : citasExistentes) {
                    if (c.getEstado() == com.vetexpert.sistema_veterinaria.agenda.entity.EstadoCita.CANCELADA) {
                        continue;
                    }
                    if (c.getVeterinario() != null && c.getVeterinario().equalsIgnoreCase(veterinario)) {
                        LocalTime existingStart = c.getHora();
                        LocalTime existingEnd = c.getHora().plusMinutes(c.getDuracionMinutos());
                        if (start.isBefore(existingEnd) && end.isAfter(existingStart)) {
                            return false;
                        }
                    }
                }
                return true;
            }).map(t -> String.format("%02d:%02d", t.getHour(), t.getMinute())).toList();

            return ResponseEntity.ok(disponibles);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
