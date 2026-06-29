package com.vetexpert.sistema_veterinaria.promociones.service;

import com.vetexpert.sistema_veterinaria.promociones.model.Promocion;
import java.util.List;
import java.util.Optional;

public interface PromocionService {
    List<Promocion> listarTodas();
    List<Promocion> listarPromocionesVigentes();
    Optional<Promocion> obtenerPorId(Long id);
    Promocion guardar(Promocion promocion);
    Promocion actualizar(Long id, Promocion promocion);
    void eliminar(Long id);
}
