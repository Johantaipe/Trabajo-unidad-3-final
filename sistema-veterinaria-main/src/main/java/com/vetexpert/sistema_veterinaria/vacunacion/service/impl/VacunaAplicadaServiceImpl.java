package com.vetexpert.sistema_veterinaria.vacunacion.service.impl;

import com.vetexpert.sistema_veterinaria.vacunacion.dto.VacunaDTO;
import com.vetexpert.sistema_veterinaria.vacunacion.model.EstadoVacuna;
import com.vetexpert.sistema_veterinaria.vacunacion.model.Vacuna;
import com.vetexpert.sistema_veterinaria.vacunacion.model.VacunaAplicada;
import com.vetexpert.sistema_veterinaria.vacunacion.repository.VacunaAplicadaRepository;
import com.vetexpert.sistema_veterinaria.vacunacion.repository.VacunaRepository;
import com.vetexpert.sistema_veterinaria.vacunacion.service.VacunaAplicadaService;
import com.vetexpert.sistema_veterinaria.mascotas.model.Especie;
import com.vetexpert.sistema_veterinaria.mascotas.model.Mascota;
import com.vetexpert.sistema_veterinaria.mascotas.service.MascotaService;
import com.vetexpert.sistema_veterinaria.caja.service.VentaService;
import com.vetexpert.sistema_veterinaria.inventario.service.MovimientoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * Implementación transaccional del servicio para gestionar las vacunas aplicadas.
 */
@Service
@Transactional
public class VacunaAplicadaServiceImpl implements VacunaAplicadaService {

    private final VacunaAplicadaRepository vacunaAplicadaRepository;
    private final VacunaRepository vacunaRepository;
    private final MascotaService mascotaService;
    private final VentaService ventaService;
    private final MovimientoService movimientoService;

    public VacunaAplicadaServiceImpl(VacunaAplicadaRepository vacunaAplicadaRepository,
                                      VacunaRepository vacunaRepository,
                                      MascotaService mascotaService,
                                      VentaService ventaService,
                                      MovimientoService movimientoService) {
        this.vacunaAplicadaRepository = vacunaAplicadaRepository;
        this.vacunaRepository = vacunaRepository;
        this.mascotaService = mascotaService;
        this.ventaService = ventaService;
        this.movimientoService = movimientoService;
    }

    @Override
    public VacunaAplicada registrarVacunaAplicada(VacunaDTO dto) {
        Mascota mascota = mascotaService.obtenerMascotaPorId(dto.getMascotaId())
                .orElseThrow(() -> new IllegalArgumentException("Mascota no encontrada con ID: " + dto.getMascotaId()));

        Vacuna vacuna = vacunaRepository.findById(dto.getVacunaId())
                .orElseThrow(() -> new IllegalArgumentException("Vacuna del catálogo no encontrada con ID: " + dto.getVacunaId()));

        validarFechas(dto.getFechaAplicacion(), dto.getFechaProximaDosis());

        // Descontar del stock si se marca como APLICADA
        EstadoVacuna estadoFinal = resolverEstado(dto.getFechaAplicacion(), dto.getFechaProximaDosis(), dto.getEstado());
        if (estadoFinal == EstadoVacuna.APLICADA || estadoFinal == EstadoVacuna.PROXIMA_A_VENCER) {
            if (vacuna.getStock() < 1) {
                throw new IllegalArgumentException("Stock insuficiente");
            }
            vacuna.setStock(vacuna.getStock() - 1);
            vacunaRepository.save(vacuna);

            // Log del movimiento de stock
            movimientoService.registrarMovimiento(
                "sistema", 
                vacuna.getId(), 
                "VACUNA", 
                vacuna.getNombre(), 
                -1, 
                "APLICACION_VACUNA", 
                "Aplicación de vacuna a mascota: " + mascota.getNombre()
            );
        }

        VacunaAplicada vacunaAplicada = new VacunaAplicada();
        vacunaAplicada.setCodigoVacunaAplicada(generarSiguienteCodigo());
        vacunaAplicada.setMascota(mascota);
        vacunaAplicada.setVacuna(vacuna);
        vacunaAplicada.setFechaAplicacion(dto.getFechaAplicacion());
        vacunaAplicada.setFechaProximaDosis(dto.getFechaProximaDosis());
        vacunaAplicada.setLote(dto.getLote());
        
        vacunaAplicada.setLaboratorio(dto.getLaboratorio() != null && !dto.getLaboratorio().trim().isEmpty() 
                ? dto.getLaboratorio() : vacuna.getLaboratorio());
        
        vacunaAplicada.setVeterinario(dto.getVeterinario());
        vacunaAplicada.setObservaciones(dto.getObservaciones());

        double precio = (dto.getPrecioCobrado() != null) ? dto.getPrecioCobrado() : vacuna.getPrecio();
        vacunaAplicada.setPrecioCobrado(precio);
        vacunaAplicada.setEstado(estadoFinal);

        VacunaAplicada saved = vacunaAplicadaRepository.save(vacunaAplicada);

        // Generar automáticamente la venta pendiente en caja
        ventaService.crearVentaPendiente(
            mascota.getPropietario().getId(), 
            mascota.getId(), 
            "Vacunación: " + vacuna.getNombre(), 
            saved.getId(), 
            "VACUNA", 
            precio
        );

        return saved;
    }

