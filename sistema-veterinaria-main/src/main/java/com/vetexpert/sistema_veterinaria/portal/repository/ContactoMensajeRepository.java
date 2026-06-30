package com.vetexpert.sistema_veterinaria.portal.repository;

import com.vetexpert.sistema_veterinaria.portal.entity.ContactoMensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactoMensajeRepository extends JpaRepository<ContactoMensaje, Long> {
    List<ContactoMensaje> findByOrderByFechaEnvioDesc();
    long countByLeido(boolean leido);
}
