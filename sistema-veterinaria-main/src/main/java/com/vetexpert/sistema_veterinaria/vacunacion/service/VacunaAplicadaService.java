package com.vetexpert.sistema_veterinaria.vacunacion.service;

import com.vetexpert.sistema_veterinaria.vacunacion.dto.VacunaDTO;
import com.vetexpert.sistema_veterinaria.vacunacion.model.VacunaAplicada;
import com.vetexpert.sistema_veterinaria.mascotas.model.Especie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz de servicio para la gestión de vacunas aplicadas/programadas a mascotas.
 */
public interface VacunaAplicadaService {

    VacunaAplicada registrarVacunaAplicada(VacunaDTO dto);

    VacunaAplicada actualizarVacunaAplicada(Long id, VacunaDTO dto);

    void eliminarVacunaAplicada(Long id);

    Optional<VacunaAplicada> obtenerVacunaAplicadaPorId(Long id);

    List<VacunaAplicada> listarVacunasAplicadas();

    Page<VacunaAplicada> listarVacunasAplicadasPaginado(String nombreMascota, Especie especie, Pageable pageable);

    List<VacunaAplicada> buscarPorMascotaId(Long mascotaId);
}
