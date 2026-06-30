package com.vetexpert.sistema_veterinaria.hospitalizacion.service.impl;

import com.vetexpert.sistema_veterinaria.hospitalizacion.entity.Hospitalizacion;
import com.vetexpert.sistema_veterinaria.hospitalizacion.repository.HospitalizacionRepository;
import com.vetexpert.sistema_veterinaria.hospitalizacion.service.HospitalizacionService;
import com.vetexpert.sistema_veterinaria.caja.service.VentaService;
import com.vetexpert.sistema_veterinaria.inventario.service.ProductoService;
import com.vetexpert.sistema_veterinaria.inventario.entity.Producto;
import com.vetexpert.sistema_veterinaria.mascotas.entity.Mascota;
import com.vetexpert.sistema_veterinaria.mascotas.repository.MascotaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class HospitalizacionServiceImpl implements HospitalizacionService {

    private final HospitalizacionRepository hospitalizacionRepository;
    private final VentaService ventaService;
    private final ProductoService productoService;
    private final MascotaRepository mascotaRepository;

    public HospitalizacionServiceImpl(HospitalizacionRepository hospitalizacionRepository,
                                      VentaService ventaService,
                                      ProductoService productoService,
                                      MascotaRepository mascotaRepository) {
        this.hospitalizacionRepository = hospitalizacionRepository;
        this.ventaService = ventaService;
        this.productoService = productoService;
        this.mascotaRepository = mascotaRepository;
    }

    @Override
    public Hospitalizacion registrarHospitalizacion(Hospitalizacion hospitalizacion) {
        hospitalizacion.setCodigoHospitalizacion(generarSiguienteCodigo());
        hospitalizacion.setEstado("INGRESADO");
        if (hospitalizacion.getMascota() != null && hospitalizacion.getMascota().getId() != null) {
            Mascota mascota = mascotaRepository.findById(hospitalizacion.getMascota().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Mascota no encontrada"));
            hospitalizacion.setMascota(mascota);
        }
        return hospitalizacionRepository.save(hospitalizacion);
    }

    @Override
    public Hospitalizacion darAlta(Long id, Hospitalizacion datosAlta) {
        Hospitalizacion hosp = hospitalizacionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Hospitalización no encontrada con ID: " + id));

        if ("ALTA".equalsIgnoreCase(hosp.getEstado())) {
            throw new IllegalArgumentException("Esta hospitalización ya está en estado ALTA.");
        }

        hosp.setEstado("ALTA");
        hosp.setFechaAlta(LocalDate.now());
        hosp.setDiagnostico(datosAlta.getDiagnostico());
        hosp.setMedicamentos(datosAlta.getMedicamentos());
        hosp.setObservaciones(datosAlta.getObservaciones());

        Hospitalizacion saved = hospitalizacionRepository.save(hosp);

        // 1. Calcular días transcurridos para facturación
        long dias = ChronoUnit.DAYS.between(saved.getFechaIngreso(), saved.getFechaAlta());
        if (dias <= 0) {
            dias = 1; // Mínimo 1 día de cobro
        }
        double totalCobrar = dias * saved.getPrecioPorDia();

        // 2. Generar venta pendiente en caja
        ventaService.crearVentaPendiente(
            saved.getMascota().getPropietario().getId(),
            saved.getMascota().getId(),
            "Hospitalización: " + dias + " día(s) de internamiento (" + saved.getCodigoHospitalizacion() + ")",
            saved.getId(),
            "SERVICIO",
            totalCobrar
        );

        // 3. Descontar stock de medicamentos utilizados
        String medicamentosTxt = saved.getMedicamentos();
        if (medicamentosTxt != null && !medicamentosTxt.trim().isEmpty()) {
            List<Producto> productosActivos = productoService.listarProductos();
            for (Producto prod : productosActivos) {
                if (medicamentosTxt.toLowerCase().contains(prod.getNombre().toLowerCase())) {
                    if (prod.getStock() >= 1) {
                        productoService.descontarStock(prod.getId(), 1, "HOSPITALIZACION", "sistema");
                    }
                }
            }
        }

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Hospitalizacion> obtenerPorId(Long id) {
        return hospitalizacionRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Hospitalizacion> listarTodas() {
        return hospitalizacionRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Hospitalizacion> listarActivas() {
        return hospitalizacionRepository.findByEstado("INGRESADO");
    }

    @Override
    @Transactional(readOnly = true)
    public List<Hospitalizacion> buscar(String query) {
        if (query == null || query.trim().isEmpty()) {
            return listarTodas();
        }
        return hospitalizacionRepository.buscarGlobal(query);
    }

    private synchronized String generarSiguienteCodigo() {
        String maxCodigo = hospitalizacionRepository.findMaxCodigoHospitalizacion();
        if (maxCodigo == null || !maxCodigo.startsWith("HOSP-")) {
            return "HOSP-000001";
        }
        try {
            int numero = Integer.parseInt(maxCodigo.substring(5));
            return String.format("HOSP-%06d", numero + 1);
        } catch (NumberFormatException e) {
            return "HOSP-000001";
        }
    }
}
