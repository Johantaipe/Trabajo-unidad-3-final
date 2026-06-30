package com.vetexpert.sistema_veterinaria.vacunas.service;

import com.vetexpert.sistema_veterinaria.vacunas.entity.VacunaAplicada;
import com.vetexpert.sistema_veterinaria.desparasitaciones.entity.Desparasitacion;
import java.util.List;
import java.util.Map;

/**
 * Servicio encargado de gestionar las alertas automáticas y notificaciones multicanal.
 */
public interface AlertaService {

    /**
     * Tarea periódica para actualizar los estados de vacunas/desparasitaciones
     * y disparar alertas de 30 días, 7 días (críticas) y de vencimiento.
     */
    void procesarAlertasAutomaticas();

    /**
     * Envía una notificación simulada por WhatsApp.
     */
    void enviarNotificacionWhatsApp(String telefono, String mensaje);

    /**
     * Envía una notificación simulada por Correo Electrónico.
     */
    void enviarNotificacionEmail(String email, String asunto, String mensaje);

    /**
     * Obtiene las vacunas próximas a vencer.
     */
    List<VacunaAplicada> obtenerVacunasProximas();

    /**
     * Obtiene las vacunas vencidas.
     */
    List<VacunaAplicada> obtenerVacunasVencidas();

    /**
     * Obtiene las desparasitaciones próximas a vencer.
     */
    List<Desparasitacion> obtenerDesparasitacionesProximas();

    /**
     * Obtiene las desparasitaciones vencidas.
     */
    List<Desparasitacion> obtenerDesparasitacionesVencidas();

    /**
     * Retorna las métricas consolidadas para el Dashboard de Alertas.
     */
    Map<String, Object> obtenerMetricasDashboard();
}
