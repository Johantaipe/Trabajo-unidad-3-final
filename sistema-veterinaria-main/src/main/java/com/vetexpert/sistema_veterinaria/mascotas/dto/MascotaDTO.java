package com.vetexpert.sistema_veterinaria.mascotas.dto;

import com.vetexpert.sistema_veterinaria.mascotas.entity.Especie;
import com.vetexpert.sistema_veterinaria.mascotas.entity.Sexo;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

/**
 * DTO para la transferencia y validación de datos de Mascotas.
 */
public class MascotaDTO {

    private Long id;
    private String codigoMascota;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s]+$", message = "El nombre solo debe contener letras")
    private String nombre;

    @NotNull(message = "La especie es obligatoria")
    private Especie especie;

    @NotBlank(message = "La raza es obligatoria")
    @Size(min = 2, message = "La raza debe tener al menos 2 caracteres")
    private String raza;

    @NotNull(message = "El sexo es obligatorio")
    private Sexo sexo;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @PastOrPresent(message = "La fecha de nacimiento no puede ser una fecha futura")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaNacimiento;

    @NotNull(message = "El peso es obligatorio")
    @Positive(message = "El peso debe ser mayor que cero")
    private Double peso;

    @Size(max = 50, message = "El color no puede superar los 50 caracteres")
    private String color;

    private boolean esterilizado;

    @Size(max = 500, message = "Las observaciones no pueden superar los 500 caracteres")
    private String observaciones;

    private String fotoUrl;

    @NotNull(message = "El propietario es obligatorio")
    private Long propietarioId;

    // ========== Constructores ==========

    public MascotaDTO() {
    }

    public MascotaDTO(Long id, String codigoMascota, String nombre, Especie especie, String raza, Sexo sexo,
                      LocalDate fechaNacimiento, Double peso, String color, boolean esterilizado,
                      String observaciones, String fotoUrl, Long propietarioId) {
        this.id = id;
        this.codigoMascota = codigoMascota;
        this.nombre = nombre;
        this.especie = especie;
        this.raza = raza;
        this.sexo = sexo;
        this.fechaNacimiento = fechaNacimiento;
        this.peso = peso;
        this.color = color;
        this.esterilizado = esterilizado;
        this.observaciones = observaciones;
        this.fotoUrl = fotoUrl;
        this.propietarioId = propietarioId;
    }

    // ========== Getters y Setters ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigoMascota() {
        return codigoMascota;
    }

    public void setCodigoMascota(String codigoMascota) {
        this.codigoMascota = codigoMascota;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Especie getEspecie() {
        return especie;
    }

    public void setEspecie(Especie especie) {
        this.especie = especie;
    }

    public String getRaza() {
        return raza;
    }

    public void setRaza(String raza) {
        this.raza = raza;
    }

    public Sexo getSexo() {
        return sexo;
    }

    public void setSexo(Sexo sexo) {
        this.sexo = sexo;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public Double getPeso() {
        return peso;
    }

    public void setPeso(Double peso) {
        this.peso = peso;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public boolean isEsterilizado() {
        return esterilizado;
    }

    public void setEsterilizado(boolean esterilizado) {
        this.esterilizado = esterilizado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public Long getPropietarioId() {
        return propietarioId;
    }

    public void setPropietarioId(Long propietarioId) {
        this.propietarioId = propietarioId;
    }

    @Override
    public String toString() {
        return "MascotaDTO{" +
                "id=" + id +
                ", codigoMascota='" + codigoMascota + '\'' +
                ", nombre='" + nombre + '\'' +
                ", especie=" + especie +
                ", raza='" + raza + '\'' +
                ", sexo=" + sexo +
                ", fechaNacimiento=" + fechaNacimiento +
                ", peso=" + peso +
                ", color='" + color + '\'' +
                ", esterilizado=" + esterilizado +
                ", propietarioId=" + propietarioId +
                '}';
    }
}
