package com.vetexpert.sistema_veterinaria.caja.controller;

import com.vetexpert.sistema_veterinaria.caja.entity.Venta;
import com.vetexpert.sistema_veterinaria.caja.entity.EstadoPago;
import com.vetexpert.sistema_veterinaria.caja.entity.TipoComprobante;
import com.vetexpert.sistema_veterinaria.caja.service.VentaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/caja")
public class CajaController {

    private final VentaService ventaService;
    private static final String UPLOAD_DIR = "src/main/resources/static/uploads/";

    public CajaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    @GetMapping
    public String listar(Model model, @RequestParam(value = "query", required = false) String query) {
        model.addAttribute("activePage", "caja");
        List<Venta> todas;
        if (query != null && !query.trim().isEmpty()) {
            todas = ventaService.buscarVentas(query);
            model.addAttribute("query", query);
        } else {
            todas = ventaService.listarVentas();
        }

        List<Venta> pendientes = todas.stream()
                .filter(v -> v.getEstadoPago() == EstadoPago.PENDIENTE)
                .toList();

        List<Venta> cobradas = todas.stream()
                .filter(v -> v.getEstadoPago() == EstadoPago.PAGADO)
                .toList();

        model.addAttribute("pendientes", pendientes);
        model.addAttribute("cobradas", cobradas);
        return "caja/lista";
    }

    @GetMapping("/detalle/{id}")
    public String verDetalle(@PathVariable("id") Long id, Model model) {
        Venta venta = ventaService.obtenerVentaPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada con ID: " + id));
        model.addAttribute("activePage", "caja");
        model.addAttribute("venta", venta);
        model.addAttribute("tiposComprobante", TipoComprobante.values());

        // WhatsApp Web link pre-filled template url generation
        String tel = venta.getPropietario().getTelefono();
        if (tel != null) {
            tel = tel.replaceAll("[^0-9]", "");
            if (tel.length() == 9 && !tel.startsWith("51")) {
                tel = "51" + tel;
            }
        } else {
            tel = "";
        }

        StringBuilder waMsg = new StringBuilder();
        waMsg.append("Estimado(a) *").append(venta.getPropietario().getNombre()).append(" ").append(venta.getPropietario().getApellido()).append("*,\n\n");
        waMsg.append("Le saludamos de *VET EXPERTS*. Adjuntamos el resumen de su comprobante:\n\n");
        waMsg.append("• *Nro. Comprobante:* ").append(venta.getCodigoVenta()).append("\n");
        waMsg.append("• *Fecha:* ").append(venta.getFechaVenta().toLocalDate().toString()).append("\n");
        waMsg.append("• *Monto Total:* S/ ").append(String.format("%.2f", venta.getTotal())).append("\n\n");
        waMsg.append("*Resumen de Detalles:*\n");
        for (var det : venta.getDetalles()) {
            waMsg.append("- ").append(det.getConcepto()).append(" (x").append(det.getCantidad()).append(")\n");
        }
        waMsg.append("\n¡Muchas gracias por su preferencia!");

        try {
            String encodedMsg = URLEncoder.encode(waMsg.toString(), StandardCharsets.UTF_8);
            model.addAttribute("whatsappUrl", "https://wa.me/" + tel + "?text=" + encodedMsg);
        } catch (Exception e) {
            model.addAttribute("whatsappUrl", "https://wa.me/" + tel);
        }

        return "caja/detalle";
    }

