package com.vetexpert.sistema_veterinaria.publico.service;

import com.vetexpert.sistema_veterinaria.publico.model.ContactoMensaje;
import java.util.List;
import java.util.Optional;

public interface ContactoMensajeService {
    List<ContactoMensaje> listarTodos();
    Optional<ContactoMensaje> obtenerPorId(Long id);
    ContactoMensaje guardar(ContactoMensaje mensaje);
    ContactoMensaje marcarComoLeido(Long id);
    void eliminar(Long id);
    long contarMensajesNoLeidos();
}
