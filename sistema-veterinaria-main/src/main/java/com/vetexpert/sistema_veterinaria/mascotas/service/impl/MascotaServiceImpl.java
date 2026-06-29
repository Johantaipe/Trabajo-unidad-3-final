package com.vetexpert.sistema_veterinaria.mascotas.service.impl;

import com.vetexpert.sistema_veterinaria.mascotas.dto.MascotaDTO;
import com.vetexpert.sistema_veterinaria.mascotas.model.Especie;
import com.vetexpert.sistema_veterinaria.mascotas.model.Mascota;
import com.vetexpert.sistema_veterinaria.mascotas.repository.MascotaRepository;
import com.vetexpert.sistema_veterinaria.mascotas.service.MascotaService;
import com.vetexpert.sistema_veterinaria.propietarios.model.Propietario;
import com.vetexpert.sistema_veterinaria.propietarios.service.PropietarioService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * Implementación transaccional del servicio de Mascotas.
 * Encapsula la lógica de negocio y la manipulación de base de datos.
 */
@Service
@Transactional
public class MascotaServiceImpl implements MascotaService {

    private final MascotaRepository mascotaRepository;
    private final PropietarioService propietarioService;

    public MascotaServiceImpl(MascotaRepository mascotaRepository, PropietarioService propietarioService) {
        this.mascotaRepository = mascotaRepository;
        this.propietarioService = propietarioService;
    }

    @Override
    public Mascota registrarMascota(MascotaDTO dto) {
        Propietario propietario = propietarioService.buscarPorId(dto.getPropietarioId())
                .orElseThrow(() -> new IllegalArgumentException("Propietario no encontrado con ID: " + dto.getPropietarioId()));

        Mascota mascota = new Mascota();
        mascota.setCodigoMascota(generarSiguienteCodigo());
        mascota.setNombre(dto.getNombre());
        mascota.setEspecie(dto.getEspecie());
        mascota.setRaza(dto.getRaza());
        mascota.setSexo(dto.getSexo());
        mascota.setFechaNacimiento(dto.getFechaNacimiento());
        mascota.setPeso(dto.getPeso());
        mascota.setColor(dto.getColor());
        mascota.setEsterilizado(dto.isEsterilizado());
        mascota.setObservaciones(dto.getObservaciones());
        mascota.setFotoUrl(dto.getFotoUrl());
        mascota.setPropietario(propietario);

        return mascotaRepository.save(mascota);
    }

    @Override
    public Mascota actualizarMascota(Long id, MascotaDTO dto) {
        Mascota mascota = mascotaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mascota no encontrada con ID: " + id));

        Propietario propietario = propietarioService.buscarPorId(dto.getPropietarioId())
                .orElseThrow(() -> new IllegalArgumentException("Propietario no encontrado con ID: " + dto.getPropietarioId()));

        mascota.setNombre(dto.getNombre());
        mascota.setEspecie(dto.getEspecie());
        mascota.setRaza(dto.getRaza());
        mascota.setSexo(dto.getSexo());
        mascota.setFechaNacimiento(dto.getFechaNacimiento());
        mascota.setPeso(dto.getPeso());
        mascota.setColor(dto.getColor());
        mascota.setEsterilizado(dto.isEsterilizado());
        mascota.setObservaciones(dto.getObservaciones());
        mascota.setFotoUrl(dto.getFotoUrl());
        mascota.setPropietario(propietario);

        return mascotaRepository.save(mascota);
    }

    @Override
    public void eliminarMascota(Long id) {
        if (!mascotaRepository.existsById(id)) {
            throw new IllegalArgumentException("Mascota no encontrada con ID: " + id);
        }
        mascotaRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Mascota> obtenerMascotaPorId(Long id) {
        return mascotaRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Mascota> listarMascotas() {
        return mascotaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Mascota> listarMascotasPaginado(String nombre, Especie especie, Pageable pageable) {
        return mascotaRepository.buscarConFiltros(nombre, especie, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Mascota> buscarPorNombre(String nombre) {
        return mascotaRepository.buscarPorNombre(nombre);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Mascota> buscarPorPropietario(Long propietarioId) {
        return mascotaRepository.buscarPorPropietario(propietarioId);
    }

    @Override
    public Mascota cambiarEstado(Long id, boolean activo) {
        Mascota mascota = mascotaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mascota no encontrada con ID: " + id));
        mascota.setActivo(activo);
        return mascotaRepository.save(mascota);
    }

    /**
     * Autogenera el siguiente código de mascota de forma secuencial con formato PET-XXXXX.
     * Ejemplo: si el máximo es PET-00003, genera PET-00004.
     */
    private synchronized String generarSiguienteCodigo() {
        String maxCodigo = mascotaRepository.findMaxCodigoMascota();
        if (maxCodigo == null || !maxCodigo.startsWith("PET-")) {
            return "PET-00001";
        }
        try {
            int numero = Integer.parseInt(maxCodigo.substring(4));
            return String.format("PET-%05d", numero + 1);
        } catch (NumberFormatException e) {
            return "PET-00001";
        }
    }
}
