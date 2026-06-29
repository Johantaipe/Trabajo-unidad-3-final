package com.vetexpert.sistema_veterinaria.publico.controller;

import com.vetexpert.sistema_veterinaria.auth.model.Usuario;
import com.vetexpert.sistema_veterinaria.auth.repository.UsuarioRepository;
import com.vetexpert.sistema_veterinaria.propietarios.model.Propietario;
import com.vetexpert.sistema_veterinaria.propietarios.repository.PropietarioRepository;
import com.vetexpert.sistema_veterinaria.mascotas.model.Mascota;
import com.vetexpert.sistema_veterinaria.mascotas.repository.MascotaRepository;
import com.vetexpert.sistema_veterinaria.agenda.model.Cita;
import com.vetexpert.sistema_veterinaria.agenda.model.EstadoCita;
import com.vetexpert.sistema_veterinaria.agenda.model.TipoCita;
import com.vetexpert.sistema_veterinaria.agenda.model.PrioridadCita;
import com.vetexpert.sistema_veterinaria.agenda.dto.CitaDTO;
import com.vetexpert.sistema_veterinaria.agenda.repository.CitaRepository;
import com.vetexpert.sistema_veterinaria.agenda.service.CitaService;
import com.vetexpert.sistema_veterinaria.vacunacion.model.VacunaAplicada;
import com.vetexpert.sistema_veterinaria.vacunacion.model.EstadoVacuna;
import com.vetexpert.sistema_veterinaria.vacunacion.repository.VacunaAplicadaRepository;
import com.vetexpert.sistema_veterinaria.caja.model.Venta;
import com.vetexpert.sistema_veterinaria.caja.model.EstadoPago;
import com.vetexpert.sistema_veterinaria.caja.repository.VentaRepository;
import com.vetexpert.sistema_veterinaria.caja.service.VentaService;
import com.vetexpert.sistema_veterinaria.historias.model.HistoriaClinica;
import com.vetexpert.sistema_veterinaria.historias.repository.HistoriaClinicaRepository;
import com.vetexpert.sistema_veterinaria.auth.service.EmailService;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Controller
@RequestMapping("/portal-cliente")
public class PortalClienteController {

    private final UsuarioRepository usuarioRepository;
    private final PropietarioRepository propietarioRepository;
    private final MascotaRepository mascotaRepository;
    private final CitaRepository citaRepository;
    private final CitaService citaService;
    private final VacunaAplicadaRepository vacunaAplicadaRepository;
    private final VentaRepository ventaRepository;
    private final VentaService ventaService;
    private final HistoriaClinicaRepository historiaClinicaRepository;
    private final EmailService emailService;

    private static final String UPLOAD_DIR = "src/main/resources/static/uploads/";

    public PortalClienteController(UsuarioRepository usuarioRepository,
                                   PropietarioRepository propietarioRepository,
                                   MascotaRepository mascotaRepository,
                                   CitaRepository citaRepository,
                                   CitaService citaService,
                                   VacunaAplicadaRepository vacunaAplicadaRepository,
                                   VentaRepository ventaRepository,
                                   VentaService ventaService,
                                   HistoriaClinicaRepository historiaClinicaRepository,
                                   EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.propietarioRepository = propietarioRepository;
        this.mascotaRepository = mascotaRepository;
        this.citaRepository = citaRepository;
        this.citaService = citaService;
        this.vacunaAplicadaRepository = vacunaAplicadaRepository;
        this.ventaRepository = ventaRepository;
        this.ventaService = ventaService;
        this.historiaClinicaRepository = historiaClinicaRepository;
        this.emailService = emailService;
    }

