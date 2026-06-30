package com.vetexpert.sistema_veterinaria.agenda.service;

import com.vetexpert.sistema_veterinaria.agenda.dto.CitaDTO;
import com.vetexpert.sistema_veterinaria.agenda.dto.ResumenMascotaDTO;
import com.vetexpert.sistema_veterinaria.agenda.entity.Cita;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface CitaService {
    Cita registrarCita(CitaDTO dto);
    Cita actualizarCita(Long id, CitaDTO dto);
    void eliminarCita(Long id);
    Optional<Cita> obtenerPorId(Long id);
    List<Cita> listarCitas();
    List<Cita> listarPorFecha(LocalDate fecha);
    
    Cita cambiarEstado(Long id, String estado);
    Cita reprogramarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora);
    
    ResumenMascotaDTO obtenerResumenMascota(Long mascotaId);
    
    void validarColision(LocalDate fecha, LocalTime hora, int duracion, String veterinario, Long excludeId);
}
