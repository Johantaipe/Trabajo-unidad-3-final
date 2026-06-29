package com.vetexpert.sistema_veterinaria.inventario.service;

import com.vetexpert.sistema_veterinaria.inventario.model.Producto;
import java.util.List;
import java.util.Optional;

public interface ProductoService {
    Producto registrarProducto(Producto producto);
    Producto actualizarProducto(Long id, Producto producto);
    void eliminarProducto(Long id);
    Optional<Producto> obtenerProductoPorId(Long id);
    List<Producto> listarProductos();
    List<Producto> listarProductosPorCategoria(String categoria);
    List<Producto> buscarProductos(String query);
    
    Producto agregarStock(Long id, Integer cantidad, Long proveedorId, Double costoCompra, String usuario);
    Producto descontarStock(Long id, Integer cantidad, String motivo, String usuario);
    
    List<Producto> listarStockCritico();
    List<Producto> listarProductosPorVencer(Integer dias);
    List<Producto> listarProductosVencidos();
}