    @Override
    public VacunaAplicada actualizarVacunaAplicada(Long id, VacunaDTO dto) {
        VacunaAplicada vacunaAplicada = vacunaAplicadaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Registro de vacuna aplicada no encontrado con ID: " + id));

        Mascota mascota = mascotaService.obtenerMascotaPorId(dto.getMascotaId())
                .orElseThrow(() -> new IllegalArgumentException("Mascota no encontrada con ID: " + dto.getMascotaId()));

        Vacuna vacuna = vacunaRepository.findById(dto.getVacunaId())
                .orElseThrow(() -> new IllegalArgumentException("Vacuna del catálogo no encontrada con ID: " + dto.getVacunaId()));

        validarFechas(dto.getFechaAplicacion(), dto.getFechaProximaDosis());

        vacunaAplicada.setMascota(mascota);
        vacunaAplicada.setVacuna(vacuna);
        vacunaAplicada.setFechaAplicacion(dto.getFechaAplicacion());
        
        if (!dto.getFechaProximaDosis().equals(vacunaAplicada.getFechaProximaDosis())) {
            vacunaAplicada.setAlerta30DiasEnviada(false);
            vacunaAplicada.setAlerta7DiasEnviada(false);
            vacunaAplicada.setAlertaVencidaEnviada(false);
        }
        
        vacunaAplicada.setFechaProximaDosis(dto.getFechaProximaDosis());
        vacunaAplicada.setLote(dto.getLote());
        vacunaAplicada.setLaboratorio(dto.getLaboratorio() != null && !dto.getLaboratorio().trim().isEmpty() 
                ? dto.getLaboratorio() : vacuna.getLaboratorio());
        vacunaAplicada.setVeterinario(dto.getVeterinario());
        vacunaAplicada.setObservaciones(dto.getObservaciones());

        double precio = (dto.getPrecioCobrado() != null) ? dto.getPrecioCobrado() : vacuna.getPrecio();
        vacunaAplicada.setPrecioCobrado(precio);

        EstadoVacuna nuevoEstado = resolverEstado(dto.getFechaAplicacion(), dto.getFechaProximaDosis(), dto.getEstado());
        vacunaAplicada.setEstado(nuevoEstado);

        return vacunaAplicadaRepository.save(vacunaAplicada);
    }

    @Override
    public void eliminarVacunaAplicada(Long id) {
        if (!vacunaAplicadaRepository.existsById(id)) {
            throw new IllegalArgumentException("Registro de vacuna aplicada no encontrado con ID: " + id);
        }
        vacunaAplicadaRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VacunaAplicada> obtenerVacunaAplicadaPorId(Long id) {
        return vacunaAplicadaRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VacunaAplicada> listarVacunasAplicadas() {
        return vacunaAplicadaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VacunaAplicada> listarVacunasAplicadasPaginado(String nombreMascota, Especie especie, Pageable pageable) {
        return vacunaAplicadaRepository.buscarConFiltros(nombreMascota, especie, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VacunaAplicada> buscarPorMascotaId(Long mascotaId) {
        return vacunaAplicadaRepository.buscarPorMascotaId(mascotaId);
    }

    private void validarFechas(LocalDate aplicacion, LocalDate proximaDosis) {
        if (proximaDosis.isBefore(aplicacion) || proximaDosis.isEqual(aplicacion)) {
            throw new IllegalArgumentException("La fecha de la próxima dosis debe ser posterior a la fecha de aplicación.");
        }
    }

    private EstadoVacuna resolverEstado(LocalDate aplicacion, LocalDate proxima, EstadoVacuna estadoPropuesto) {
        LocalDate hoy = LocalDate.now();
        if (hoy.isAfter(proxima)) {
            return EstadoVacuna.VENCIDA;
        }
        long diasParaVencer = ChronoUnit.DAYS.between(hoy, proxima);
        if (diasParaVencer <= 30) {
            return EstadoVacuna.PROXIMA_A_VENCER;
        }
        if (estadoPropuesto != null) {
            return estadoPropuesto;
        }
        return EstadoVacuna.APLICADA;
    }

    private synchronized String generarSiguienteCodigo() {
        String maxCodigo = vacunaAplicadaRepository.findMaxCodigoVacunaAplicada();
        if (maxCodigo == null || !maxCodigo.startsWith("VAP-")) {
            return "VAP-000001";
        }
        try {
            int numero = Integer.parseInt(maxCodigo.substring(4));
            return String.format("VAP-%06d", numero + 1);
        } catch (NumberFormatException e) {
            return "VAP-000001";
        }
    }
}
