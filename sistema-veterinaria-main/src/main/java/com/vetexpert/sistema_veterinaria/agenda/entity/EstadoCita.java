package com.vetexpert.sistema_veterinaria.agenda.entity;

/**
 * Enum que representa los estados por los que transiciona una cita médica.
 */
public enum EstadoCita {
    PROGRAMADA,
    CONFIRMADA,
    EN_CURSO,
    EN_ESPERA,
    ATENDIDA,
    FINALIZADA,
    REPROGRAMADA,
    CANCELADA,
    NO_ASISTIO
}
