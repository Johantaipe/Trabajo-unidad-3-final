package com.vetexpert.sistema_veterinaria.vacunacion.service;

import com.vetexpert.sistema_veterinaria.vacunacion.model.Vacuna;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz de servicio para gestionar el Catálogo de Vacunas.
 */
public interface VacunaService {

    Vacuna registrarVacuna(Vacuna vacuna);

    Vacuna actualizarVacuna(Long id, Vacuna vacuna);

    void eliminarVacuna(Long id);

    Optional<Vacuna> obtenerVacunaPorId(Long id);

    List<Vacuna> listarVacunas();

    List<Vacuna> listarVacunasActivas();
}
