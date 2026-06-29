package com.vetexpert.sistema_veterinaria.historias.service.impl;

import com.vetexpert.sistema_veterinaria.historias.dto.HistoriaClinicaDTO;
import com.vetexpert.sistema_veterinaria.historias.model.EstadoConsulta;
import com.vetexpert.sistema_veterinaria.historias.model.HistoriaClinica;
import com.vetexpert.sistema_veterinaria.historias.repository.HistoriaClinicaRepository;
import com.vetexpert.sistema_veterinaria.historias.service.HistoriaClinicaService;
import com.vetexpert.sistema_veterinaria.mascotas.model.Mascota;
import com.vetexpert.sistema_veterinaria.mascotas.service.MascotaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import com.vetexpert.sistema_veterinaria.caja.service.VentaService;
import com.vetexpert.sistema_veterinaria.servicios.service.ServicioService;
import com.vetexpert.sistema_veterinaria.servicios.model.Servicio;
import com.vetexpert.sistema_veterinaria.inventario.service.ProductoService;
import com.vetexpert.sistema_veterinaria.inventario.model.Producto;

/**
 * Implementación transaccional del servicio de Historia Clínica.
 * Implementa la lógica de negocio, mapeos DTO y generación secuencial de códigos.
 */
@Service
@Transactional
public class HistoriaClinicaServiceImpl implements HistoriaClinicaService {

    private final HistoriaClinicaRepository historiaClinicaRepository;
    private final MascotaService mascotaService;
    private final VentaService ventaService;
    private final ServicioService servicioService;
    private final ProductoService productoService;

    public HistoriaClinicaServiceImpl(HistoriaClinicaRepository historiaClinicaRepository,
                                      MascotaService mascotaService,
                                      VentaService ventaService,
                                      ServicioService servicioService,
                                      ProductoService productoService) {
        this.historiaClinicaRepository = historiaClinicaRepository;
        this.mascotaService = mascotaService;
        this.ventaService = ventaService;
        this.servicioService = servicioService;
        this.productoService = productoService;
    }

    @Override
    public HistoriaClinica registrarConsulta(HistoriaClinicaDTO dto) {
        Mascota mascota = mascotaService.obtenerMascotaPorId(dto.getMascotaId())
                .orElseThrow(() -> new IllegalArgumentException("Mascota no encontrada con ID: " + dto.getMascotaId()));

        HistoriaClinica consulta = new HistoriaClinica();
        consulta.setCodigoConsulta(generarSiguienteCodigo());
        consulta.setFechaConsulta(dto.getFechaConsulta());
        consulta.setMotivoConsulta(dto.getMotivoConsulta());
        consulta.setAnamnesis(dto.getAnamnesis());
        consulta.setTemperatura(dto.getTemperatura());
        consulta.setPesoActual(dto.getPesoActual());
        consulta.setFrecuenciaCardiaca(dto.getFrecuenciaCardiaca());
        consulta.setFrecuenciaRespiratoria(dto.getFrecuenciaRespiratoria());
        consulta.setDiagnostico(dto.getDiagnostico());
        consulta.setTratamiento(dto.getTratamiento());
        consulta.setMedicamentos(dto.getMedicamentos());
        consulta.setRecomendaciones(dto.getRecomendaciones());
        consulta.setProximaVisita(dto.getProximaVisita());
        consulta.setEstadoConsulta(dto.getEstadoConsulta());
        consulta.setVeterinario(dto.getVeterinario());
        consulta.setMascota(mascota);

        HistoriaClinica saved = historiaClinicaRepository.save(consulta);
        procesarAutomatizacionConsulta(saved);
        return saved;
    }

