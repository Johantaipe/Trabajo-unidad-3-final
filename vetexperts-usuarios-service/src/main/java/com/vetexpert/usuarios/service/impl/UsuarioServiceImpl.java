package com.vetexpert.usuarios.service.impl;

import com.vetexpert.usuarios.dto.*;
import com.vetexpert.usuarios.entity.Usuario;
import com.vetexpert.usuarios.exception.DuplicateResourceException;
import com.vetexpert.usuarios.exception.InvalidPasswordException;
import com.vetexpert.usuarios.exception.ResourceNotFoundException;
import com.vetexpert.usuarios.mapper.UsuarioMapper;
import com.vetexpert.usuarios.repository.UsuarioRepository;
import com.vetexpert.usuarios.service.UsuarioService;
import com.vetexpert.usuarios.validator.UsuarioValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de gestión de usuarios.
 */
@Service
public class UsuarioServiceImpl implements UsuarioService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioServiceImpl.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioMapper mapper;
    private final UsuarioValidator validator;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository,
                              PasswordEncoder passwordEncoder,
                              UsuarioMapper mapper,
                              UsuarioValidator validator) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.mapper = mapper;
        this.validator = validator;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioDTO> listarTodos() {
        return usuarioRepository.findAll()
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioDTO buscarPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
        return mapper.toDTO(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioDTO buscarPorUsername(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
        return mapper.toDTO(usuario);
    }

    @Override
    @Transactional
    public UsuarioDTO crearUsuario(UsuarioCreateDTO dto) {
        // Validaciones
        validator.validarUsername(dto.getUsername());
        validator.validarRol(dto.getRol());
        validator.validarPasswordsCoinciden(dto.getPassword(), dto.getConfirmPassword());

        if (usuarioRepository.existsByUsername(dto.getUsername())) {
            throw new DuplicateResourceException("Ya existe un usuario con el username: " + dto.getUsername());
        }
        if (usuarioRepository.existsByCorreo(dto.getCorreo())) {
            throw new DuplicateResourceException("Ya existe un usuario con el correo: " + dto.getCorreo());
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(dto.getUsername());
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setRol(dto.getRol().toUpperCase());
        usuario.setCorreo(dto.getCorreo());
        usuario.setNombre(dto.getNombre());
        usuario.setApellido(dto.getApellido());
        usuario.setTelefono(dto.getTelefono());
        usuario.setEnabled(true);

        Usuario saved = usuarioRepository.save(usuario);
        log.info("Usuario creado: {} con rol {}", saved.getUsername(), saved.getRol());

        return mapper.toDTO(saved);
    }

    @Override
    @Transactional
    public UsuarioDTO actualizarUsuario(Long id, UsuarioUpdateDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));

        // Verificar correo no duplicado si cambió
        if (!usuario.getCorreo().equals(dto.getCorreo()) &&
                usuarioRepository.existsByCorreo(dto.getCorreo())) {
            throw new DuplicateResourceException("Ya existe un usuario con el correo: " + dto.getCorreo());
        }

        // Verificar username no duplicado si cambió y fue proporcionado
        if (dto.getUsername() != null && !dto.getUsername().isBlank() &&
                !usuario.getUsername().equals(dto.getUsername())) {
            validator.validarUsername(dto.getUsername());
            if (usuarioRepository.existsByUsername(dto.getUsername())) {
                throw new DuplicateResourceException("Ya existe un usuario con el username: " + dto.getUsername());
            }
            usuario.setUsername(dto.getUsername());
        }

        if (dto.getRol() != null && !dto.getRol().isBlank()) {
            validator.validarRol(dto.getRol());
            usuario.setRol(dto.getRol().toUpperCase());
        }

        usuario.setNombre(dto.getNombre());
        usuario.setApellido(dto.getApellido());
        usuario.setCorreo(dto.getCorreo());
        usuario.setTelefono(dto.getTelefono());
        usuario.setDistrito(dto.getDistrito());

        Usuario updated = usuarioRepository.save(usuario);
        log.info("Usuario actualizado: {}", updated.getUsername());

        return mapper.toDTO(updated);
    }

    @Override
    @Transactional
    public ApiResponseDTO cambiarPassword(Long id, CambioPasswordDTO dto) {
        validator.validarPasswordsCoinciden(dto.getPasswordNueva(), dto.getConfirmPasswordNueva());

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));

        if (!passwordEncoder.matches(dto.getPasswordActual(), usuario.getPassword())) {
            throw new InvalidPasswordException("La contraseña actual es incorrecta");
        }

        usuario.setPassword(passwordEncoder.encode(dto.getPasswordNueva()));
        usuarioRepository.save(usuario);

        log.info("Contraseña cambiada para usuario: {}", usuario.getUsername());

        return ApiResponseDTO.ok("Contraseña actualizada exitosamente");
    }

    @Override
    @Transactional
    public ApiResponseDTO toggleEstado(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));

        usuario.setEnabled(!usuario.isEnabled());
        usuarioRepository.save(usuario);

        String estado = usuario.isEnabled() ? "habilitado" : "deshabilitado";
        log.info("Usuario {} {}", usuario.getUsername(), estado);

        return ApiResponseDTO.ok("Usuario " + estado + " exitosamente");
    }

    @Override
    @Transactional
    public ApiResponseDTO eliminarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));

        usuarioRepository.delete(usuario);
        log.info("Usuario eliminado: {}", usuario.getUsername());

        return ApiResponseDTO.ok("Usuario eliminado exitosamente");
    }
}
