package com.vetexpert.sistema_veterinaria.vacunas.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuración del módulo de vacunación. Enciende el planificador de Spring para alertas automáticas.
 */
@Configuration
@EnableScheduling
public class VacunacionConfig {
}
