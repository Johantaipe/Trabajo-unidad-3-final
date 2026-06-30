package com.vetexpert.sistema_veterinaria.agenda.repository;

import com.vetexpert.sistema_veterinaria.agenda.entity.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    @Query("SELECT MAX(c.codigoCita) FROM Cita c")
    String findMaxCodigoCita();

    List<Cita> findByFecha(LocalDate fecha);

    @Query("SELECT c FROM Cita c WHERE c.fecha = :fecha AND c.veterinario = :veterinario AND c.estado != 'CANCELADA'")
    List<Cita> findByFechaAndVeterinario(@Param("fecha") LocalDate fecha, @Param("veterinario") String veterinario);

    List<Cita> findByPropietarioIdOrderByFechaDesc(Long propietarioId);
}