    @Override
    public HistoriaClinica editarConsulta(Long id, HistoriaClinicaDTO dto) {
        HistoriaClinica consulta = historiaClinicaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Consulta clínica no encontrada con ID: " + id));

        Mascota mascota = mascotaService.obtenerMascotaPorId(dto.getMascotaId())
                .orElseThrow(() -> new IllegalArgumentException("Mascota no encontrada con ID: " + dto.getMascotaId()));

        consulta.setFechaConsulta(dto.getFechaConsulta());
        consulta.setMotivoConsulta(dto.getMotivoConsulta());
        consulta.setAnamnesis(dto.getAnamnesis());
        consulta.setTemperatura(dto.getTemperatura());
        consulta.setPesoActual(dto.getPesoActual());
        consulta.setFrecuenciaCardiaca(dto.getFrecuenciaCardiaca());
        consulta.setFrecuenciaRespiratoria(dto.getFrecuenciaRespiratoria());
        consulta.setDiagnostico(dto.getDiagnostico());
        consulta.setTratamiento(dto.getTratamiento());
        consulta.setMedicamentos(dto.getMedicamentos());
        consulta.setRecomendaciones(dto.getRecomendaciones());
        consulta.setProximaVisita(dto.getProximaVisita());
        consulta.setEstadoConsulta(dto.getEstadoConsulta());
        consulta.setVeterinario(dto.getVeterinario());
        consulta.setMascota(mascota);

        HistoriaClinica saved = historiaClinicaRepository.save(consulta);
        procesarAutomatizacionConsulta(saved);
        return saved;
    }

    @Override
    public void eliminarConsulta(Long id) {
        if (!historiaClinicaRepository.existsById(id)) {
            throw new IllegalArgumentException("Consulta clínica no encontrada con ID: " + id);
        }
        historiaClinicaRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistoriaClinica> buscarPorMascota(Long mascotaId) {
        return historiaClinicaRepository.buscarPorMascota(mascotaId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<HistoriaClinica> obtenerDetalleConsulta(Long id) {
        return historiaClinicaRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistoriaClinica> listarConsultas() {
        return historiaClinicaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HistoriaClinica> listarConsultasPaginado(String mascotaNombre, EstadoConsulta estado, Pageable pageable) {
        return historiaClinicaRepository.buscarConFiltros(mascotaNombre, estado, pageable);
    }

    @Override
    public HistoriaClinica cambiarEstado(Long id, EstadoConsulta estado) {
        HistoriaClinica consulta = historiaClinicaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Consulta clínica no encontrada con ID: " + id));
        consulta.setEstadoConsulta(estado);
        HistoriaClinica saved = historiaClinicaRepository.save(consulta);
        procesarAutomatizacionConsulta(saved);
        return saved;
    }

    private void procesarAutomatizacionConsulta(HistoriaClinica consulta) {
        if (consulta.getEstadoConsulta() == EstadoConsulta.ATENDIDA || consulta.getEstadoConsulta() == EstadoConsulta.CERRADA) {
            // 1. Verificar si ya existe una venta generada para esta consulta
            boolean existe = ventaService.existeVentaParaConcepto(consulta.getId(), "CONSULTA");
            if (!existe) {
                // Obtener precio del servicio "Consulta General"
                double precioConsulta = 40.0;
                Optional<Servicio> servicioOpt = servicioService.buscarPorNombre("Consulta General");
                if (servicioOpt.isPresent()) {
                    precioConsulta = servicioOpt.get().getPrecioBase();
                }

                // Generar venta pendiente para la consulta en caja
                ventaService.crearVentaPendiente(
                    consulta.getMascota().getPropietario().getId(),
                    consulta.getMascota().getId(),
                    "Consulta Clínica: " + consulta.getCodigoConsulta(),
                    consulta.getId(),
                    "CONSULTA",
                    precioConsulta
                );

                // 2. Descontar stock de medicamentos utilizados
                String medicamentosTxt = consulta.getMedicamentos();
                if (medicamentosTxt != null && !medicamentosTxt.trim().isEmpty()) {
                    List<Producto> productosActivos = productoService.listarProductos();
                    for (Producto prod : productosActivos) {
                        // Comprobar si el nombre del producto está contenido en el texto de medicamentos (ignorando mayúsculas/minúsculas)
                        if (medicamentosTxt.toLowerCase().contains(prod.getNombre().toLowerCase())) {
                            // Descontar 1 del stock
                            if (prod.getStock() >= 1) {
                                productoService.descontarStock(prod.getId(), 1, "CONSUMO_CLINICO", "sistema");
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Genera automáticamente el código correlativo de consulta (CONS-000001, CONS-000002, etc.).
     */
    private synchronized String generarSiguienteCodigo() {
        String maxCodigo = historiaClinicaRepository.findMaxCodigoConsulta();
        if (maxCodigo == null || !maxCodigo.startsWith("CONS-")) {
            return "CONS-000001";
        }
        try {
            int numero = Integer.parseInt(maxCodigo.substring(5));
            return String.format("CONS-%06d", numero + 1);
        } catch (NumberFormatException e) {
            return "CONS-000001";
        }
    }
}