    @GetMapping("/cargando")
    public String cargando(HttpSession session) {
        if (session.getAttribute("usuario") == null || !"CLIENTE".equals(session.getAttribute("rol"))) {
            return "redirect:/login-cliente";
        }
        return "auth/cargando";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuario");
        if (usuarioLogueado == null || !"CLIENTE".equals(session.getAttribute("rol"))) {
            return "redirect:/login-cliente";
        }

        // Refrescar el usuario desde la base de datos
        Usuario usuario = usuarioRepository.findById(usuarioLogueado.getId()).orElse(usuarioLogueado);
        session.setAttribute("usuario", usuario);

        Propietario propietario = propietarioRepository.findByCorreo(usuario.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el propietario para el usuario: " + usuario.getUsername()));

        List<Mascota> mascotas = mascotaRepository.buscarPorPropietario(propietario.getId());
        List<Cita> citas = citaRepository.findByPropietarioIdOrderByFechaDesc(propietario.getId());
        List<Venta> ventas = ventaRepository.findByPropietarioIdOrderByFechaVentaDesc(propietario.getId());

        // Recopilar vacunas e historias para todas las mascotas del propietario
        List<VacunaAplicada> vacunas = new ArrayList<>();
        List<HistoriaClinica> historias = new ArrayList<>();
        for (Mascota m : mascotas) {
            vacunas.addAll(vacunaAplicadaRepository.buscarPorMascotaId(m.getId()));
            historias.addAll(historiaClinicaRepository.buscarPorMascota(m.getId()));
        }

        // Ordenar
        vacunas.sort(Comparator.comparing(VacunaAplicada::getFechaAplicacion).reversed());
        historias.sort(Comparator.comparing(HistoriaClinica::getFechaConsulta).reversed());

        // Métricas
        long countMascotas = mascotas.size();
        
        // Próxima cita
        Cita proximaCita = citas.stream()
                .filter(c -> (c.getEstado() == EstadoCita.PROGRAMADA || c.getEstado() == EstadoCita.CONFIRMADA || c.getEstado() == EstadoCita.REPROGRAMADA)
                        && !c.getFecha().isBefore(LocalDate.now()))
                .min(Comparator.comparing(Cita::getFecha).thenComparing(Cita::getHora))
                .orElse(null);

        // Próxima vacuna
        VacunaAplicada proximaVacuna = vacunas.stream()
                .filter(va -> (va.getEstado() == EstadoVacuna.PROXIMA_A_VENCER || va.getEstado() == EstadoVacuna.PROGRAMADA)
                        && !va.getFechaProximaDosis().isBefore(LocalDate.now()))
                .min(Comparator.comparing(VacunaAplicada::getFechaProximaDosis))
                .orElse(null);

        // Pagos pendientes
        long countPagosPendientes = ventas.stream().filter(v -> v.getEstadoPago() == EstadoPago.PENDIENTE).count();
        double totalPagosPendientes = ventas.stream().filter(v -> v.getEstadoPago() == EstadoPago.PENDIENTE)
                .mapToDouble(Venta::getTotal).sum();

        // Comprobantes emitidos
        long countComprobantes = ventas.stream().filter(v -> v.getEstadoPago() == EstadoPago.PAGADO).count();

        // Notificaciones dinámicas
        List<Map<String, Object>> notificaciones = buildNotificaciones(propietario, mascotas, citas, vacunas, ventas);

        // Enviar al modelo
        model.addAttribute("usuario", usuario);
        model.addAttribute("propietario", propietario);
        model.addAttribute("mascotas", mascotas);
        model.addAttribute("citas", citas);
        model.addAttribute("vacunas", vacunas);
        model.addAttribute("pagos", ventas);
        model.addAttribute("comprobantes", ventas);
        model.addAttribute("historias", historias);
        
        model.addAttribute("countMascotas", countMascotas);
        model.addAttribute("proximaCita", proximaCita);
        model.addAttribute("proximaVacuna", proximaVacuna);
        if (proximaVacuna != null) {
            long diasRestantes = ChronoUnit.DAYS.between(LocalDate.now(), proximaVacuna.getFechaProximaDosis());
            model.addAttribute("diasVacunaRestantes", diasRestantes);
        }
        model.addAttribute("countPagosPendientes", countPagosPendientes);
        model.addAttribute("totalPagosPendientes", totalPagosPendientes);
        model.addAttribute("countComprobantes", countComprobantes);
        model.addAttribute("notificaciones", notificaciones);
        model.addAttribute("activePage", "portal-dashboard");

        // Datos para Reserva
        model.addAttribute("tiposCita", TipoCita.values());
        model.addAttribute("veterinarios", List.of("Dr. Carlos T.", "Dra. Lucía M.", "Dr. Julio R."));

        return "portal/dashboard";
    }

    @PostMapping("/reservar")
    public String procesarReserva(@RequestParam("mascotaId") Long mascotaId,
                                  @RequestParam("tipoCita") String tipoCitaStr,
                                  @RequestParam("fecha") String fechaStr,
                                  @RequestParam("hora") String horaStr,
                                  @RequestParam("veterinario") String veterinario,
                                  @RequestParam("motivoConsulta") String motivoConsulta,
                                  @RequestParam(value = "observaciones", required = false) String observaciones,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuario");
        if (usuarioLogueado == null || !"CLIENTE".equals(session.getAttribute("rol"))) {
            return "redirect:/login-cliente";
        }

        try {
            CitaDTO dto = new CitaDTO();
            dto.setMascotaId(mascotaId);
            dto.setTipoCita(TipoCita.valueOf(tipoCitaStr));
            dto.setFecha(LocalDate.parse(fechaStr));
            dto.setHora(LocalTime.parse(horaStr));
            dto.setVeterinario(veterinario);
            dto.setMotivoConsulta(motivoConsulta);
            dto.setObservaciones(observaciones);
            dto.setEstado(EstadoCita.PROGRAMADA);
            dto.setPrioridad(PrioridadCita.NORMAL);
            dto.setDuracionMinutos(dto.getTipoCita().getDuracionPorDefecto());

            citaService.registrarCita(dto);
            redirectAttributes.addFlashAttribute("mensajeExito", "¡Cita reservada con éxito!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al reservar la cita: " + e.getMessage());
        }

        return "redirect:/portal-cliente/dashboard#sec-citas";
    }

    @PostMapping("/perfil/actualizar")
    public String actualizarPerfil(@RequestParam("nombre") String nombre,
                                   @RequestParam("apellido") String apellido,
                                   @RequestParam("telefono") String telefono,
                                   @RequestParam(value = "password", required = false) String password,
                                   @RequestParam(value = "fotoFile", required = false) MultipartFile file,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuario");
        if (usuarioLogueado == null || !"CLIENTE".equals(session.getAttribute("rol"))) {
            return "redirect:/login-cliente";
        }

        try {
            Usuario usuario = usuarioRepository.findById(usuarioLogueado.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

            usuario.setNombre(nombre.trim());
            usuario.setApellido(apellido.trim());
            usuario.setTelefono(telefono.trim());

            if (password != null && !password.trim().isEmpty()) {
                if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&._\\-#/])[A-Za-z\\d@$!%*?&._\\-#/]{8,}$")) {
                    throw new IllegalArgumentException("La contraseña debe tener mínimo 8 caracteres, una mayúscula, una minúscula, un número y un carácter especial.");
                }
                String hashedPassword = org.mindrot.jbcrypt.BCrypt.hashpw(password, org.mindrot.jbcrypt.BCrypt.gensalt());
                usuario.setPassword(hashedPassword);
            }

            if (file != null && !file.isEmpty()) {
                String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                Path uploadPath = Paths.get(UPLOAD_DIR + "perfiles/");
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(file.getInputStream(), filePath);
                usuario.setFotoUrl("/uploads/perfiles/" + fileName);
            }

            usuarioRepository.save(usuario);

            // Actualizar Propietario
            Propietario propietario = propietarioRepository.findByCorreo(usuario.getUsername()).orElse(null);
            if (propietario != null) {
                propietario.setNombre(usuario.getNombre());
                propietario.setApellido(usuario.getApellido());
                propietario.setTelefono(usuario.getTelefono());
                propietarioRepository.save(propietario);
            }

            session.setAttribute("usuario", usuario);
            redirectAttributes.addFlashAttribute("mensajeExito", "Perfil actualizado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al actualizar perfil: " + e.getMessage());
        }

        return "redirect:/portal-cliente/dashboard#sec-perfil";
    }

    @PostMapping("/comprobantes/enviar-correo/{id}")
    @ResponseBody
    public Map<String, Object> enviarCorreoComprobante(@PathVariable("id") Long id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuario");
        if (usuarioLogueado == null || !"CLIENTE".equals(session.getAttribute("rol"))) {
            response.put("success", false);
            response.put("message", "Acceso denegado.");
            return response;
        }

        try {
            Venta venta = ventaService.obtenerVentaPorId(id)
                    .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada con ID: " + id));

            // Forzar generación del simulated PDF
            ventaService.enviarComprobantePorCorreo(venta);

            // Intentar envío real si el SMTP está configurado
            try {
                String codigo = venta.getCodigoVenta();
                String asunto = "Tu Comprobante Electrónico " + codigo + " - VET Experts";
                String bodyHtml = "<div style='font-family: Arial, sans-serif; padding: 20px; border: 1px solid #ddd; max-width: 600px;'>" +
                        "<h2>VET EXPERTS - Comprobante Electrónico</h2>" +
                        "<p>Hola " + venta.getPropietario().getNombre() + ",</p>" +
                        "<p>Adjuntamos el detalle de tu comprobante <strong>" + codigo + "</strong> emitido el " + venta.getFechaVenta().toLocalDate() + ":</p>" +
                        "<ul>" +
                        "<li><strong>Total:</strong> S/ " + String.format("%.2f", venta.getTotal()) + "</li>" +
                        "<li><strong>Método de pago:</strong> " + (venta.getMetodoPago() != null ? venta.getMetodoPago() : "Efectivo") + "</li>" +
                        "</ul>" +
                        "<p>Puedes descargarlo directamente desde tu portal de cliente.</p>" +
                        "<hr/><p style='font-size: 12px; color: #888;'>Vet Experts - Huancayo, Perú</p></div>";
                
                emailService.enviarHtml(venta.getPropietario().getCorreo(), asunto, bodyHtml);
                response.put("success", true);
                response.put("message", "✓ El comprobante ha sido enviado a tu correo " + venta.getPropietario().getCorreo() + ".");
            } catch (Exception ex) {
                // Si falla por falta de SMTP real, el PDF simulado igual se escribió
                response.put("success", true);
                response.put("message", "✓ Comprobante generado en servidor. (Simulación de envío de correo exitosa).");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al enviar comprobante: " + e.getMessage());
        }
        return response;
    }

    private List<Map<String, Object>> buildNotificaciones(Propietario propietario,
                                                          List<Mascota> mascotas,
                                                          List<Cita> citas,
                                                          List<VacunaAplicada> vacunas,
                                                          List<Venta> ventas) {
        List<Map<String, Object>> list = new ArrayList<>();
        LocalDate hoy = LocalDate.now();

        // 1. Bienvenida
        Map<String, Object> welcome = new HashMap<>();
        welcome.put("icono", "bi-stars text-primary");
        welcome.put("mensaje", "🎉 ¡Bienvenido a tu nuevo portal de cliente Vet Experts!");
        welcome.put("fecha", "Hace un momento");
        list.add(welcome);

        // 2. Pagos Pendientes
        for (Venta v : ventas) {
            if (v.getEstadoPago() == EstadoPago.PENDIENTE) {
                Map<String, Object> notif = new HashMap<>();
                notif.put("icono", "bi-credit-card-2-back-fill text-danger");
                notif.put("mensaje", "💳 Pago pendiente de S/ " + String.format("%.2f", v.getTotal()) + " (Código: " + v.getCodigoVenta() + ")");
                notif.put("fecha", "Urgente");
                list.add(notif);
            }
        }

        // 3. Citas de hoy o de los próximos 2 días
        for (Cita c : citas) {
            if (c.getEstado() == EstadoCita.PROGRAMADA || c.getEstado() == EstadoCita.CONFIRMADA || c.getEstado() == EstadoCita.REPROGRAMADA) {
                long dias = ChronoUnit.DAYS.between(hoy, c.getFecha());
                if (dias >= 0 && dias <= 2) {
                    String desc = (dias == 0) ? "hoy" : ((dias == 1) ? "mañana" : "en 2 días");
                    Map<String, Object> notif = new HashMap<>();
                    notif.put("icono", "bi-calendar-check-fill text-warning");
                    notif.put("mensaje", "📅 Recordatorio: Cita de " + c.getMascota().getNombre() + " con el " + c.getVeterinario() + " es " + desc + " a las " + c.getHora());
                    notif.put("fecha", "Faltan " + (dias == 0 ? "horas" : dias + " día(s)"));
                    list.add(notif);
                }
            }
        }

        // 4. Vacunas próximas en los siguientes 7 días
        for (VacunaAplicada va : vacunas) {
            if (va.getEstado() == EstadoVacuna.PROXIMA_A_VENCER || va.getEstado() == EstadoVacuna.PROGRAMADA) {
                long dias = ChronoUnit.DAYS.between(hoy, va.getFechaProximaDosis());
                if (dias >= 0 && dias <= 7) {
                    Map<String, Object> notif = new HashMap<>();
                    notif.put("icono", "bi-virus text-warning");
                    notif.put("mensaje", "💉 Recordatorio: Vacuna " + va.getVacuna().getNombre() + " para " + va.getMascota().getNombre() + " programada para el " + va.getFechaProximaDosis());
                    notif.put("fecha", "En " + dias + " días");
                    list.add(notif);
                }
            }
        }

        return list;
    }
}
