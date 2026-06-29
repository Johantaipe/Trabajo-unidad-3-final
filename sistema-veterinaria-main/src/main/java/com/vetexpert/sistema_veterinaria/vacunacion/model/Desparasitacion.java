package com.vetexpert.sistema_veterinaria.vacunacion.model;

import com.vetexpert.sistema_veterinaria.mascotas.model.Mascota;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa una desparasitación aplicada o programada para una mascota.
 * Módulo: vacunacion
 */
@Entity
@Table(name = "desparasitaciones")
public class Desparasitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_desparasitacion", nullable = false, unique = true, length = 15)
    private String codigoDesparasitacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mascota_id", nullable = false)
    private Mascota mascota;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoDesparasitacion tipo;

    @Column(nullable = false, length = 100)
    private String producto;

    @Column(name = "fecha_aplicacion", nullable = false)
    private LocalDate fechaAplicacion;

    @Column(name = "fecha_proxima_aplicacion", nullable = false)
    private LocalDate fechaProximaAplicacion;

    @Column(nullable = false, length = 100)
    private String veterinario;

    @Column(length = 500)
    private String observaciones;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoVacuna estado;

    @Column(nullable = false)
    private Double precio = 0.0;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    // ========== Ciclo de Vida JPA (Auditoría) ==========

    @PrePersist
    protected void onCreate() {
        this.fechaRegistro = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
        if (this.precio == null) this.precio = 0.0;
    }

    @PreUpdate
    protected void onUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }

    // ========== Constructores ==========

    public Desparasitacion() {
    }

    // ========== Getters y Setters ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigoDesparasitacion() {
        return codigoDesparasitacion;
    }

    public void setCodigoDesparasitacion(String codigoDesparasitacion) {
        this.codigoDesparasitacion = codigoDesparasitacion;
    }

    public Mascota getMascota() {
        return mascota;
    }

    public void setMascota(Mascota mascota) {
        this.mascota = mascota;
    }

    public TipoDesparasitacion getTipo() {
        return tipo;
    }

    public void setTipo(TipoDesparasitacion tipo) {
        this.tipo = tipo;
    }

    public String getProducto() {
        return producto;
    }

    public void setProducto(String producto) {
        this.producto = producto;
    }

    public LocalDate getFechaAplicacion() {
        return fechaAplicacion;
    }

    public void setFechaAplicacion(LocalDate fechaAplicacion) {
        this.fechaAplicacion = fechaAplicacion;
    }

    public LocalDate getFechaProximaAplicacion() {
        return fechaProximaAplicacion;
    }

    public void setFechaProximaAplicacion(LocalDate fechaProximaAplicacion) {
        this.fechaProximaAplicacion = fechaProximaAplicacion;
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

    public EstadoVacuna getEstado() {
        return estado;
    }

    public void setEstado(EstadoVacuna estado) {
        this.estado = estado;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
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
}
