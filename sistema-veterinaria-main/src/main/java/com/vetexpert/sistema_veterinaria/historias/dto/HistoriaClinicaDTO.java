package com.vetexpert.sistema_veterinaria.historias.dto;

import com.vetexpert.sistema_veterinaria.historias.model.EstadoConsulta;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

/**
 * DTO para la transferencia y validación de datos de consultas de Historia Clínica.
 */
public class HistoriaClinicaDTO {

    private Long id;
    private String codigoConsulta;

    @NotNull(message = "La fecha de la consulta es obligatoria")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaConsulta;

    @NotBlank(message = "El motivo de la consulta es obligatorio")
    @Size(min = 5, max = 300, message = "El motivo de la consulta debe tener entre 5 y 300 caracteres")
    private String motivoConsulta;

    private String anamnesis;

    @Min(value = 30, message = "La temperatura mínima permitida es 30 grados")
    @Max(value = 45, message = "La temperatura máxima permitida es 45 grados")
    private Double temperatura;

    @NotNull(message = "El peso actual es obligatorio")
    @Positive(message = "El peso actual debe ser mayor que cero")
    private Double pesoActual;

    private Integer frecuenciaCardiaca;
    private Integer frecuenciaRespiratoria;

    @NotBlank(message = "El diagnóstico es obligatorio")
    @Size(min = 5, message = "El diagnóstico debe tener al menos 5 caracteres")
    private String diagnostico;

    @Size(max = 1000, message = "El tratamiento no puede superar los 1000 caracteres")
    private String tratamiento;

    @Size(max = 1000, message = "Los medicamentos no pueden superar los 1000 caracteres")
    private String medicamentos;

    @Size(max = 1000, message = "Las recomendaciones no pueden superar los 1000 caracteres")
    private String recomendaciones;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate proximaVisita;

    @NotNull(message = "El estado de la consulta es obligatorio")
    private EstadoConsulta estadoConsulta;

    @NotBlank(message = "El veterinario es obligatorio")
    private String veterinario;

    @NotNull(message = "La mascota es obligatoria")
    private Long mascotaId;

    // ========== Constructores ==========

    public HistoriaClinicaDTO() {
    }

    public HistoriaClinicaDTO(Long id, String codigoConsulta, LocalDate fechaConsulta, String motivoConsulta,
                              String anamnesis, Double temperatura, Double pesoActual, Integer frecuenciaCardiaca,
                              Integer frecuenciaRespiratoria, String diagnostico, String tratamiento,
                              String medicamentos, String recomendaciones, LocalDate proximaVisita,
                              EstadoConsulta estadoConsulta, String veterinario, Long mascotaId) {
        this.id = id;
        this.codigoConsulta = codigoConsulta;
        this.fechaConsulta = fechaConsulta;
        this.motivoConsulta = motivoConsulta;
        this.anamnesis = anamnesis;
        this.temperatura = temperatura;
        this.pesoActual = pesoActual;
        this.frecuenciaCardiaca = frecuenciaCardiaca;
        this.frecuenciaRespiratoria = frecuenciaRespiratoria;
        this.diagnostico = diagnostico;
        this.tratamiento = tratamiento;
        this.medicamentos = medicamentos;
        this.recomendaciones = recomendaciones;
        this.proximaVisita = proximaVisita;
        this.estadoConsulta = estadoConsulta;
        this.veterinario = veterinario;
        this.mascotaId = mascotaId;
    }

    // ========== Getters y Setters ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigoConsulta() {
        return codigoConsulta;
    }

    public void setCodigoConsulta(String codigoConsulta) {
        this.codigoConsulta = codigoConsulta;
    }

    public LocalDate getFechaConsulta() {
        return fechaConsulta;
    }

    public void setFechaConsulta(LocalDate fechaConsulta) {
        this.fechaConsulta = fechaConsulta;
    }

    public String getMotivoConsulta() {
        return motivoConsulta;
    }

    public void setMotivoConsulta(String motivoConsulta) {
        this.motivoConsulta = motivoConsulta;
    }

    public String getAnamnesis() {
        return anamnesis;
    }

    public void setAnamnesis(String anamnesis) {
        this.anamnesis = anamnesis;
    }

    public Double getTemperatura() {
        return temperatura;
    }

    public void setTemperatura(Double temperatura) {
        this.temperatura = temperatura;
    }

    public Double getPesoActual() {
        return pesoActual;
    }

    public void setPesoActual(Double pesoActual) {
        this.pesoActual = pesoActual;
    }

    public Integer getFrecuenciaCardiaca() {
        return frecuenciaCardiaca;
    }

    public void setFrecuenciaCardiaca(Integer frecuenciaCardiaca) {
        this.frecuenciaCardiaca = frecuenciaCardiaca;
    }

    public Integer getFrecuenciaRespiratoria() {
        return frecuenciaRespiratoria;
    }

    public void setFrecuenciaRespiratoria(Integer frecuenciaRespiratoria) {
        this.frecuenciaRespiratoria = frecuenciaRespiratoria;
    }

    public String getDiagnostico() {
        return diagnostico;
    }

    public void setDiagnostico(String diagnostico) {
        this.diagnostico = diagnostico;
    }

    public String getTratamiento() {
        return tratamiento;
    }

    public void setTratamiento(String tratamiento) {
        this.tratamiento = tratamiento;
    }

    public String getMedicamentos() {
        return medicamentos;
    }

    public void setMedicamentos(String medicamentos) {
        this.medicamentos = medicamentos;
    }

    public String getRecomendaciones() {
        return recomendaciones;
    }

    public void setRecomendaciones(String recomendaciones) {
        this.recomendaciones = recomendaciones;
    }

    public LocalDate getProximaVisita() {
        return proximaVisita;
    }

    public void setProximaVisita(LocalDate proximaVisita) {
        this.proximaVisita = proximaVisita;
    }

    public EstadoConsulta getEstadoConsulta() {
        return estadoConsulta;
    }

    public void setEstadoConsulta(EstadoConsulta estadoConsulta) {
        this.estadoConsulta = estadoConsulta;
    }

    public String getVeterinario() {
        return veterinario;
    }

    public void setVeterinario(String veterinario) {
        this.veterinario = veterinario;
    }

    public Long getMascotaId() {
        return mascotaId;
    }

    public void setMascotaId(Long mascotaId) {
        this.mascotaId = mascotaId;
    }

    @Override
    public String toString() {
        return "HistoriaClinicaDTO{" +
                "id=" + id +
                ", codigoConsulta='" + codigoConsulta + '\'' +
                ", fechaConsulta=" + fechaConsulta +
                ", motivoConsulta='" + motivoConsulta + '\'' +
                ", temperatura=" + temperatura +
                ", pesoActual=" + pesoActual +
                ", estadoConsulta=" + estadoConsulta +
                ", veterinario='" + veterinario + '\'' +
                ", mascotaId=" + mascotaId +
                '}';
    }
}
