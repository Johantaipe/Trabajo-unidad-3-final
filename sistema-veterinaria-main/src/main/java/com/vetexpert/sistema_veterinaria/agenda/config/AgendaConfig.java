package com.vetexpert.sistema_veterinaria.agenda.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Configuración del módulo de Agenda.
 * Lee valores configurables desde application.properties con valores de contingencia por defecto.
 */
@Configuration
public class AgendaConfig {

    @Value("${agenda.apertura:08:00}")
    private String horaAperturaStr;

    @Value("${agenda.cierre:20:00}")
    private String horaCierreStr;

    @Value("${agenda.duracion-defecto:30}")
    private int duracionDefecto;

    @Value("${agenda.dias-laborables:MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY,SATURDAY}")
    private String diasLaborablesStr;

    public LocalTime getHoraApertura() {
        try {
            return LocalTime.parse(horaAperturaStr);
        } catch (Exception e) {
            return LocalTime.of(8, 0);
        }
    }

    public LocalTime getHoraCierre() {
        try {
            return LocalTime.parse(horaCierreStr);
        } catch (Exception e) {
            return LocalTime.of(20, 0);
        }
    }

    public int getDuracionDefecto() {
        return duracionDefecto;
    }

    public List<DayOfWeek> getDiasLaborables() {
        try {
            return Arrays.stream(diasLaborablesStr.split(","))
                    .map(String::trim)
                    .map(String::toUpperCase)
                    .map(DayOfWeek::valueOf)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return List.of(
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY
            );
        }
    }
}
