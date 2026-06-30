package com.vetexpert.sistema_veterinaria.portal.service.impl;

import com.vetexpert.sistema_veterinaria.portal.entity.ContactoMensaje;
import com.vetexpert.sistema_veterinaria.portal.repository.ContactoMensajeRepository;
import com.vetexpert.sistema_veterinaria.portal.service.ContactoMensajeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ContactoMensajeServiceImpl implements ContactoMensajeService {

    private final ContactoMensajeRepository repository;

    public ContactoMensajeServiceImpl(ContactoMensajeRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContactoMensaje> listarTodos() {
        return repository.findByOrderByFechaEnvioDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ContactoMensaje> obtenerPorId(Long id) {
        return repository.findById(id);
    }

    @Override
    public ContactoMensaje guardar(ContactoMensaje mensaje) {
        return repository.save(mensaje);
    }

    @Override
    public ContactoMensaje marcarComoLeido(Long id) {
        ContactoMensaje mensaje = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mensaje no encontrado con ID: " + id));
        mensaje.setLeido(true);
        return repository.save(mensaje);
    }

    @Override
    public void eliminar(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Mensaje no encontrado con ID: " + id);
        }
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarMensajesNoLeidos() {
        return repository.countByLeido(false);
    }
}
