package com.vetexpert.sistema_veterinaria.vacunacion.repository;

import com.vetexpert.sistema_veterinaria.vacunacion.model.Desparasitacion;
import com.vetexpert.sistema_veterinaria.vacunacion.model.EstadoVacuna;
import com.vetexpert.sistema_veterinaria.mascotas.model.Especie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio JPA para la entidad Desparasitacion.
 */
@Repository
public interface DesparasitacionRepository extends JpaRepository<Desparasitacion, Long> {

    @Query("SELECT MAX(d.codigoDesparasitacion) FROM Desparasitacion d")
    String findMaxCodigoDesparasitacion();

    @Query("SELECT d FROM Desparasitacion d WHERE d.mascota.id = :mascotaId ORDER BY d.fechaAplicacion DESC")
    List<Desparasitacion> buscarPorMascotaId(@Param("mascotaId") Long mascotaId);

    @Query("SELECT d FROM Desparasitacion d WHERE " +
           "(:nombreMascota IS NULL OR :nombreMascota = '' OR LOWER(d.mascota.nombre) LIKE LOWER(CONCAT('%', :nombreMascota, '%'))) AND " +
           "(:especie IS NULL OR d.mascota.especie = :especie)")
    Page<Desparasitacion> buscarConFiltros(@Param("nombreMascota") String nombreMascota, 
                                            @Param("especie") Especie especie, 
                                            Pageable pageable);

    @Query("SELECT d FROM Desparasitacion d WHERE d.estado = :estado ORDER BY d.fechaProximaAplicacion ASC")
    List<Desparasitacion> buscarPorEstado(@Param("estado") EstadoVacuna estado);

    @Query("SELECT d FROM Desparasitacion d WHERE d.fechaProximaAplicacion < :fechaActual AND d.estado <> 'VENCIDA'")
    List<Desparasitacion> buscarDesparasitacionesExpiradasSinActualizar(@Param("fechaActual") LocalDate fechaActual);

    @Query("SELECT d FROM Desparasitacion d WHERE d.fechaProximaAplicacion >= :fechaActual AND d.fechaProximaAplicacion <= :fechaLimite AND d.estado <> 'PROXIMA_A_VENCER' AND d.estado <> 'VENCIDA'")
    List<Desparasitacion> buscarDesparasitacionesProximasSinActualizar(@Param("fechaActual") LocalDate fechaActual, @Param("fechaLimite") LocalDate fechaLimite);

    long countByEstado(EstadoVacuna estado);
}
