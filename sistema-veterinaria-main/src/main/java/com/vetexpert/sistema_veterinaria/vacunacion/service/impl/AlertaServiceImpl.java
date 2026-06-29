package com.vetexpert.sistema_veterinaria.vacunacion.service.impl;

import com.vetexpert.sistema_veterinaria.vacunacion.model.EstadoVacuna;
import com.vetexpert.sistema_veterinaria.vacunacion.model.VacunaAplicada;
import com.vetexpert.sistema_veterinaria.vacunacion.model.Desparasitacion;
import com.vetexpert.sistema_veterinaria.vacunacion.repository.VacunaAplicadaRepository;
import com.vetexpert.sistema_veterinaria.vacunacion.repository.DesparasitacionRepository;
import com.vetexpert.sistema_veterinaria.vacunacion.service.AlertaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementación del servicio de alertas.
 * Analiza fechas para actualizar estados y generar notificaciones sobre VacunasAplicadas.
 */
@Service
@Transactional
public class AlertaServiceImpl implements AlertaService {

    private static final Logger log = LoggerFactory.getLogger(AlertaServiceImpl.class);

    private final VacunaAplicadaRepository vacunaAplicadaRepository;
    private final DesparasitacionRepository desparasitacionRepository;

    public AlertaServiceImpl(VacunaAplicadaRepository vacunaAplicadaRepository, 
                             DesparasitacionRepository desparasitacionRepository) {
        this.vacunaAplicadaRepository = vacunaAplicadaRepository;
        this.desparasitacionRepository = desparasitacionRepository;
    }

    @Scheduled(cron = "0 0 1 * * ?")
    @Override
    public void procesarAlertasAutomaticas() {
        log.info("Iniciando proceso diario automático de validación de Vacunas Aplicadas y Desparasitaciones...");
        LocalDate hoy = LocalDate.now();

        // 1. Procesar Vacunas Aplicadas
        List<VacunaAplicada> vacunas = vacunaAplicadaRepository.findAll();
        for (VacunaAplicada vacuna : vacunas) {
            LocalDate proxima = vacuna.getFechaProximaDosis();
            if (proxima == null) continue;

            boolean cambios = false;

            // Lógica de Vencimiento
            if (hoy.isAfter(proxima)) {
                if (vacuna.getEstado() != EstadoVacuna.VENCIDA) {
                    vacuna.setEstado(EstadoVacuna.VENCIDA);
                    cambios = true;
                }
                if (!vacuna.isAlertaVencidaEnviada()) {
                    enviarAlertaVencida(vacuna);
                    vacuna.setAlertaVencidaEnviada(true);
                    cambios = true;
                }
            } else {
                long diasParaVencer = ChronoUnit.DAYS.between(hoy, proxima);

                // Alerta Crítica de 7 días
                if (diasParaVencer <= 7) {
                    if (vacuna.getEstado() != EstadoVacuna.PROXIMA_A_VENCER) {
                        vacuna.setEstado(EstadoVacuna.PROXIMA_A_VENCER);
                        cambios = true;
                    }
                    if (!vacuna.isAlerta7DiasEnviada()) {
                        enviarAlerta7Dias(vacuna);
                        vacuna.setAlerta7DiasEnviada(true);
                        cambios = true;
                    }
                }
                // Alerta de 30 días
                else if (diasParaVencer <= 30) {
                    if (vacuna.getEstado() != EstadoVacuna.PROXIMA_A_VENCER) {
                        vacuna.setEstado(EstadoVacuna.PROXIMA_A_VENCER);
                        cambios = true;
                    }
                    if (!vacuna.isAlerta30DiasEnviada()) {
                        enviarAlerta30Dias(vacuna);
                        vacuna.setAlerta30DiasEnviada(true);
                        cambios = true;
                    }
                }
            }

            if (cambios) {
                vacunaAplicadaRepository.save(vacuna);
            }
        }

        // 2. Procesar Desparasitaciones
        List<Desparasitacion> desparasitaciones = desparasitacionRepository.findAll();
        for (Desparasitacion desp : desparasitaciones) {
            LocalDate proxima = desp.getFechaProximaAplicacion();
            if (proxima == null) continue;

            boolean cambios = false;

            if (hoy.isAfter(proxima)) {
                if (desp.getEstado() != EstadoVacuna.VENCIDA) {
                    desp.setEstado(EstadoVacuna.VENCIDA);
                    cambios = true;
                }
            } else {
                long diasParaVencer = ChronoUnit.DAYS.between(hoy, proxima);
                if (diasParaVencer <= 30) {
                    if (desp.getEstado() != EstadoVacuna.PROXIMA_A_VENCER) {
                        desp.setEstado(EstadoVacuna.PROXIMA_A_VENCER);
                        cambios = true;
                    }
                }
            }

            if (cambios) {
                desparasitacionRepository.save(desp);
            }
        }

        log.info("Proceso diario automático finalizado.");
    }

