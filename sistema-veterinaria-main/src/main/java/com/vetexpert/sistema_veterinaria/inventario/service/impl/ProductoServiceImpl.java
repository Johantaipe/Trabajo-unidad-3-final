package com.vetexpert.sistema_veterinaria.inventario.service.impl;

import com.vetexpert.sistema_veterinaria.inventario.model.Producto;
import com.vetexpert.sistema_veterinaria.inventario.model.Proveedor;
import com.vetexpert.sistema_veterinaria.inventario.repository.ProductoRepository;
import com.vetexpert.sistema_veterinaria.inventario.repository.ProveedorRepository;
import com.vetexpert.sistema_veterinaria.inventario.service.ProductoService;
import com.vetexpert.sistema_veterinaria.inventario.service.MovimientoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final ProveedorRepository proveedorRepository;
    private final MovimientoService movimientoService;

    public ProductoServiceImpl(ProductoRepository productoRepository,
                               ProveedorRepository proveedorRepository,
                               MovimientoService movimientoService) {
        this.productoRepository = productoRepository;
        this.proveedorRepository = proveedorRepository;
        this.movimientoService = movimientoService;
    }

    @Override
    public Producto registrarProducto(Producto producto) {
        producto.setCodigoProducto(generarSiguienteCodigo());
        producto.setActivo(true);
        producto.setEstado("ACTIVO");
        Producto saved = productoRepository.save(producto);
        
        // Registrar movimiento inicial de ingreso si hay stock
        if (saved.getStock() > 0) {
            movimientoService.registrarMovimiento(
                "sistema", 
                saved.getId(), 
                "PRODUCTO", 
                saved.getNombre(), 
                saved.getStock(), 
                "INGRESO", 
                "Inventario inicial"
            );
        }
        return saved;
    }

    @Override
    public Producto actualizarProducto(Long id, Producto datos) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + id));

        int stockAnterior = producto.getStock();
        
        producto.setNombre(datos.getNombre());
        producto.setCategoria(datos.getCategoria());
        producto.setDescripcion(datos.getDescripcion());
        producto.setProveedor(datos.getProveedor());
        producto.setPrecioCompra(datos.getPrecioCompra());
        producto.setPrecioVenta(datos.getPrecioVenta());
        producto.setStock(datos.getStock());
        producto.setStockMinimo(datos.getStockMinimo());
        producto.setLote(datos.getLote());
        producto.setFechaVencimiento(datos.getFechaVencimiento());
        producto.setEstado(datos.getEstado());
        producto.setObservaciones(datos.getObservaciones());
        producto.setFotoUrl(datos.getFotoUrl());

        Producto updated = productoRepository.save(producto);

        // Si el stock cambió manualmente en la edición, registrar un movimiento de ajuste
        if (updated.getStock() != stockAnterior) {
            int diff = updated.getStock() - stockAnterior;
            movimientoService.registrarMovimiento(
                "sistema", 
                updated.getId(), 
                "PRODUCTO", 
                updated.getNombre(), 
                diff, 
                "AJUSTE", 
                "Ajuste manual de stock en edición"
            );
        }

        return updated;
    }

    @Override
    public void eliminarProducto(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + id));
        producto.setActivo(false);
        productoRepository.save(producto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Producto> obtenerProductoPorId(Long id) {
        return productoRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> listarProductos() {
        return productoRepository.findByActivoTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> listarProductosPorCategoria(String categoria) {
        return productoRepository.findByCategoriaAndActivoTrue(categoria);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> buscarProductos(String query) {
        if (query == null || query.trim().isEmpty()) {
            return listarProductos();
        }
        return productoRepository.buscarGlobal(query);
    }

    @Override
    public Producto agregarStock(Long id, Integer cantidad, Long proveedorId, Double costoCompra, String usuario) {
        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a agregar debe ser mayor a cero");
        }
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + id));

        if (proveedorId != null) {
            Proveedor prov = proveedorRepository.findById(proveedorId)
                    .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado con ID: " + proveedorId));
            producto.setProveedor(prov);
        }

        if (costoCompra != null && costoCompra > 0) {
            producto.setPrecioCompra(costoCompra);
        }

        producto.setStock(producto.getStock() + cantidad);
        Producto saved = productoRepository.save(producto);

        // Registrar movimiento
        movimientoService.registrarMovimiento(
            usuario, 
            saved.getId(), 
            "PRODUCTO", 
            saved.getNombre(), 
            cantidad, 
            "INGRESO", 
            "Entrada por compra / reabastecimiento"
        );

        return saved;
    }

    @Override
    public Producto descontarStock(Long id, Integer cantidad, String motivo, String usuario) {
        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a descontar debe ser mayor a cero");
        }
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + id));

        if (producto.getStock() < cantidad) {
            throw new IllegalArgumentException("Stock insuficiente");
        }

        producto.setStock(producto.getStock() - cantidad);
        Producto saved = productoRepository.save(producto);

        // Registrar movimiento
        movimientoService.registrarMovimiento(
            usuario, 
            saved.getId(), 
            "PRODUCTO", 
            saved.getNombre(), 
            -cantidad, 
            motivo, 
            "Descuento automático o consumo de stock"
        );

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> listarStockCritico() {
        return productoRepository.findStockCritico();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> listarProductosPorVencer(Integer dias) {
        LocalDate limite = LocalDate.now().plusDays(dias);
        return productoRepository.findProductosPorVencer(limite);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> listarProductosVencidos() {
        return productoRepository.findProductosVencidos(LocalDate.now());
    }

    private synchronized String generarSiguienteCodigo() {
        String maxCodigo = productoRepository.findMaxCodigoProducto();
        if (maxCodigo == null || !maxCodigo.startsWith("PROD-")) {
            return "PROD-000001";
        }
        try {
            int numero = Integer.parseInt(maxCodigo.substring(5));
            return String.format("PROD-%06d", numero + 1);
        } catch (NumberFormatException e) {
            return "PROD-000001";
        }
    }
}
