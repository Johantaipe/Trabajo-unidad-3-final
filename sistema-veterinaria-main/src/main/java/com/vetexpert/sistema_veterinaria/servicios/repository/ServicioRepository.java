package com.vetexpert.sistema_veterinaria.servicios.repository;

import com.vetexpert.sistema_veterinaria.servicios.model.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Long> {

    @Query("SELECT MAX(s.codigoServicio) FROM Servicio s")
    String findMaxCodigoServicio();

    List<Servicio> findByActivoTrue();

    Optional<Servicio> findByNombreIgnoreCase(String nombre);
}
