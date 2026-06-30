package com.vetexpert.sistema_veterinaria.inventario.service.impl;

import com.vetexpert.sistema_veterinaria.inventario.entity.Movimiento;
import com.vetexpert.sistema_veterinaria.inventario.repository.MovimientoRepository;
import com.vetexpert.sistema_veterinaria.inventario.service.MovimientoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class MovimientoServiceImpl implements MovimientoService {

    private final MovimientoRepository movimientoRepository;

    public MovimientoServiceImpl(MovimientoRepository movimientoRepository) {
        this.movimientoRepository = movimientoRepository;
    }

    @Override
    public Movimiento registrarMovimiento(String usuario, Long productoId, String productoTipo, String productoNombre, Integer cantidad, String tipoMovimiento, String motivo) {
        Movimiento mov = new Movimiento(usuario, productoId, productoTipo, productoNombre, cantidad, tipoMovimiento, motivo);
        return movimientoRepository.save(mov);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Movimiento> listarMovimientos() {
        return movimientoRepository.findUltimosMovimientos();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Movimiento> listarMovimientosDeProducto(Long productoId, String productoTipo) {
        return movimientoRepository.findByProductoIdAndProductoTipoOrderByFechaDesc(productoId, productoTipo);
    }
}
