package com.vetexpert.sistema_veterinaria.servicios.service;

import com.vetexpert.sistema_veterinaria.servicios.model.Servicio;
import java.util.List;
import java.util.Optional;

public interface ServicioService {
    Servicio registrarServicio(Servicio servicio);
    Servicio actualizarServicio(Long id, Servicio servicio);
    void eliminarServicio(Long id);
    Optional<Servicio> obtenerServicioPorId(Long id);
    List<Servicio> listarServicios();
    List<Servicio> listarServiciosActivos();
    Optional<Servicio> buscarPorNombre(String nombre);
}
