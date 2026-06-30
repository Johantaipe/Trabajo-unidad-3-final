package com.vetexpert.sistema_veterinaria.agenda.dto;

import com.vetexpert.sistema_veterinaria.agenda.entity.EstadoCita;
import com.vetexpert.sistema_veterinaria.agenda.entity.PrioridadCita;
import com.vetexpert.sistema_veterinaria.agenda.entity.TipoCita;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * DTO para la transferencia y validación de datos de Citas.
 */
public class CitaDTO {

    private Long id;
    private String codigoCita;

    @NotNull(message = "La mascota es obligatoria")
    private Long mascotaId;

    private Long propietarioId;

    @NotBlank(message = "El veterinario es obligatorio")
    @Size(max = 100, message = "El nombre del veterinario no puede superar los 100 caracteres")
    private String veterinario;

    @NotNull(message = "La fecha es obligatoria")
    @FutureOrPresent(message = "La fecha no puede ser en el pasado")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fecha;

    @NotNull(message = "La hora es obligatoria")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime hora;

    @Min(value = 1, message = "La duración debe ser de al menos 1 minuto")
    private int duracionMinutos;

    @NotNull(message = "El tipo de cita es obligatorio")
    private TipoCita tipoCita;

    @NotBlank(message = "El motivo de la consulta es obligatorio")
    @Size(min = 5, max = 300, message = "El motivo de la consulta debe tener entre 5 y 300 caracteres")
    private String motivoConsulta;

    private EstadoCita estado;

    @NotNull(message = "La prioridad es obligatoria")
    private PrioridadCita prioridad;

    @Size(max = 500, message = "Las observaciones no pueden superar los 500 caracteres")
    private String observaciones;

    private boolean recordatorioProgramado;
    private boolean recordatorioEnviado;
    private boolean confirmacionRecibida;
    private LocalDateTime fechaConfirmacion;

    // ========== Constructores ==========

    public CitaDTO() {
    }

    // ========== Getters y Setters ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigoCita() {
        return codigoCita;
    }

    public void setCodigoCita(String codigoCita) {
        this.codigoCita = codigoCita;
    }

    public Long getMascotaId() {
        return mascotaId;
    }

    public void setMascotaId(Long mascotaId) {
        this.mascotaId = mascotaId;
    }

    public Long getPropietarioId() {
        return propietarioId;
    }

    public void setPropietarioId(Long propietarioId) {
        this.propietarioId = propietarioId;
    }

    public String getVeterinario() {
        return veterinario;
    }

    public void setVeterinario(String veterinario) {
        this.veterinario = veterinario;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public int getDuracionMinutos() {
        return duracionMinutos;
    }

    public void setDuracionMinutos(int duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
    }

    public TipoCita getTipoCita() {
        return tipoCita;
    }

    public void setTipoCita(TipoCita tipoCita) {
        this.tipoCita = tipoCita;
    }

    public String getMotivoConsulta() {
        return motivoConsulta;
    }

    public void setMotivoConsulta(String motivoConsulta) {
        this.motivoConsulta = motivoConsulta;
    }

    public EstadoCita getEstado() {
        return estado;
    }

    public void setEstado(EstadoCita estado) {
        this.estado = estado;
    }

    public PrioridadCita getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(PrioridadCita prioridad) {
        this.prioridad = prioridad;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public boolean isRecordatorioProgramado() {
        return recordatorioProgramado;
    }

    public void setRecordatorioProgramado(boolean recordatorioProgramado) {
        this.recordatorioProgramado = recordatorioProgramado;
    }

    public boolean isRecordatorioEnviado() {
        return recordatorioEnviado;
    }

    public void setRecordatorioEnviado(boolean recordatorioEnviado) {
        this.recordatorioEnviado = recordatorioEnviado;
    }

    public boolean isConfirmacionRecibida() {
        return confirmacionRecibida;
    }

    public void setConfirmacionRecibida(boolean confirmacionRecibida) {
        this.confirmacionRecibida = confirmacionRecibida;
    }

    public LocalDateTime getFechaConfirmacion() {
        return fechaConfirmacion;
    }

    public void setFechaConfirmacion(LocalDateTime fechaConfirmacion) {
        this.fechaConfirmacion = fechaConfirmacion;
    }
}
