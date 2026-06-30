package com.vetexpert.sistema_veterinaria.vacunas.entity;

import com.vetexpert.sistema_veterinaria.mascotas.entity.Mascota;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa una vacuna aplicada o programada para una mascota.
 * Módulo: vacunacion
 */
@Entity
@Table(name = "vacunas_aplicadas")
public class VacunaAplicada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_vacuna_aplicada", nullable = false, unique = true, length = 15)
    private String codigoVacunaAplicada;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mascota_id", nullable = false)
    private Mascota mascota;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vacuna_id", nullable = false)
    private Vacuna vacuna; // Vínculo al catálogo de vacunas

    @Column(name = "fecha_aplicacion", nullable = false)
    private LocalDate fechaAplicacion;

    @Column(name = "fecha_proxima_dosis", nullable = false)
    private LocalDate fechaProximaDosis;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoVacuna estado;

    @Column(length = 50)
    private String lote;

    @Column(length = 100)
    private String laboratorio; // Laboratorio registrado al aplicar

    @Column(nullable = false, length = 100)
    private String veterinario;

    @Column(length = 500)
    private String observaciones;

    @Column(name = "precio_cobrado", nullable = false)
    private Double precioCobrado;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @Column(name = "alerta_30_dias_enviada", nullable = false)
    private boolean alerta30DiasEnviada = false;

    @Column(name = "alerta_7_dias_enviada", nullable = false)
    private boolean alerta7DiasEnviada = false;

    @Column(name = "alerta_vencida_enviada", nullable = false)
    private boolean alertaVencidaEnviada = false;

    // ========== Ciclo de Vida JPA (Auditoría) ==========

    @PrePersist
    protected void onCreate() {
        this.fechaRegistro = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
        this.alerta30DiasEnviada = false;
        this.alerta7DiasEnviada = false;
        this.alertaVencidaEnviada = false;
    }

    @PreUpdate
    protected void onUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }

    // ========== Constructores ==========

    public VacunaAplicada() {
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

    public Mascota getMascota() {
        return mascota;
    }

    public void setMascota(Mascota mascota) {
        this.mascota = mascota;
    }

    public Vacuna getVacuna() {
        return vacuna;
    }

    public void setVacuna(Vacuna vacuna) {
        this.vacuna = vacuna;
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

    public boolean isAlerta30DiasEnviada() {
        return alerta30DiasEnviada;
    }

    public void setAlerta30DiasEnviada(boolean alerta30DiasEnviada) {
        this.alerta30DiasEnviada = alerta30DiasEnviada;
    }

    public boolean isAlerta7DiasEnviada() {
        return alerta7DiasEnviada;
    }

    public void setAlerta7DiasEnviada(boolean alerta7DiasEnviada) {
        this.alerta7DiasEnviada = alerta7DiasEnviada;
    }

    public boolean isAlertaVencidaEnviada() {
        return alertaVencidaEnviada;
    }

    public void setAlertaVencidaEnviada(boolean alertaVencidaEnviada) {
        this.alertaVencidaEnviada = alertaVencidaEnviada;
    }
}
