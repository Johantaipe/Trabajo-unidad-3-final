package com.vetexpert.sistema_veterinaria.propietarios.service.impl;

import com.vetexpert.sistema_veterinaria.propietarios.entity.Propietario;
import com.vetexpert.sistema_veterinaria.propietarios.repository.PropietarioRepository;
import com.vetexpert.sistema_veterinaria.propietarios.service.PropietarioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio de Propietarios.
 * Contiene la lógica de negocio para operaciones CRUD.
 */
@Service
@Transactional
public class PropietarioServiceImpl implements PropietarioService {

    private final PropietarioRepository propietarioRepository;

    /**
     * Inyección de dependencias por constructor (recomendado por Spring).
     */
    public PropietarioServiceImpl(PropietarioRepository propietarioRepository) {
        this.propietarioRepository = propietarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Propietario> listarTodos() {
        return propietarioRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Propietario> buscarPorId(Long id) {
        return propietarioRepository.findById(id);
    }

    @Override
    public Propietario guardar(Propietario propietario) {
        // Validar unicidad del DNI
        if (propietarioRepository.existsByDni(propietario.getDni())) {
            throw new IllegalArgumentException("Ya existe un propietario con el DNI: " + propietario.getDni());
        }
        return propietarioRepository.save(propietario);
    }

    @Override
    public Propietario actualizar(Long id, Propietario propietario) {
        return propietarioRepository.findById(id)
                .map(existente -> {
                    // Validar que el DNI no esté en uso por otro propietario
                    Optional<Propietario> otroPropietario = propietarioRepository.findByDni(propietario.getDni());
                    if (otroPropietario.isPresent() && !otroPropietario.get().getId().equals(id)) {
                        throw new IllegalArgumentException("Ya existe otro propietario con el DNI: " + propietario.getDni());
                    }

                    existente.setNombre(propietario.getNombre());
                    existente.setApellido(propietario.getApellido());
                    existente.setDni(propietario.getDni());
                    existente.setTelefono(propietario.getTelefono());
                    existente.setCorreo(propietario.getCorreo());
                    existente.setDireccion(propietario.getDireccion());
                    return propietarioRepository.save(existente);
                })
                .orElseThrow(() -> new IllegalArgumentException("Propietario no encontrado con ID: " + id));
    }

    @Override
    public void eliminar(Long id) {
        if (!propietarioRepository.existsById(id)) {
            throw new IllegalArgumentException("Propietario no encontrado con ID: " + id);
        }
        propietarioRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorDni(String dni) {
        return propietarioRepository.existsByDni(dni);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Propietario> buscarPorDni(String dni) {
        return propietarioRepository.findByDni(dni);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Propietario> buscarInteligente(String query) {
        if (query == null || query.trim().isEmpty()) {
            return propietarioRepository.findAll();
        }
        return propietarioRepository.buscarInteligente(query);
    }
}
