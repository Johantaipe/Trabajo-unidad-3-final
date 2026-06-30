package com.vetexpert.sistema_veterinaria.inventario.repository;

import com.vetexpert.sistema_veterinaria.inventario.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Optional<Producto> findByCodigoProducto(String codigoProducto);

    boolean existsByCodigoProducto(String codigoProducto);

    List<Producto> findByActivoTrue();

    List<Producto> findByCategoriaAndActivoTrue(String categoria);

    @Query("SELECT MAX(p.codigoProducto) FROM Producto p")
    String findMaxCodigoProducto();

    @Query("SELECT p FROM Producto p WHERE p.activo = true AND (" +
           "LOWER(p.codigoProducto) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.nombre) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.categoria) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.lote) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "(p.proveedor IS NOT NULL AND LOWER(p.proveedor.razonSocial) LIKE LOWER(CONCAT('%', :query, '%'))))")
    List<Producto> buscarGlobal(@Param("query") String query);

    @Query("SELECT p FROM Producto p WHERE p.activo = true AND p.stock <= p.stockMinimo")
    List<Producto> findStockCritico();

    @Query("SELECT p FROM Producto p WHERE p.activo = true AND p.fechaVencimiento IS NOT NULL AND p.fechaVencimiento <= :fechaLimite ORDER BY p.fechaVencimiento ASC")
    List<Producto> findProductosPorVencer(@Param("fechaLimite") LocalDate fechaLimite);

    @Query("SELECT p FROM Producto p WHERE p.activo = true AND p.fechaVencimiento IS NOT NULL AND p.fechaVencimiento < :hoy ORDER BY p.fechaVencimiento ASC")
    List<Producto> findProductosVencidos(@Param("hoy") LocalDate hoy);
}
