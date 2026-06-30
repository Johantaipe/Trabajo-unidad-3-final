package com.vetexpert.sistema_veterinaria.historias.entity;

import com.vetexpert.sistema_veterinaria.mascotas.entity.Mascota;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa una consulta en el historial clínico de una mascota.
 * Módulo: historias
 */
@Entity
@Table(name = "historias_clinicas")
public class HistoriaClinica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_consulta", nullable = false, unique = true, length = 12)
    private String codigoConsulta;

    @NotNull(message = "La fecha de la consulta es obligatoria")
    @Column(name = "fecha_consulta", nullable = false)
    private LocalDate fechaConsulta;

    @NotBlank(message = "El motivo de la consulta es obligatorio")
    @Size(min = 5, max = 300, message = "El motivo de la consulta debe tener entre 5 y 300 caracteres")
    @Column(name = "motivo_consulta", nullable = false, length = 300)
    private String motivoConsulta;

    @Column(columnDefinition = "TEXT")
    private String anamnesis;

    @Min(value = 30, message = "La temperatura mínima permitida es 30 grados")
    @Max(value = 45, message = "La temperatura máxima permitida es 45 grados")
    private Double temperatura;

    @NotNull(message = "El peso actual es obligatorio")
    @Positive(message = "El peso actual debe ser mayor que cero")
    @Column(name = "peso_actual", nullable = false)
    private Double pesoActual;

    @Column(name = "frecuencia_cardiaca")
    private Integer frecuenciaCardiaca;

    @Column(name = "frecuencia_respiratoria")
    private Integer frecuenciaRespiratoria;

    @NotBlank(message = "El diagnóstico es obligatorio")
    @Size(min = 5, message = "El diagnóstico debe tener al menos 5 caracteres")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String diagnostico;

    @Size(max = 1000, message = "El tratamiento no puede superar los 1000 caracteres")
    @Column(columnDefinition = "TEXT")
    private String tratamiento;

    @Size(max = 1000, message = "Los medicamentos no pueden superar los 1000 caracteres")
    @Column(columnDefinition = "TEXT")
    private String medicamentos;

    @Size(max = 1000, message = "Las recomendaciones no pueden superar los 1000 caracteres")
    @Column(columnDefinition = "TEXT")
    private String recomendaciones;

    @Column(name = "proxima_visita")
    private LocalDate proximaVisita;

    @NotNull(message = "El estado de la consulta es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_consulta", nullable = false, length = 20)
    private EstadoConsulta estadoConsulta;

    @NotBlank(message = "El nombre del veterinario es obligatorio")
    @Column(nullable = false, length = 100)
    private String veterinario;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @NotNull(message = "La mascota es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mascota_id", nullable = false)
    private Mascota mascota;

    // ========== Ciclo de Vida JPA (Auditoría) ==========

    @PrePersist
    protected void onCreate() {
        this.fechaRegistro = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }

    // ========== Constructores ==========

    public HistoriaClinica() {
    }

    public HistoriaClinica(String codigoConsulta, LocalDate fechaConsulta, String motivoConsulta, String anamnesis,
                           Double temperatura, Double pesoActual, Integer frecuenciaCardiaca, Integer frecuenciaRespiratoria,
                           String diagnostico, String tratamiento, String medicamentos, String recomendaciones,
                           LocalDate proximaVisita, EstadoConsulta estadoConsulta, String veterinario, Mascota mascota) {
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
        this.mascota = mascota;
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

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public Mascota getMascota() {
        return mascota;
    }

    public void setMascota(Mascota mascota) {
        this.mascota = mascota;
    }

    @Override
    public String toString() {
        return "HistoriaClinica{" +
                "id=" + id +
                ", codigoConsulta='" + codigoConsulta + '\'' +
                ", fechaConsulta=" + fechaConsulta +
                ", motivoConsulta='" + motivoConsulta + '\'' +
                ", temperatura=" + temperatura +
                ", pesoActual=" + pesoActual +
                ", estadoConsulta=" + estadoConsulta +
                ", veterinario='" + veterinario + '\'' +
                '}';
    }
}
