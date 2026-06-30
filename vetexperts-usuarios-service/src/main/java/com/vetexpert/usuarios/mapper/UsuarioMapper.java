package com.vetexpert.usuarios.mapper;

import com.vetexpert.usuarios.dto.UsuarioDTO;
import com.vetexpert.usuarios.entity.Usuario;
import org.springframework.stereotype.Component;

/**
 * Mapper manual para convertir entre entidad Usuario y sus DTOs.
 */
@Component
public class UsuarioMapper {

    public UsuarioDTO toDTO(Usuario usuario) {
        if (usuario == null) return null;
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(usuario.getId());
        dto.setUsername(usuario.getUsername());
        dto.setRol(usuario.getRol());
        dto.setCorreo(usuario.getCorreo());
        dto.setNombre(usuario.getNombre());
        dto.setApellido(usuario.getApellido());
        dto.setTelefono(usuario.getTelefono());
        dto.setDistrito(usuario.getDistrito());
        dto.setFotoUrl(usuario.getFotoUrl());
        dto.setEnabled(usuario.isEnabled());
        dto.setCreatedAt(usuario.getCreatedAt());
        dto.setUpdatedAt(usuario.getUpdatedAt());
        return dto;
    }

    public Usuario toEntity(UsuarioDTO dto) {
        if (dto == null) return null;
        Usuario u = new Usuario();
        u.setId(dto.getId());
        u.setUsername(dto.getUsername());
        u.setRol(dto.getRol());
        u.setCorreo(dto.getCorreo());
        u.setNombre(dto.getNombre());
        u.setApellido(dto.getApellido());
        u.setTelefono(dto.getTelefono());
        u.setDistrito(dto.getDistrito());
        u.setFotoUrl(dto.getFotoUrl());
        u.setEnabled(dto.isEnabled());
        return u;
    }
}
