package com.vetexpert.sistema_veterinaria.inventario.service;

import com.vetexpert.sistema_veterinaria.inventario.entity.Movimiento;
import java.util.List;

public interface MovimientoService {
    Movimiento registrarMovimiento(String usuario, Long productoId, String productoTipo, String productoNombre, Integer cantidad, String tipoMovimiento, String motivo);
    List<Movimiento> listarMovimientos();
    List<Movimiento> listarMovimientosDeProducto(Long productoId, String productoTipo);
}
