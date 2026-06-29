package com.vetexpert.sistema_veterinaria.inventario.repository;

import com.vetexpert.sistema_veterinaria.inventario.model.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {

    Optional<Proveedor> findByRuc(String ruc);

    boolean existsByRuc(String ruc);

    @Query("SELECT p FROM Proveedor p WHERE " +
           "LOWER(p.ruc) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.razonSocial) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.contacto) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Proveedor> buscarInteligente(@Param("query") String query);
}
