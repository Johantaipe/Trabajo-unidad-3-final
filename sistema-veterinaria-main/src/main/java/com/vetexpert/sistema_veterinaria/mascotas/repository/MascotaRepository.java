package com.vetexpert.sistema_veterinaria.mascotas.repository;

import com.vetexpert.sistema_veterinaria.mascotas.model.Especie;
import com.vetexpert.sistema_veterinaria.mascotas.model.Mascota;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad Mascota.
 * Define operaciones CRUD y consultas específicas con JPQL.
 */
@Repository
public interface MascotaRepository extends JpaRepository<Mascota, Long> {

    /**
     * Busca mascotas que contengan el nombre especificado (case-insensitive).
     */
    @Query("SELECT m FROM Mascota m WHERE LOWER(m.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Mascota> buscarPorNombre(@Param("nombre") String nombre);

    /**
     * Busca mascotas por su especie.
     */
    @Query("SELECT m FROM Mascota m WHERE m.especie = :especie")
    List<Mascota> buscarPorEspecie(@Param("especie") Especie especie);

    /**
     * Lista únicamente las mascotas activas.
     */
    @Query("SELECT m FROM Mascota m WHERE m.activo = true")
    List<Mascota> listarActivos();

    /**
     * Busca mascotas asociadas a un propietario por su ID.
     */
    @Query("SELECT m FROM Mascota m WHERE m.propietario.id = :propietarioId")
    List<Mascota> buscarPorPropietario(@Param("propietarioId") Long propietarioId);

    /**
     * Busca una mascota por su código único de mascota.
     */
    @Query("SELECT m FROM Mascota m WHERE m.codigoMascota = :codigoMascota")
    Optional<Mascota> buscarPorCodigoMascota(@Param("codigoMascota") String codigoMascota);

    /**
     * Obtiene el código de mascota máximo registrado (para autogenerar el siguiente secuencial).
     */
    @Query("SELECT MAX(m.codigoMascota) FROM Mascota m")
    String findMaxCodigoMascota();

    /**
     * Realiza una búsqueda dinámica paginada aplicando filtros opcionales de nombre y especie.
     */
    @Query("SELECT m FROM Mascota m WHERE " +
           "(:nombre IS NULL OR :nombre = '' OR LOWER(m.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND " +
           "(:especie IS NULL OR m.especie = :especie)")
    Page<Mascota> buscarConFiltros(@Param("nombre") String nombre, @Param("especie") Especie especie, Pageable pageable);
}
