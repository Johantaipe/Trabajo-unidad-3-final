package com.vetexpert.sistema_veterinaria.vacunacion.service.impl;

import com.vetexpert.sistema_veterinaria.vacunacion.dto.DesparasitacionDTO;
import com.vetexpert.sistema_veterinaria.vacunacion.model.EstadoVacuna;
import com.vetexpert.sistema_veterinaria.vacunacion.model.Desparasitacion;
import com.vetexpert.sistema_veterinaria.vacunacion.repository.DesparasitacionRepository;
import com.vetexpert.sistema_veterinaria.vacunacion.service.DesparasitacionService;
import com.vetexpert.sistema_veterinaria.mascotas.model.Especie;
import com.vetexpert.sistema_veterinaria.mascotas.model.Mascota;
import com.vetexpert.sistema_veterinaria.mascotas.service.MascotaService;
import com.vetexpert.sistema_veterinaria.caja.service.VentaService;
import com.vetexpert.sistema_veterinaria.inventario.service.ProductoService;
import com.vetexpert.sistema_veterinaria.inventario.model.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * Implementación transaccional para el servicio de Desparasitaciones.
 */
@Service
@Transactional
public class DesparasitacionServiceImpl implements DesparasitacionService {

    private final DesparasitacionRepository desparasitacionRepository;
    private final MascotaService mascotaService;
    private final VentaService ventaService;
    private final ProductoService productoService;

    public DesparasitacionServiceImpl(DesparasitacionRepository desparasitacionRepository,
                                      MascotaService mascotaService,
                                      VentaService ventaService,
                                      ProductoService productoService) {
        this.desparasitacionRepository = desparasitacionRepository;
        this.mascotaService = mascotaService;
        this.ventaService = ventaService;
        this.productoService = productoService;
    }

    @Override
    public Desparasitacion registrarDesparasitacion(DesparasitacionDTO dto) {
        Mascota mascota = mascotaService.obtenerMascotaPorId(dto.getMascotaId())
                .orElseThrow(() -> new IllegalArgumentException("Mascota no encontrada con ID: " + dto.getMascotaId()));

        validarFechas(dto.getFechaAplicacion(), dto.getFechaProximaAplicacion());

        Desparasitacion desp = new Desparasitacion();
        desp.setCodigoDesparasitacion(generarSiguienteCodigo());
        desp.setMascota(mascota);
        desp.setTipo(dto.getTipo());
        desp.setProducto(dto.getProducto());
        desp.setFechaAplicacion(dto.getFechaAplicacion());
        desp.setFechaProximaAplicacion(dto.getFechaProximaAplicacion());
        desp.setVeterinario(dto.getVeterinario());
        desp.setObservaciones(dto.getObservaciones());
        desp.setPrecio(dto.getPrecio());

        desp.setEstado(resolverEstado(dto.getFechaAplicacion(), dto.getFechaProximaAplicacion(), dto.getEstado()));

        Desparasitacion saved = desparasitacionRepository.save(desp);

        // Generar venta pendiente en caja si se marca como APLICADA
        if (saved.getEstado() == EstadoVacuna.APLICADA || saved.getEstado() == EstadoVacuna.PROXIMA_A_VENCER) {
            ventaService.crearVentaPendiente(
                mascota.getPropietario().getId(),
                mascota.getId(),
                "Desparasitación: " + saved.getProducto() + " (" + saved.getTipo() + ")",
                saved.getId(),
                "DESPARASITACION",
                saved.getPrecio()
            );

            // Descontar del stock del inventario si el producto existe
            if (saved.getProducto() != null && !saved.getProducto().trim().isEmpty()) {
                List<Producto> matchingProducts = productoService.buscarProductos(saved.getProducto());
                for (Producto prod : matchingProducts) {
                    if (prod.getNombre().toLowerCase().contains(saved.getProducto().toLowerCase().trim()) 
                        || saved.getProducto().toLowerCase().trim().contains(prod.getNombre().toLowerCase())) {
                        if (prod.getStock() >= 1) {
                            productoService.descontarStock(prod.getId(), 1, "DESPARASITACION", "sistema");
                        }
                        break;
                    }
                }
            }
        }

        return saved;
    }

    @Override
    public Desparasitacion actualizarDesparasitacion(Long id, DesparasitacionDTO dto) {
        Desparasitacion desp = desparasitacionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Desparasitación no encontrada con ID: " + id));

        Mascota mascota = mascotaService.obtenerMascotaPorId(dto.getMascotaId())
                .orElseThrow(() -> new IllegalArgumentException("Mascota no encontrada con ID: " + dto.getMascotaId()));

        validarFechas(dto.getFechaAplicacion(), dto.getFechaProximaAplicacion());

        desp.setMascota(mascota);
        desp.setTipo(dto.getTipo());
        desp.setProducto(dto.getProducto());
        desp.setFechaAplicacion(dto.getFechaAplicacion());
        desp.setFechaProximaAplicacion(dto.getFechaProximaAplicacion());
        desp.setVeterinario(dto.getVeterinario());
        desp.setObservaciones(dto.getObservaciones());
        desp.setPrecio(dto.getPrecio());

        desp.setEstado(resolverEstado(dto.getFechaAplicacion(), dto.getFechaProximaAplicacion(), dto.getEstado()));

        return desparasitacionRepository.save(desp);
    }

    @Override
    public void eliminarDesparasitacion(Long id) {
        if (!desparasitacionRepository.existsById(id)) {
            throw new IllegalArgumentException("Desparasitación no encontrada con ID: " + id);
        }
        desparasitacionRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Desparasitacion> obtenerDesparasitacionPorId(Long id) {
        return desparasitacionRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Desparasitacion> listarDesparasitaciones() {
        return desparasitacionRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Desparasitacion> listarDesparasitacionesPaginado(String nombreMascota, Especie especie, Pageable pageable) {
        return desparasitacionRepository.buscarConFiltros(nombreMascota, especie, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Desparasitacion> buscarPorMascotaId(Long mascotaId) {
        return desparasitacionRepository.buscarPorMascotaId(mascotaId);
    }

    private void validarFechas(LocalDate aplicacion, LocalDate proxima) {
        if (proxima.isBefore(aplicacion) || proxima.isEqual(aplicacion)) {
            throw new IllegalArgumentException("La fecha de la próxima aplicación debe ser posterior a la fecha de aplicación.");
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
        String maxCodigo = desparasitacionRepository.findMaxCodigoDesparasitacion();
        if (maxCodigo == null || !maxCodigo.startsWith("DES-")) {
            return "DES-000001";
        }
        try {
            int numero = Integer.parseInt(maxCodigo.substring(4));
            return String.format("DES-%06d", numero + 1);
        } catch (NumberFormatException e) {
            return "DES-000001";
        }
    }
}
