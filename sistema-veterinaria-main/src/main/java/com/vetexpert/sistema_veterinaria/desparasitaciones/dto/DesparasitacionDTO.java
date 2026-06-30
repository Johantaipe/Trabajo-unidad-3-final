package com.vetexpert.sistema_veterinaria.desparasitaciones.dto;

import com.vetexpert.sistema_veterinaria.vacunas.entity.EstadoVacuna;
import com.vetexpert.sistema_veterinaria.desparasitaciones.entity.TipoDesparasitacion;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

/**
 * DTO para la transferencia y validación de datos de Desparasitación.
 */
public class DesparasitacionDTO {

    private Long id;
    private String codigoDesparasitacion;

    @NotNull(message = "La mascota es obligatoria")
    private Long mascotaId;

    @NotNull(message = "El tipo de desparasitación es obligatorio")
    private TipoDesparasitacion tipo;

    @NotBlank(message = "El producto es obligatorio")
    @Size(max = 100, message = "El producto no puede superar los 100 caracteres")
    private String producto;

    @NotNull(message = "La fecha de aplicación es obligatoria")
    @PastOrPresent(message = "La fecha de aplicación no puede ser una fecha futura")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaAplicacion;

    @NotNull(message = "La fecha de próxima aplicación es obligatoria")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaProximaAplicacion;

    @NotBlank(message = "El veterinario es obligatorio")
    @Size(max = 100, message = "El nombre del veterinario no puede superar los 100 caracteres")
    private String veterinario;

    @Size(max = 500, message = "Las observaciones no pueden superar los 500 caracteres")
    private String observaciones;

    private EstadoVacuna estado;

    @NotNull(message = "El precio es obligatorio")
    @PositiveOrZero(message = "El precio debe ser mayor o igual a cero")
    private Double precio;

    // ========== Constructores ==========

    public DesparasitacionDTO() {
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

    public Long getMascotaId() {
        return mascotaId;
    }

    public void setMascotaId(Long mascotaId) {
        this.mascotaId = mascotaId;
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
}