    @PostMapping("/cobrar/{id}")
    public String cobrar(@PathVariable("id") Long id,
                         @RequestParam("tipoComprobante") String tipoComprobante,
                         @RequestParam("metodoPago") String metodoPago,
                         @RequestParam(value = "codigoOperacion", required = false) String codigoOperacion,
                         @RequestParam(value = "montoPago", required = false) Double montoPago,
                         @RequestParam(value = "evidenciaFile", required = false) MultipartFile file,
                         @RequestParam(value = "observaciones", required = false) String observaciones) {
        
        String evidenciaUrl = null;
        if (file != null && !file.isEmpty()) {
            try {
                String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                Path uploadPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(file.getInputStream(), filePath);
                evidenciaUrl = "/uploads/" + fileName;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            ventaService.registrarCobro(id, tipoComprobante, metodoPago, codigoOperacion, montoPago, evidenciaUrl, observaciones);
        } catch (Exception e) {
            return "redirect:/caja/detalle/" + id + "?error=" + e.getMessage();
        }
        return "redirect:/caja";
    }

    @GetMapping("/anular/{id}")
    public String anular(@PathVariable("id") Long id) {
        ventaService.anularVenta(id);
        return "redirect:/caja";
    }

    @GetMapping("/detalle/{id}/enviar-correo")
    public String enviarCorreoManual(@PathVariable("id") Long id) {
        Venta venta = ventaService.obtenerVentaPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada"));
        ventaService.enviarComprobantePorCorreo(venta);
        return "redirect:/caja/detalle/" + id + "?emailSent=true";
    }

    @GetMapping("/reportes")
    public String reportes(Model model) {
        model.addAttribute("activePage", "reportes");

        List<Venta> todas = ventaService.listarVentas();
        List<Venta> pagadas = todas.stream()
                .filter(v -> v.getEstadoPago() == EstadoPago.PAGADO)
                .toList();

        double totalRecaudado = pagadas.stream().mapToDouble(Venta::getTotal).sum();
        double recaudadoMes = ventaService.listarVentasMes().stream()
                .filter(v -> v.getEstadoPago() == EstadoPago.PAGADO)
                .mapToDouble(Venta::getTotal)
                .sum();
        double recaudadoDia = ventaService.listarVentasDia().stream()
                .filter(v -> v.getEstadoPago() == EstadoPago.PAGADO)
                .mapToDouble(Venta::getTotal)
                .sum();

        // Recaudacion por semana (ultimos 7 dias)
        LocalDateTime startOfWeek = LocalDateTime.of(LocalDate.now().minusDays(7), LocalTime.MIN);
        double recaudadoSemana = pagadas.stream()
                .filter(v -> v.getFechaVenta().isAfter(startOfWeek))
                .mapToDouble(Venta::getTotal)
                .sum();

        // Recaudacion por Metodo de Pago
        double totalEfectivo = pagadas.stream()
                .filter(v -> "EFECTIVO".equalsIgnoreCase(v.getMetodoPago()) || v.getMetodoPago() == null)
                .mapToDouble(Venta::getTotal).sum();
        double totalTarjeta = pagadas.stream()
                .filter(v -> "TARJETA".equalsIgnoreCase(v.getMetodoPago()))
                .mapToDouble(Venta::getTotal).sum();
        double totalTransferencia = pagadas.stream()
                .filter(v -> "TRANSFERENCIA".equalsIgnoreCase(v.getMetodoPago()))
                .mapToDouble(Venta::getTotal).sum();
        double totalYape = pagadas.stream()
                .filter(v -> "YAPE".equalsIgnoreCase(v.getMetodoPago()))
                .mapToDouble(Venta::getTotal).sum();
        double totalPlin = pagadas.stream()
                .filter(v -> "PLIN".equalsIgnoreCase(v.getMetodoPago()))
                .mapToDouble(Venta::getTotal).sum();

        // Agrupar e identificar conceptos más vendidos
        Map<String, Long> countPorConcepto = pagadas.stream()
                .flatMap(v -> v.getDetalles().stream())
                .collect(Collectors.groupingBy(d -> d.getConcepto() + " (" + d.getConceptoTipo() + ")", Collectors.summingLong(d -> d.getCantidad())));

        List<Map.Entry<String, Long>> bestSellers = countPorConcepto.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(10)
                .toList();

        model.addAttribute("totalRecaudado", totalRecaudado);
        model.addAttribute("recaudadoMes", recaudadoMes);
        model.addAttribute("recaudadoSemana", recaudadoSemana);
        model.addAttribute("recaudadoDia", recaudadoDia);

        model.addAttribute("totalEfectivo", totalEfectivo);
        model.addAttribute("totalTarjeta", totalTarjeta);
        model.addAttribute("totalTransferencia", totalTransferencia);
        model.addAttribute("totalYape", totalYape);
        model.addAttribute("totalPlin", totalPlin);

        model.addAttribute("bestSellers", bestSellers);
        model.addAttribute("totalVentas", pagadas.size());

        return "caja/reportes";
    }
}
