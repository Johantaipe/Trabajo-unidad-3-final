package com.vetexpert.sistema_veterinaria.caja.service.impl;

import com.vetexpert.sistema_veterinaria.caja.entity.Venta;
import com.vetexpert.sistema_veterinaria.caja.entity.DetalleVenta;
import com.vetexpert.sistema_veterinaria.caja.entity.EstadoPago;
import com.vetexpert.sistema_veterinaria.caja.entity.TipoComprobante;
import com.vetexpert.sistema_veterinaria.caja.repository.VentaRepository;
import com.vetexpert.sistema_veterinaria.caja.service.VentaService;
import com.vetexpert.sistema_veterinaria.propietarios.entity.Propietario;
import com.vetexpert.sistema_veterinaria.propietarios.repository.PropietarioRepository;
import com.vetexpert.sistema_veterinaria.mascotas.entity.Mascota;
import com.vetexpert.sistema_veterinaria.mascotas.repository.MascotaRepository;
import com.vetexpert.sistema_veterinaria.inventario.entity.Producto;
import com.vetexpert.sistema_veterinaria.inventario.service.ProductoService;
import com.vetexpert.sistema_veterinaria.vacunas.entity.Vacuna;
import com.vetexpert.sistema_veterinaria.vacunas.repository.VacunaRepository;
import com.vetexpert.sistema_veterinaria.inventario.service.MovimientoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class VentaServiceImpl implements VentaService {

    private final VentaRepository ventaRepository;
    private final PropietarioRepository propietarioRepository;
    private final MascotaRepository mascotaRepository;
    private final ProductoService productoService;
    private final VacunaRepository vacunaRepository;
    private final MovimientoService movimientoService;

    public VentaServiceImpl(VentaRepository ventaRepository,
                            PropietarioRepository propietarioRepository,
                            MascotaRepository mascotaRepository,
                            ProductoService productoService,
                            VacunaRepository vacunaRepository,
                            MovimientoService movimientoService) {
        this.ventaRepository = ventaRepository;
        this.propietarioRepository = propietarioRepository;
        this.mascotaRepository = mascotaRepository;
        this.productoService = productoService;
        this.vacunaRepository = vacunaRepository;
        this.movimientoService = movimientoService;
    }

    @Override
    public Venta registrarVenta(Venta venta) {
        venta.setCodigoVenta(generarSiguienteCodigo());
        venta.setFechaVenta(LocalDateTime.now());
        
        // Recalcular montos
        double total = 0.0;
        for (DetalleVenta det : venta.getDetalles()) {
            det.setVenta(venta);
            total += det.getSubtotal();
        }
        venta.setTotal(total);
        venta.setSubtotal(total / 1.18);
        venta.setIgv(total - venta.getSubtotal());

        Venta saved = ventaRepository.save(venta);

        // Si se marca como PAGADO al registrar, procesar deducción de stock
        if (saved.getEstadoPago() == EstadoPago.PAGADO) {
            descontarStockDeDetalles(saved);
        }

        return saved;
    }

    @Override
    public Venta crearVentaPendiente(Long propietarioId, Long mascotaId, String concepto, Long conceptoId, String conceptoTipo, Double total) {
        Propietario propietario = propietarioRepository.findById(propietarioId)
                .orElseThrow(() -> new IllegalArgumentException("Propietario no encontrado con ID: " + propietarioId));
        
        Mascota mascota = null;
        if (mascotaId != null) {
            mascota = mascotaRepository.findById(mascotaId).orElse(null);
        }

        Venta venta = new Venta();
        venta.setCodigoVenta(generarSiguienteCodigo());
        venta.setFechaVenta(LocalDateTime.now());
        venta.setPropietario(propietario);
        venta.setMascota(mascota);
        venta.setTotal(total);
        venta.setSubtotal(total / 1.18);
        venta.setIgv(total - venta.getSubtotal());
        venta.setEstadoPago(EstadoPago.PENDIENTE);
        venta.setTipoComprobante(TipoComprobante.NOTA_VENTA);

        DetalleVenta detalle = new DetalleVenta(concepto, conceptoId, conceptoTipo, 1, total);
        venta.agregarDetalle(detalle);

        return ventaRepository.save(venta);
    }

    @Override
    public Venta registrarCobro(Long id, String tipoComprobante, String metodoPago, String codigoOperacion, Double montoPago, String evidenciaUrl, String observaciones) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada con ID: " + id));

        if (venta.getEstadoPago() == EstadoPago.PAGADO) {
            throw new IllegalArgumentException("Esta venta ya ha sido cobrada.");
        }

        venta.setEstadoPago(EstadoPago.PAGADO);
        venta.setTipoComprobante(TipoComprobante.valueOf(tipoComprobante));
        venta.setMetodoPago(metodoPago);
        venta.setCodigoOperacion(codigoOperacion);
        venta.setMontoPago(montoPago != null ? montoPago : venta.getTotal());
        venta.setEvidenciaUrl(evidenciaUrl);

        if (observaciones != null && !observaciones.trim().isEmpty()) {
            venta.setObservaciones(observaciones);
        }
        venta.setFechaVenta(LocalDateTime.now());

        Venta saved = ventaRepository.save(venta);
        descontarStockDeDetalles(saved);

        // Enviar comprobante por correo si el cliente tiene correo
        try {
            enviarComprobantePorCorreo(saved);
        } catch (Exception e) {
            System.err.println("Error enviando comprobante por correo: " + e.getMessage());
        }

        return saved;
    }

    @Override
    public Venta anularVenta(Long id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada con ID: " + id));

        venta.setEstadoPago(EstadoPago.ANULADO);
        return ventaRepository.save(venta);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Venta> obtenerVentaPorId(Long id) {
        return ventaRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Venta> listarVentas() {
        return ventaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Venta> buscarVentas(String query) {
        if (query == null || query.trim().isEmpty()) {
            return ventaRepository.findAll();
        }
        return ventaRepository.buscarCaja(query);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Venta> listarVentasDia() {
        LocalDateTime start = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        return ventaRepository.findByFechaVentaBetweenOrderByFechaVentaDesc(start, end);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Venta> listarVentasMes() {
        LocalDateTime start = LocalDateTime.of(LocalDate.now().withDayOfMonth(1), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        return ventaRepository.findByFechaVentaBetweenOrderByFechaVentaDesc(start, end);
    }

    private void descontarStockDeDetalles(Venta venta) {
        for (DetalleVenta det : venta.getDetalles()) {
            if ("PRODUCTO".equalsIgnoreCase(det.getConceptoTipo())) {
                productoService.descontarStock(det.getConceptoId(), det.getCantidad(), "VENTA", "cajero");
            } else if ("VACUNA".equalsIgnoreCase(det.getConceptoTipo())) {
                Vacuna vac = vacunaRepository.findById(det.getConceptoId())
                        .orElseThrow(() -> new IllegalArgumentException("Vacuna no encontrada en el catálogo: ID " + det.getConceptoId()));
                if (vac.getStock() < det.getCantidad()) {
                    throw new IllegalArgumentException("Stock insuficiente para la vacuna: " + vac.getNombre());
                }
                vac.setStock(vac.getStock() - det.getCantidad());
                vacunaRepository.save(vac);
                
                // Registrar movimiento automático
                movimientoService.registrarMovimiento(
                    "cajero", 
                    vac.getId(), 
                    "VACUNA", 
                    vac.getNombre(), 
                    -det.getCantidad(), 
                    "VENTA", 
                    "Descuento por venta de dosis de vacuna"
                );
            }
        }
    }

    private synchronized String generarSiguienteCodigo() {
        String maxCodigo = ventaRepository.findMaxCodigoVenta();
        if (maxCodigo == null || !maxCodigo.startsWith("VENT-")) {
            return "VENT-000001";
        }
        try {
            int numero = Integer.parseInt(maxCodigo.substring(5));
            return String.format("VENT-%06d", numero + 1);
        } catch (NumberFormatException e) {
            return "VENT-000001";
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeVentaParaConcepto(Long conceptoId, String conceptoTipo) {
        return ventaRepository.existeVentaParaConcepto(conceptoId, conceptoTipo);
    }

    @Override
    public void enviarComprobantePorCorreo(Venta venta) {
        if (venta.getPropietario().getCorreo() != null && !venta.getPropietario().getCorreo().trim().isEmpty()) {
            String codigo = venta.getCodigoVenta();
            String fileName = "comprobante_" + codigo + ".pdf";
            java.nio.file.Path folder = java.nio.file.Paths.get("src/main/resources/static/uploads/comprobantes/");
            try {
                java.nio.file.Files.createDirectories(folder);
                java.nio.file.Path file = folder.resolve(fileName);
                
                StringBuilder sb = new StringBuilder();
                sb.append("=========================================\n");
                sb.append("         COMPROBANTE ELECTRÓNICO         \n");
                sb.append("               VET EXPERTS               \n");
                sb.append("=========================================\n");
                sb.append("Código Venta: ").append(venta.getCodigoVenta()).append("\n");
                sb.append("Fecha Venta: ").append(venta.getFechaVenta().toString()).append("\n");
                sb.append("Cliente: ").append(venta.getPropietario().getNombre()).append(" ").append(venta.getPropietario().getApellido()).append("\n");
                sb.append("DNI: ").append(venta.getPropietario().getDni()).append("\n");
                if (venta.getMascota() != null) {
                    sb.append("Mascota: ").append(venta.getMascota().getNombre()).append(" (").append(venta.getMascota().getEspecie().name()).append(")\n");
                }
                sb.append("Método de Pago: ").append(venta.getMetodoPago() != null ? venta.getMetodoPago() : "EFECTIVO").append("\n");
                if (venta.getCodigoOperacion() != null) {
                    sb.append("Cod. Operación: ").append(venta.getCodigoOperacion()).append("\n");
                }
                sb.append("-----------------------------------------\n");
                sb.append("Detalles:\n");
                for (DetalleVenta det : venta.getDetalles()) {
                    sb.append("- ").append(det.getConcepto()).append(" x").append(det.getCantidad()).append(" : S/ ").append(det.getSubtotal()).append("\n");
                }
                sb.append("-----------------------------------------\n");
                sb.append("Subtotal: S/ ").append(String.format("%.2f", venta.getSubtotal())).append("\n");
                sb.append("IGV (18%): S/ ").append(String.format("%.2f", venta.getIgv())).append("\n");
                sb.append("TOTAL: S/ ").append(String.format("%.2f", venta.getTotal())).append("\n");
                sb.append("=========================================\n");
                
                java.nio.file.Files.writeString(file, sb.toString());
                System.out.println(">>> [CORREO SIMULADO VET EXPERTS] Comprobante " + codigo + " enviado a " + venta.getPropietario().getCorreo() + " - Adjunto: " + file.toAbsolutePath());
            } catch (Exception e) {
                System.err.println("Error simulando PDF o enviando correo: " + e.getMessage());
            }
        }
    }
}
