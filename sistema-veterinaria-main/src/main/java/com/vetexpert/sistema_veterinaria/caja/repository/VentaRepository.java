package com.vetexpert.sistema_veterinaria.caja.repository;

import com.vetexpert.sistema_veterinaria.caja.entity.Venta;
import com.vetexpert.sistema_veterinaria.caja.entity.EstadoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    @Query("SELECT MAX(v.codigoVenta) FROM Venta v")
    String findMaxCodigoVenta();

    List<Venta> findByEstadoPago(EstadoPago estadoPago);

    @Query("SELECT v FROM Venta v WHERE " +
           "LOWER(v.codigoVenta) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(v.propietario.dni) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(v.propietario.nombre) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(v.propietario.apellido) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY v.fechaVenta DESC")
    List<Venta> buscarCaja(@Param("query") String query);

    List<Venta> findByFechaVentaBetweenOrderByFechaVentaDesc(LocalDateTime start, LocalDateTime end);

    List<Venta> findByEstadoPagoAndFechaVentaBetween(EstadoPago estadoPago, LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(v) > 0 FROM Venta v JOIN v.detalles d WHERE d.conceptoId = :conceptoId AND d.conceptoTipo = :conceptoTipo AND v.estadoPago != 'ANULADO'")
    boolean existeVentaParaConcepto(@Param("conceptoId") Long conceptoId, @Param("conceptoTipo") String conceptoTipo);

    List<Venta> findByPropietarioIdOrderByFechaVentaDesc(Long propietarioId);
}
