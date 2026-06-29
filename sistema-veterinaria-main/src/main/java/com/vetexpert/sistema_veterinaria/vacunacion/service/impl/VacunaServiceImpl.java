package com.vetexpert.sistema_veterinaria.vacunacion.service.impl;

import com.vetexpert.sistema_veterinaria.vacunacion.model.Vacuna;
import com.vetexpert.sistema_veterinaria.vacunacion.repository.VacunaRepository;
import com.vetexpert.sistema_veterinaria.vacunacion.service.VacunaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio para el catálogo de vacunas.
 */
@Service
@Transactional
public class VacunaServiceImpl implements VacunaService {

    private final VacunaRepository vacunaRepository;

    public VacunaServiceImpl(VacunaRepository vacunaRepository) {
        this.vacunaRepository = vacunaRepository;
    }

    @Override
    public Vacuna registrarVacuna(Vacuna vacuna) {
        vacuna.setCodigoVacuna(generarSiguienteCodigo());
        vacuna.setActivo(true);
        return vacunaRepository.save(vacuna);
    }

    @Override
    public Vacuna actualizarVacuna(Long id, Vacuna datosActualizar) {
        Vacuna vacuna = vacunaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vacuna no encontrada con ID: " + id));
        
        vacuna.setNombre(datosActualizar.getNombre());
        vacuna.setLaboratorio(datosActualizar.getLaboratorio());
        vacuna.setPrecio(datosActualizar.getPrecio());
        vacuna.setDescripcion(datosActualizar.getDescripcion());
        vacuna.setActivo(datosActualizar.isActivo());

        return vacunaRepository.save(vacuna);
    }

    @Override
    public void eliminarVacuna(Long id) {
        if (!vacunaRepository.existsById(id)) {
            throw new IllegalArgumentException("Vacuna no encontrada con ID: " + id);
        }
        vacunaRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Vacuna> obtenerVacunaPorId(Long id) {
        return vacunaRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vacuna> listarVacunas() {
        return vacunaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vacuna> listarVacunasActivas() {
        return vacunaRepository.findByActivoTrue();
    }

    private synchronized String generarSiguienteCodigo() {
        String maxCodigo = vacunaRepository.findMaxCodigoVacuna();
        if (maxCodigo == null || !maxCodigo.startsWith("VAC-")) {
            return "VAC-000001";
        }
        try {
            int numero = Integer.parseInt(maxCodigo.substring(4));
            return String.format("VAC-%06d", numero + 1);
        } catch (NumberFormatException e) {
            return "VAC-000001";
        }
    }
}
