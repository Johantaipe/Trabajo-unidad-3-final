package com.vetexpert.sistema_veterinaria.inventario.service.impl;

import com.vetexpert.sistema_veterinaria.inventario.entity.Proveedor;
import com.vetexpert.sistema_veterinaria.inventario.repository.ProveedorRepository;
import com.vetexpert.sistema_veterinaria.inventario.service.ProveedorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProveedorServiceImpl implements ProveedorService {

    private final ProveedorRepository proveedorRepository;

    public ProveedorServiceImpl(ProveedorRepository proveedorRepository) {
        this.proveedorRepository = proveedorRepository;
    }

    @Override
    public Proveedor registrarProveedor(Proveedor proveedor) {
        if (proveedorRepository.existsByRuc(proveedor.getRuc())) {
            throw new IllegalArgumentException("Ya existe un proveedor registrado con el RUC: " + proveedor.getRuc());
        }
        return proveedorRepository.save(proveedor);
    }

    @Override
    public Proveedor actualizarProveedor(Long id, Proveedor datos) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado con ID: " + id));

        if (!proveedor.getRuc().equals(datos.getRuc()) && proveedorRepository.existsByRuc(datos.getRuc())) {
            throw new IllegalArgumentException("Ya existe otro proveedor registrado con el RUC: " + datos.getRuc());
        }

        proveedor.setRuc(datos.getRuc());
        proveedor.setRazonSocial(datos.getRazonSocial());
        proveedor.setContacto(datos.getContacto());
        proveedor.setTelefono(datos.getTelefono());
        proveedor.setCorreo(datos.getCorreo());
        proveedor.setDireccion(datos.getDireccion());
        proveedor.setProductosSuministrados(datos.getProductosSuministrados());

        return proveedorRepository.save(proveedor);
    }

    @Override
    public void eliminarProveedor(Long id) {
        if (!proveedorRepository.existsById(id)) {
            throw new IllegalArgumentException("Proveedor no encontrado con ID: " + id);
        }
        proveedorRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Proveedor> obtenerProveedorPorId(Long id) {
        return proveedorRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Proveedor> listarProveedores() {
        return proveedorRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Proveedor> buscarProveedores(String query) {
        if (query == null || query.trim().isEmpty()) {
            return proveedorRepository.findAll();
        }
        return proveedorRepository.buscarInteligente(query);
    }
}
