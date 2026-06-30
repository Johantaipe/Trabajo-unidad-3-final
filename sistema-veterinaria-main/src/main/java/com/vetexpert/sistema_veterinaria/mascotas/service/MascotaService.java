package com.vetexpert.sistema_veterinaria.mascotas.service;

import com.vetexpert.sistema_veterinaria.mascotas.dto.MascotaDTO;
import com.vetexpert.sistema_veterinaria.mascotas.entity.Especie;
import com.vetexpert.sistema_veterinaria.mascotas.entity.Mascota;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz del servicio para la gestión de Mascotas.
 * Define la API del negocio siguiendo el principio de inversión de dependencias.
 */
public interface MascotaService {

    /**
     * Registra una nueva mascota en el sistema a partir de su DTO.
     */
    Mascota registrarMascota(MascotaDTO mascotaDTO);

    /**
     * Actualiza los datos de una mascota existente.
     */
    Mascota actualizarMascota(Long id, MascotaDTO mascotaDTO);

    /**
     * Elimina físicamente una mascota del sistema.
     */
    void eliminarMascota(Long id);

    /**
     * Obtiene una mascota por su ID.
     */
    Optional<Mascota> obtenerMascotaPorId(Long id);

    /**
     * Lista todas las mascotas del sistema.
     */
    List<Mascota> listarMascotas();

    /**
     * Obtiene una lista paginada de mascotas aplicando filtros dinámicos.
     */
    Page<Mascota> listarMascotasPaginado(String nombre, Especie especie, Pageable pageable);

    /**
     * Busca mascotas por su nombre.
     */
    List<Mascota> buscarPorNombre(String nombre);

    /**
     * Busca mascotas por el ID del propietario.
     */
    List<Mascota> buscarPorPropietario(Long propietarioId);

    /**
     * Cambia el estado de activación de la mascota (activo true/false).
     */
    Mascota cambiarEstado(Long id, boolean activo);
}
