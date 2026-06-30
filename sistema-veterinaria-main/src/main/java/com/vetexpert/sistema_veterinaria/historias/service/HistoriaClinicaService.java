package com.vetexpert.sistema_veterinaria.historias.service;

import com.vetexpert.sistema_veterinaria.historias.dto.HistoriaClinicaDTO;
import com.vetexpert.sistema_veterinaria.historias.entity.EstadoConsulta;
import com.vetexpert.sistema_veterinaria.historias.entity.HistoriaClinica;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz del servicio para la gestión de Historias Clínicas (Consultas).
 * Define el contrato de negocio siguiendo principios SOLID.
 */
public interface HistoriaClinicaService {

    /**
     * Registra una nueva consulta clínica.
     */
    HistoriaClinica registrarConsulta(HistoriaClinicaDTO dto);

    /**
     * Edita los datos de una consulta clínica existente.
     */
    HistoriaClinica editarConsulta(Long id, HistoriaClinicaDTO dto);

    /**
     * Elimina físicamente una consulta clínica del historial.
     */
    void eliminarConsulta(Long id);

    /**
     * Obtiene el historial clínico de una mascota específica por su ID.
     */
    List<HistoriaClinica> buscarPorMascota(Long mascotaId);

    /**
     * Obtiene los detalles de una consulta clínica específica por su ID.
     */
    Optional<HistoriaClinica> obtenerDetalleConsulta(Long id);

    /**
     * Lista todas las consultas registradas en el sistema.
     */
    List<HistoriaClinica> listarConsultas();

    /**
     * Obtiene una lista paginada de consultas clínicas aplicando filtros dinámicos.
     */
    Page<HistoriaClinica> listarConsultasPaginado(String mascotaNombre, EstadoConsulta estado, Pageable pageable);

    /**
     * Cambia el estado de una consulta (ej. PENDIENTE, ATENDIDA, HOSPITALIZADA, CERRADA).
     */
    HistoriaClinica cambiarEstado(Long id, EstadoConsulta estado);
}
