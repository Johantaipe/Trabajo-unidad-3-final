package com.vetexpert.sistema_veterinaria.servicios.service.impl;

import com.vetexpert.sistema_veterinaria.servicios.model.Servicio;
import com.vetexpert.sistema_veterinaria.servicios.repository.ServicioRepository;
import com.vetexpert.sistema_veterinaria.servicios.service.ServicioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ServicioServiceImpl implements ServicioService {

    private final ServicioRepository servicioRepository;

    public ServicioServiceImpl(ServicioRepository servicioRepository) {
        this.servicioRepository = servicioRepository;
    }

    @Override
    public Servicio registrarServicio(Servicio servicio) {
        servicio.setCodigoServicio(generarSiguienteCodigo());
        servicio.setActivo(true);
        return servicioRepository.save(servicio);
    }

    @Override
    public Servicio actualizarServicio(Long id, Servicio datos) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado con ID: " + id));

        servicio.setNombre(datos.getNombre());
        servicio.setPrecioBase(datos.getPrecioBase());
        servicio.setDescripcion(datos.getDescripcion());
        servicio.setActivo(datos.isActivo());

        return servicioRepository.save(servicio);
    }

    @Override
    public void eliminarServicio(Long id) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado con ID: " + id));
        servicio.setActivo(false);
        servicioRepository.save(servicio);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Servicio> obtenerServicioPorId(Long id) {
        return servicioRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Servicio> listarServicios() {
        return servicioRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Servicio> listarServiciosActivos() {
        return servicioRepository.findByActivoTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Servicio> buscarPorNombre(String nombre) {
        return servicioRepository.findByNombreIgnoreCase(nombre);
    }

    private synchronized String generarSiguienteCodigo() {
        String maxCodigo = servicioRepository.findMaxCodigoServicio();
        if (maxCodigo == null || !maxCodigo.startsWith("SERV-")) {
            return "SERV-000001";
        }
        try {
            int numero = Integer.parseInt(maxCodigo.substring(5));
            return String.format("SERV-%06d", numero + 1);
        } catch (NumberFormatException e) {
            return "SERV-000001";
        }
    }
}
