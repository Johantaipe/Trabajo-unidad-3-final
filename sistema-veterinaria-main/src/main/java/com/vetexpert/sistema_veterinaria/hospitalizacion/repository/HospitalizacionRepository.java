package com.vetexpert.sistema_veterinaria.hospitalizacion.repository;

import com.vetexpert.sistema_veterinaria.hospitalizacion.entity.Hospitalizacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HospitalizacionRepository extends JpaRepository<Hospitalizacion, Long> {

    @Query("SELECT MAX(h.codigoHospitalizacion) FROM Hospitalizacion h")
    String findMaxCodigoHospitalizacion();

    List<Hospitalizacion> findByEstado(String estado);

    @Query("SELECT h FROM Hospitalizacion h WHERE " +
           "LOWER(h.codigoHospitalizacion) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(h.mascota.nombre) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(h.motivo) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY h.fechaIngreso DESC")
    List<Hospitalizacion> buscarGlobal(@Param("query") String query);
}
