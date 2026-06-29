package com.vetexpert.sistema_veterinaria.vacunacion.repository;

import com.vetexpert.sistema_veterinaria.vacunacion.model.Vacuna;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad Vacuna (Catálogo).
 * Módulo: vacunacion
 */
@Repository
public interface VacunaRepository extends JpaRepository<Vacuna, Long> {

    @Query("SELECT MAX(v.codigoVacuna) FROM Vacuna v")
    String findMaxCodigoVacuna();

    List<Vacuna> findByActivoTrue();

    Optional<Vacuna> findByNombreIgnoreCase(String nombre);
}
