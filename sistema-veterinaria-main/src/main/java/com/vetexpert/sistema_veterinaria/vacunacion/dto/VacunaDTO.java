package com.vetexpert.sistema_veterinaria.vacunacion.dto;

import com.vetexpert.sistema_veterinaria.vacunacion.model.EstadoVacuna;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

/**
 * DTO para la transferencia y validación de datos de Vacunas Aplicadas.
 */
public class VacunaDTO {

    private Long id;
    private String codigoVacunaAplicada;

    @NotNull(message = "La mascota es obligatoria")
    private Long mascotaId;

    @NotNull(message = "La vacuna del catálogo es obligatoria")
    private Long vacunaId;

    @NotNull(message = "La fecha de aplicación es obligatoria")
    @PastOrPresent(message = "La fecha de aplicación no puede ser una fecha futura")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaAplicacion;

    @NotNull(message = "La fecha de próxima dosis es obligatoria")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaProximaDosis;

    private EstadoVacuna estado;

    @Size(max = 50, message = "El lote no puede superar los 50 caracteres")
    private String lote;

    @Size(max = 100, message = "El laboratorio no puede superar los 100 caracteres")
    private String laboratorio;

    @NotBlank(message = "El veterinario es obligatorio")
    @Size(max = 100, message = "El nombre del veterinario no puede superar los 100 caracteres")
    private String veterinario;

    @Size(max = 500, message = "Las observaciones no pueden superar los 500 caracteres")
    private String observaciones;

    private Double precioCobrado;

    // ========== Constructores ==========

    public VacunaDTO() {
    }

    // ========== Getters y Setters ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigoVacunaAplicada() {
        return codigoVacunaAplicada;
    }

    public void setCodigoVacunaAplicada(String codigoVacunaAplicada) {
        this.codigoVacunaAplicada = codigoVacunaAplicada;
    }

    public Long getMascotaId() {
        return mascotaId;
    }

    public void setMascotaId(Long mascotaId) {
        this.mascotaId = mascotaId;
    }

    public Long getVacunaId() {
        return vacunaId;
    }

    public void setVacunaId(Long vacunaId) {
        this.vacunaId = vacunaId;
    }

    public LocalDate getFechaAplicacion() {
        return fechaAplicacion;
    }

    public void setFechaAplicacion(LocalDate fechaAplicacion) {
        this.fechaAplicacion = fechaAplicacion;
    }

    public LocalDate getFechaProximaDosis() {
        return fechaProximaDosis;
    }

    public void setFechaProximaDosis(LocalDate fechaProximaDosis) {
        this.fechaProximaDosis = fechaProximaDosis;
    }

    public EstadoVacuna getEstado() {
        return estado;
    }

    public void setEstado(EstadoVacuna estado) {
        this.estado = estado;
    }

    public String getLote() {
        return lote;
    }

    public void setLote(String lote) {
        this.lote = lote;
    }

    public String getLaboratorio() {
        return laboratorio;
    }

    public void setLaboratorio(String laboratorio) {
        this.laboratorio = laboratorio;
    }

    public String getVeterinario() {
        return veterinario;
    }

    public void setVeterinario(String veterinario) {
        this.veterinario = veterinario;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Double getPrecioCobrado() {
        return precioCobrado;
    }

    public void setPrecioCobrado(Double precioCobrado) {
        this.precioCobrado = precioCobrado;
    }
}
