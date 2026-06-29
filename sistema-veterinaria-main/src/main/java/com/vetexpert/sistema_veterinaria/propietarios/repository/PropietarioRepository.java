package com.vetexpert.sistema_veterinaria.propietarios.repository;

import com.vetexpert.sistema_veterinaria.propietarios.model.Propietario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

/**
 * Repositorio JPA para la entidad Propietario.
 * Proporciona operaciones CRUD y consultas personalizadas.
 */
@Repository
public interface PropietarioRepository extends JpaRepository<Propietario, Long> {

    /**
     * Busca un propietario por su DNI.
     * Utilizado para validar unicidad.
     */
    Optional<Propietario> findByDni(String dni);

    /**
     * Verifica si existe un propietario con el DNI dado.
     */
    boolean existsByDni(String dni);

    /**
     * Busca un propietario por su correo electrónico.
     */
    Optional<Propietario> findByCorreo(String correo);

    /**
     * Busca de manera inteligente propietarios por DNI, nombre, apellido o teléfono.
     */
    @Query("SELECT p FROM Propietario p WHERE " +
           "LOWER(p.dni) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.nombre) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.apellido) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "(p.telefono IS NOT NULL AND LOWER(p.telefono) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Propietario> buscarInteligente(@Param("query") String query);
}
