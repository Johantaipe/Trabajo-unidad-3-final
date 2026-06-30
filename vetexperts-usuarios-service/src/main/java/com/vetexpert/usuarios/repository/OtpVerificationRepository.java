package com.vetexpert.usuarios.repository;

import com.vetexpert.usuarios.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {

    List<OtpVerification> findByCorreoAndEstado(String correo, String estado);

    Optional<OtpVerification> findTopByCorreoAndEstadoOrderByFechaCreacionDesc(String correo, String estado);

    long countByCorreoAndFechaCreacionAfter(String correo, LocalDateTime fecha);

    @Modifying
    @Transactional
    @Query("UPDATE OtpVerification o SET o.estado = 'EXPIRADO' WHERE o.correo = :correo AND o.estado = 'PENDIENTE'")
    void invalidarOtpsPendientesPorCorreo(String correo);
}
