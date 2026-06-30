package com.vetexpert.sistema_veterinaria.historias.repository;

import com.vetexpert.sistema_veterinaria.historias.entity.EstadoConsulta;
import com.vetexpert.sistema_veterinaria.historias.entity.HistoriaClinica;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad HistoriaClinica.
 * Proporciona métodos CRUD y consultas específicas con JPQL.
 */
@Repository
public interface HistoriaClinicaRepository extends JpaRepository<HistoriaClinica, Long> {

    /**
     * Busca el historial clínico completo de una mascota ordenado de la consulta más reciente a la más antigua.
     */
    @Query("SELECT h FROM HistoriaClinica h WHERE h.mascota.id = :mascotaId ORDER BY h.fechaConsulta DESC, h.id DESC")
    List<HistoriaClinica> buscarPorMascota(@Param("mascotaId") Long mascotaId);

    /**
     * Busca una consulta por su código único de consulta.
     */
    @Query("SELECT h FROM HistoriaClinica h WHERE h.codigoConsulta = :codigoConsulta")
    Optional<HistoriaClinica> buscarPorCodigoConsulta(@Param("codigoConsulta") String codigoConsulta);

    /**
     * Busca consultas realizadas en una fecha específica.
     */
    @Query("SELECT h FROM HistoriaClinica h WHERE h.fechaConsulta = :fecha")
    List<HistoriaClinica> buscarPorFecha(@Param("fecha") LocalDate fecha);

    /**
     * Busca consultas por su estado clínico.
     */
    @Query("SELECT h FROM HistoriaClinica h WHERE h.estadoConsulta = :estado")
    List<HistoriaClinica> buscarPorEstado(@Param("estado") EstadoConsulta estado);

    /**
     * Busca consultas por el nombre del veterinario (búsqueda parcial insensible a mayúsculas/minúsculas).
     */
    @Query("SELECT h FROM HistoriaClinica h WHERE LOWER(h.veterinario) LIKE LOWER(CONCAT('%', :veterinario, '%'))")
    List<HistoriaClinica> listarPorVeterinario(@Param("veterinario") String veterinario);

    /**
     * Obtiene el código de consulta máximo registrado (para autogenerar el siguiente secuencial).
     */
    @Query("SELECT MAX(h.codigoConsulta) FROM HistoriaClinica h")
    String findMaxCodigoConsulta();

    /**
     * Realiza una búsqueda dinámica paginada aplicando filtros opcionales de nombre de mascota y estado de consulta.
     */
    @Query("SELECT h FROM HistoriaClinica h WHERE " +
           "(:mascotaNombre IS NULL OR :mascotaNombre = '' OR LOWER(h.mascota.nombre) LIKE LOWER(CONCAT('%', :mascotaNombre, '%'))) AND " +
           "(:estado IS NULL OR h.estadoConsulta = :estado)")
    Page<HistoriaClinica> buscarConFiltros(@Param("mascotaNombre") String mascotaNombre,
                                           @Param("estado") EstadoConsulta estado,
                                           Pageable pageable);
}
