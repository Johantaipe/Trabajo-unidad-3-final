package com.vetexpert.sistema_veterinaria.promociones.service.impl;

import com.vetexpert.sistema_veterinaria.promociones.model.Promocion;
import com.vetexpert.sistema_veterinaria.promociones.repository.PromocionRepository;
import com.vetexpert.sistema_veterinaria.promociones.service.PromocionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PromocionServiceImpl implements PromocionService {

    private final PromocionRepository promocionRepository;

    public PromocionServiceImpl(PromocionRepository promocionRepository) {
        this.promocionRepository = promocionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Promocion> listarTodas() {
        return promocionRepository.findByOrderByFechaRegistroDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Promocion> listarPromocionesVigentes() {
        return promocionRepository.findActivePromotions(LocalDate.now());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Promocion> obtenerPorId(Long id) {
        return promocionRepository.findById(id);
    }

    @Override
    public Promocion guardar(Promocion promocion) {
        return promocionRepository.save(promocion);
    }

    @Override
    public Promocion actualizar(Long id, Promocion promocion) {
        Promocion existente = promocionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Promoción no encontrada con ID: " + id));

        existente.setTitulo(promocion.getTitulo());
        existente.setDescripcion(promocion.getDescripcion());
        existente.setDescuento(promocion.getDescuento());
        existente.setImagenUrl(promocion.getImagenUrl());
        existente.setActivo(promocion.isActivo());
        existente.setFechaInicio(promocion.getFechaInicio());
        existente.setFechaFin(promocion.getFechaFin());

        return promocionRepository.save(existente);
    }

    @Override
    public void eliminar(Long id) {
        if (!promocionRepository.existsById(id)) {
            throw new IllegalArgumentException("Promoción no encontrada con ID: " + id);
        }
        promocionRepository.deleteById(id);
    }
}
