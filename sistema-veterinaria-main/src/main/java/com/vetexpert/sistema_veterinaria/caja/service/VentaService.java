package com.vetexpert.sistema_veterinaria.caja.service;

import com.vetexpert.sistema_veterinaria.caja.entity.Venta;
import java.util.List;
import java.util.Optional;

public interface VentaService {
    Venta registrarVenta(Venta venta);
    
    Venta crearVentaPendiente(Long propietarioId, Long mascotaId, String concepto, Long conceptoId, String conceptoTipo, Double total);
    
    Venta registrarCobro(Long id, String tipoComprobante, String metodoPago, String codigoOperacion, Double montoPago, String evidenciaUrl, String observaciones);
    
    Venta anularVenta(Long id);
    
    void enviarComprobantePorCorreo(Venta venta);
    
    Optional<Venta> obtenerVentaPorId(Long id);
    
    List<Venta> listarVentas();
    
    List<Venta> buscarVentas(String query);
    
    List<Venta> listarVentasDia();
    
    List<Venta> listarVentasMes();

    boolean existeVentaParaConcepto(Long conceptoId, String conceptoTipo);
}
