package com.vetexpert.sistema_veterinaria.hospitalizacion.service;

import com.vetexpert.sistema_veterinaria.hospitalizacion.entity.Hospitalizacion;
import java.util.List;
import java.util.Optional;

public interface HospitalizacionService {
    Hospitalizacion registrarHospitalizacion(Hospitalizacion hospitalizacion);
    Hospitalizacion darAlta(Long id, Hospitalizacion datosAlta);
    Optional<Hospitalizacion> obtenerPorId(Long id);
    List<Hospitalizacion> listarTodas();
    List<Hospitalizacion> listarActivas();
    List<Hospitalizacion> buscar(String query);
}
