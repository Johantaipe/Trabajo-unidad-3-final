package com.vetexpert.usuarios.service;

import com.vetexpert.usuarios.dto.*;

import java.util.List;

/**
 * Servicio de gestión de usuarios (CRUD, cambio de password, habilitación).
 */
public interface UsuarioService {

    List<UsuarioDTO> listarTodos();

    UsuarioDTO buscarPorId(Long id);

    UsuarioDTO buscarPorUsername(String username);

    UsuarioDTO crearUsuario(UsuarioCreateDTO dto);

    UsuarioDTO actualizarUsuario(Long id, UsuarioUpdateDTO dto);

    ApiResponseDTO cambiarPassword(Long id, CambioPasswordDTO dto);

    ApiResponseDTO toggleEstado(Long id);

    ApiResponseDTO eliminarUsuario(Long id);
}
