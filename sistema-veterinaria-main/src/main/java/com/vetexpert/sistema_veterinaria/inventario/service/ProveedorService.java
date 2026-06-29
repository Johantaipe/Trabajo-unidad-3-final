package com.vetexpert.sistema_veterinaria.inventario.service;

import com.vetexpert.sistema_veterinaria.inventario.model.Proveedor;
import java.util.List;
import java.util.Optional;

public interface ProveedorService {
    Proveedor registrarProveedor(Proveedor proveedor);
    Proveedor actualizarProveedor(Long id, Proveedor proveedor);
    void eliminarProveedor(Long id);
    Optional<Proveedor> obtenerProveedorPorId(Long id);
    List<Proveedor> listarProveedores();
    List<Proveedor> buscarProveedores(String query);
}
