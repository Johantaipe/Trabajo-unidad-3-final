package com.vetexpert.sistema_veterinaria.agenda.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO para llevar el resumen clínico y administrativo de una mascota para autocompletado y widgets.
 */
public class ResumenMascotaDTO {

    private String propietarioNombre;
    private String propietarioDni;
    private String propietarioTelefono;
    private String propietarioCorreo;

    private LocalDate ultimaConsulta;
    private LocalDate ultimaVacuna;
    private LocalDate proximaVacuna;
    private LocalDate ultimaDesparasitacion;

    private String estadoGeneral; // Activa, Inactiva, En observación, etc.
    private long totalVisitas;
    private String mascotaEspecie;

    private List<String> vacunasPendientes;
    private List<String> desparasitacionesPendientes;
    
    private LocalDate proximaCita;
    private String proximaCitaDetalle;

    // ========== Constructores ==========

    public ResumenMascotaDTO() {
    }

    // ========== Getters y Setters ==========

    public String getPropietarioNombre() {
        return propietarioNombre;
    }

    public void setPropietarioNombre(String propietarioNombre) {
        this.propietarioNombre = propietarioNombre;
    }

    public String getPropietarioDni() {
        return propietarioDni;
    }

    public void setPropietarioDni(String propietarioDni) {
        this.propietarioDni = propietarioDni;
    }

    public String getPropietarioTelefono() {
        return propietarioTelefono;
    }

    public void setPropietarioTelefono(String propietarioTelefono) {
        this.propietarioTelefono = propietarioTelefono;
    }

    public String getPropietarioCorreo() {
        return propietarioCorreo;
    }

    public void setPropietarioCorreo(String propietarioCorreo) {
        this.propietarioCorreo = propietarioCorreo;
    }

    public LocalDate getUltimaConsulta() {
        return ultimaConsulta;
    }

    public void setUltimaConsulta(LocalDate ultimaConsulta) {
        this.ultimaConsulta = ultimaConsulta;
    }

    public LocalDate getUltimaVacuna() {
        return ultimaVacuna;
    }

    public void setUltimaVacuna(LocalDate ultimaVacuna) {
        this.ultimaVacuna = ultimaVacuna;
    }

    public LocalDate getProximaVacuna() {
        return proximaVacuna;
    }

    public void setProximaVacuna(LocalDate proximaVacuna) {
        this.proximaVacuna = proximaVacuna;
    }

    public LocalDate getUltimaDesparasitacion() {
        return ultimaDesparasitacion;
    }

    public void setUltimaDesparasitacion(LocalDate ultimaDesparasitacion) {
        this.ultimaDesparasitacion = ultimaDesparasitacion;
    }

    public String getEstadoGeneral() {
        return estadoGeneral;
    }

    public void setEstadoGeneral(String estadoGeneral) {
        this.estadoGeneral = estadoGeneral;
    }

    public long getTotalVisitas() {
        return totalVisitas;
    }

    public void setTotalVisitas(long totalVisitas) {
        this.totalVisitas = totalVisitas;
    }

    public List<String> getVacunasPendientes() {
        return vacunasPendientes;
    }

    public void setVacunasPendientes(List<String> vacunasPendientes) {
        this.vacunasPendientes = vacunasPendientes;
    }

    public List<String> getDesparasitacionesPendientes() {
        return desparasitacionesPendientes;
    }

    public void setDesparasitacionesPendientes(List<String> desparasitacionesPendientes) {
        this.desparasitacionesPendientes = desparasitacionesPendientes;
    }

    public LocalDate getProximaCita() {
        return proximaCita;
    }

    public void setProximaCita(LocalDate proximaCita) {
        this.proximaCita = proximaCita;
    }

    public String getProximaCitaDetalle() {
        return proximaCitaDetalle;
    }

    public void setProximaCitaDetalle(String proximaCitaDetalle) {
        this.proximaCitaDetalle = proximaCitaDetalle;
    }

    public String getMascotaEspecie() {
        return mascotaEspecie;
    }

    public void setMascotaEspecie(String mascotaEspecie) {
        this.mascotaEspecie = mascotaEspecie;
    }
}
