package com.vetexpert.sistema_veterinaria.desparasitaciones.service;

import com.vetexpert.sistema_veterinaria.desparasitaciones.dto.DesparasitacionDTO;
import com.vetexpert.sistema_veterinaria.desparasitaciones.entity.Desparasitacion;
import com.vetexpert.sistema_veterinaria.mascotas.entity.Especie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz del servicio para la gestión de Desparasitaciones.
 */
public interface DesparasitacionService {

    /**
     * Registra una nueva desparasitación a partir de su DTO.
     */
    Desparasitacion registrarDesparasitacion(DesparasitacionDTO dto);

    /**
     * Actualiza los datos de una desparasitación existente.
     */
    Desparasitacion actualizarDesparasitacion(Long id, DesparasitacionDTO dto);

    /**
     * Elimina físicamente una desparasitación del sistema.
     */
    void eliminarDesparasitacion(Long id);

    /**
     * Obtiene una desparasitación por su ID.
     */
    Optional<Desparasitacion> obtenerDesparasitacionPorId(Long id);

    /**
     * Lista todas las desparasitaciones del sistema.
     */
    List<Desparasitacion> listarDesparasitaciones();

    /**
     * Lista desparasitaciones paginadas aplicando filtros de búsqueda por mascota y especie.
     */
    Page<Desparasitacion> listarDesparasitacionesPaginado(String nombreMascota, Especie especie, Pageable pageable);

    /**
     * Obtiene las desparasitaciones de una mascota específica.
     */
    List<Desparasitacion> buscarPorMascotaId(Long mascotaId);
}
