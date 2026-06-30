package com.vetexpert.sistema_veterinaria.propietarios.service;

import com.vetexpert.sistema_veterinaria.propietarios.entity.Propietario;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz del servicio de Propietarios.
 * Define el contrato para las operaciones de negocio.
 * Aplica principio de Inversión de Dependencias (SOLID).
 */
public interface PropietarioService {

    /**
     * Lista todos los propietarios registrados.
     */
    List<Propietario> listarTodos();

    /**
     * Busca un propietario por su ID.
     */
    Optional<Propietario> buscarPorId(Long id);

    /**
     * Guarda un nuevo propietario.
     * @throws IllegalArgumentException si el DNI ya existe en la base de datos.
     */
    Propietario guardar(Propietario propietario);

    /**
     * Actualiza un propietario existente.
     * @throws IllegalArgumentException si el DNI ya está en uso por otro propietario.
     */
    Propietario actualizar(Long id, Propietario propietario);

    /**
     * Elimina un propietario por su ID.
     */
    void eliminar(Long id);

    /**
     * Verifica si ya existe un propietario con el DNI dado.
     */
    boolean existePorDni(String dni);

    /**
     * Busca un propietario por su DNI.
     */
    Optional<Propietario> buscarPorDni(String dni);

    /**
     * Busca de manera inteligente por DNI, nombre, apellido o teléfono.
     */
    List<Propietario> buscarInteligente(String query);
}
