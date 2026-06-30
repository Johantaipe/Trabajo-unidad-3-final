package com.vetexpert.sistema_veterinaria.agenda.entity;

/**
 * Enum que representa los distintos tipos de cita en la agenda veterinaria.
 */
public enum TipoCita {
    CONSULTA_GENERAL,
    VACUNACION,
    DESPARASITACION,
    CONTROL,
    EMERGENCIA,
    CIRUGIA,
    PELUQUERIA,
    BAÑO,
    HIGIENE_DENTAL,
    OTRO;

    /**
     * Retorna la duración por defecto en minutos para cada tipo de cita.
     */
    public int getDuracionPorDefecto() {
        return switch (this) {
            case VACUNACION -> 15;
            case DESPARASITACION -> 15;
            case CONTROL -> 20;
            case CONSULTA_GENERAL -> 30;
            case BAÑO -> 30;
            case PELUQUERIA -> 60;
            case CIRUGIA -> 60; // Configurable
            case EMERGENCIA -> 30; // Atención inmediata
            case HIGIENE_DENTAL -> 30;
            case OTRO -> 30;
        };
    }
}
