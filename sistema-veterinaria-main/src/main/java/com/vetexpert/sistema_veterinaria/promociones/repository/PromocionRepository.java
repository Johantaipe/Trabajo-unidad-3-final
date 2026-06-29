package com.vetexpert.sistema_veterinaria.promociones.repository;

import com.vetexpert.sistema_veterinaria.promociones.model.Promocion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PromocionRepository extends JpaRepository<Promocion, Long> {

    @Query("SELECT p FROM Promocion p WHERE p.activo = true AND :fecha BETWEEN p.fechaInicio AND p.fechaFin ORDER BY p.id DESC")
    List<Promocion> findActivePromotions(@Param("fecha") LocalDate fecha);

    List<Promocion> findByOrderByFechaRegistroDesc();
}
