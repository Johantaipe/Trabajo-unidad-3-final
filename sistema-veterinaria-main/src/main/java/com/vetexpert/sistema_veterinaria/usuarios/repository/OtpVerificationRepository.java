package com.vetexpert.sistema_veterinaria.usuarios.repository;

import com.vetexpert.sistema_veterinaria.usuarios.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {

    /**
     * Obtiene la última verificación generada para un correo específico.
     */
    Optional<OtpVerification> findFirstByCorreoOrderByFechaCreacionDesc(String correo);

    /**
     * Obtiene la lista de códigos enviados a un correo desde una fecha determinada.
     * Utilizado para validar el límite de tasa anti-spam (máximo 3 envíos por 15 minutos).
     */
    List<OtpVerification> findByCorreoAndFechaCreacionAfter(String correo, LocalDateTime limitTime);
}
