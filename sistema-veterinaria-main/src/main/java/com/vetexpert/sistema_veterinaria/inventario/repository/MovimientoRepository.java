package com.vetexpert.sistema_veterinaria.inventario.repository;

import com.vetexpert.sistema_veterinaria.inventario.model.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    List<Movimiento> findByProductoIdAndProductoTipoOrderByFechaDesc(Long productoId, String productoTipo);

    @Query("SELECT m FROM Movimiento m ORDER BY m.fecha DESC")
    List<Movimiento> findUltimosMovimientos();
}