    private void enviarAlerta30Dias(VacunaAplicada v) {
        String propietario = v.getMascota().getPropietario().getNombre() + " " + v.getMascota().getPropietario().getApellido();
        String mascota = v.getMascota().getNombre();
        String mensaje = String.format("Estimado(a) %s, le recordamos que la vacuna %s de su mascota %s vencerá en 30 días (%s). Agende su cita en VET EXPERTS.",
                propietario, v.getVacuna().getNombre(), mascota, v.getFechaProximaDosis());

        enviarNotificacionWhatsApp(v.getMascota().getPropietario().getTelefono(), mensaje);
        enviarNotificacionEmail(v.getMascota().getPropietario().getCorreo(), "Recordatorio de Vacunación: 30 días", mensaje);
    }

    private void enviarAlerta7Dias(VacunaAplicada v) {
        String propietario = v.getMascota().getPropietario().getNombre() + " " + v.getMascota().getPropietario().getApellido();
        String mascota = v.getMascota().getNombre();
        String mensaje = String.format("ALERTA CRÍTICA: Estimado(a) %s, la vacuna %s de su mascota %s vencerá en 7 días (%s). Evite dejar desprotegido a su compañero.",
                propietario, v.getVacuna().getNombre(), mascota, v.getFechaProximaDosis());

        enviarNotificacionWhatsApp(v.getMascota().getPropietario().getTelefono(), mensaje);
        enviarNotificacionEmail(v.getMascota().getPropietario().getCorreo(), "ALERTA CRÍTICA: Próximo Vencimiento de Vacuna", mensaje);
    }

    private void enviarAlertaVencida(VacunaAplicada v) {
        String propietario = v.getMascota().getPropietario().getNombre() + " " + v.getMascota().getPropietario().getApellido();
        String mascota = v.getMascota().getNombre();
        String mensaje = String.format("ATENCIÓN: Estimado(a) %s, la vacuna %s de su mascota %s VENCIÓ el %s. Es urgente aplicar el refuerzo médico.",
                propietario, v.getVacuna().getNombre(), mascota, v.getFechaProximaDosis());

        enviarNotificacionWhatsApp(v.getMascota().getPropietario().getTelefono(), mensaje);
        enviarNotificacionEmail(v.getMascota().getPropietario().getCorreo(), "URGENTE: Vacuna Vencida", mensaje);
    }

    @Override
    public void enviarNotificacionWhatsApp(String telefono, String mensaje) {
        log.info("[WHATSAPP SIMULATOR] Enviando mensaje a {}: {}", telefono, mensaje);
    }

    @Override
    public void enviarNotificacionEmail(String email, String asunto, String mensaje) {
        log.info("[EMAIL SIMULATOR] Enviando correo a <{}> Asunto: [{}] - Contenido: {}", email, asunto, mensaje);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VacunaAplicada> obtenerVacunasProximas() {
        return vacunaAplicadaRepository.buscarPorEstado(EstadoVacuna.PROXIMA_A_VENCER);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VacunaAplicada> obtenerVacunasVencidas() {
        return vacunaAplicadaRepository.buscarPorEstado(EstadoVacuna.VENCIDA);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Desparasitacion> obtenerDesparasitacionesProximas() {
        return desparasitacionRepository.buscarPorEstado(EstadoVacuna.PROXIMA_A_VENCER);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Desparasitacion> obtenerDesparasitacionesVencidas() {
        return desparasitacionRepository.buscarPorEstado(EstadoVacuna.VENCIDA);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerMetricasDashboard() {
        Map<String, Object> metricas = new HashMap<>();
        metricas.put("totalVacunasAplicadas", vacunaAplicadaRepository.countByEstado(EstadoVacuna.APLICADA));
        metricas.put("vacunasPendientes", vacunaAplicadaRepository.countByEstado(EstadoVacuna.PROGRAMADA));
        metricas.put("vacunasVencidas", vacunaAplicadaRepository.countByEstado(EstadoVacuna.VENCIDA));
        metricas.put("desparasitacionesActivas", desparasitacionRepository.countByEstado(EstadoVacuna.APLICADA));

        long proximasVacunas = vacunaAplicadaRepository.countByEstado(EstadoVacuna.PROXIMA_A_VENCER);
        long proximasDesp = desparasitacionRepository.countByEstado(EstadoVacuna.PROXIMA_A_VENCER);
        metricas.put("proximasAlertas", proximasVacunas + proximasDesp);

        return metricas;
    }
}
