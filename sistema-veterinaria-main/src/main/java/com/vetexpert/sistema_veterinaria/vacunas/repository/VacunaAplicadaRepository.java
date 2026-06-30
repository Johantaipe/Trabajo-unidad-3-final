package com.vetexpert.sistema_veterinaria.vacunas.repository;

import com.vetexpert.sistema_veterinaria.vacunas.entity.VacunaAplicada;
import com.vetexpert.sistema_veterinaria.vacunas.entity.EstadoVacuna;
import com.vetexpert.sistema_veterinaria.mascotas.entity.Especie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repositorio JPA para la entidad VacunaAplicada (registro de inmunización).
 * Módulo: vacunacion
 */
@Repository
public interface VacunaAplicadaRepository extends JpaRepository<VacunaAplicada, Long> {

    @Query("SELECT MAX(va.codigoVacunaAplicada) FROM VacunaAplicada va")
    String findMaxCodigoVacunaAplicada();

    @Query("SELECT va FROM VacunaAplicada va WHERE va.mascota.id = :mascotaId ORDER BY va.fechaAplicacion DESC")
    List<VacunaAplicada> buscarPorMascotaId(@Param("mascotaId") Long mascotaId);

    @Query("SELECT va FROM VacunaAplicada va WHERE " +
           "(:nombreMascota IS NULL OR :nombreMascota = '' OR LOWER(va.mascota.nombre) LIKE LOWER(CONCAT('%', :nombreMascota, '%'))) AND " +
           "(:especie IS NULL OR va.mascota.especie = :especie)")
    Page<VacunaAplicada> buscarConFiltros(@Param("nombreMascota") String nombreMascota, 
                                          @Param("especie") Especie especie, 
                                          Pageable pageable);

    @Query("SELECT va FROM VacunaAplicada va WHERE va.estado = :estado ORDER BY va.fechaProximaDosis ASC")
    List<VacunaAplicada> buscarPorEstado(@Param("estado") EstadoVacuna estado);

    long countByEstado(EstadoVacuna estado);
}
