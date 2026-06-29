package com.vetexpert.sistema_veterinaria.auth.controller;

import com.vetexpert.sistema_veterinaria.auth.model.Usuario;
import com.vetexpert.sistema_veterinaria.auth.repository.UsuarioRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;

    public UsuarioController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // ==========================================
    // LISTADO DE USUARIOS
    // ==========================================
    @GetMapping("/usuarios")
    public String listarUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioRepository.findAll());
        model.addAttribute("activePage", "usuarios");
        return "usuarios/lista";
    }

    // ==========================================
    // NUEVO USUARIO
    // ==========================================
    @GetMapping("/usuarios/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("roles", List.of("ADMIN", "SECRETARIA", "VETERINARIO"));
        model.addAttribute("activePage", "usuarios");
        return "usuarios/formulario";
    }

    @PostMapping("/usuarios/nuevo")
    public String guardarNuevoUsuario(
            @ModelAttribute("usuario") Usuario usuario,
            @RequestParam(value = "confirmPassword", required = false) String confirmPassword,
            Model model,
            RedirectAttributes redirectAttributes) {

        boolean hasErrors = false;
        model.addAttribute("roles", List.of("ADMIN", "SECRETARIA", "VETERINARIO"));
        model.addAttribute("activePage", "usuarios");

        // Validaciones manuales
        if (usuario.getNombre() == null || usuario.getNombre().trim().isEmpty()) {
            model.addAttribute("errorNombre", "El nombre es obligatorio");
            hasErrors = true;
        }
        if (usuario.getApellido() == null || usuario.getApellido().trim().isEmpty()) {
            model.addAttribute("errorApellido", "El apellido es obligatorio");
            hasErrors = true;
        }
        if (usuario.getCorreo() == null || usuario.getCorreo().trim().isEmpty()) {
            model.addAttribute("errorCorreo", "El correo electrónico es obligatorio");
            hasErrors = true;
        } else if (usuarioRepository.existsByCorreo(usuario.getCorreo().trim())) {
            model.addAttribute("errorCorreo", "El correo electrónico ya se encuentra registrado");
            hasErrors = true;
        }
        if (usuario.getUsername() == null || usuario.getUsername().trim().isEmpty()) {
            model.addAttribute("errorUsername", "El usuario es obligatorio");
            hasErrors = true;
        } else if (usuarioRepository.existsByUsername(usuario.getUsername().trim())) {
            model.addAttribute("errorUsername", "El usuario ya se encuentra registrado");
            hasErrors = true;
        }
        if (usuario.getPassword() == null || usuario.getPassword().isEmpty()) {
            model.addAttribute("errorPassword", "La contraseña es obligatoria");
            hasErrors = true;
        } else if (usuario.getPassword().length() < 6) {
            model.addAttribute("errorPassword", "Debe tener al menos 6 caracteres");
            hasErrors = true;
        }
        if (confirmPassword == null || !confirmPassword.equals(usuario.getPassword())) {
            model.addAttribute("errorConfirmPassword", "Las contraseñas no coinciden");
            hasErrors = true;
        }

        if (hasErrors) {
            return "usuarios/formulario";
        }

        // Hashear password y guardar
        String hashed = BCrypt.hashpw(usuario.getPassword(), BCrypt.gensalt());
        usuario.setPassword(hashed);
        usuario.setEnabled(true); // Activo por defecto al crearse
        usuarioRepository.save(usuario);

        redirectAttributes.addFlashAttribute("mensajeExito", "Usuario creado correctamente");
        return "redirect:/usuarios";
    }

    // ==========================================
    // EDITAR USUARIO
    // ==========================================
    @GetMapping("/usuarios/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable("id") Long id, Model model) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));
        model.addAttribute("usuario", usuario);
        model.addAttribute("roles", List.of("ADMIN", "SECRETARIA", "VETERINARIO"));
        model.addAttribute("activePage", "usuarios");
        return "usuarios/formulario";
    }

    @PostMapping("/usuarios/editar/{id}")
    public String guardarEditarUsuario(
            @PathVariable("id") Long id,
            @ModelAttribute("usuario") Usuario usuarioForm,
            Model model,
            RedirectAttributes redirectAttributes) {

        Usuario usuarioDb = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));

        boolean hasErrors = false;
        model.addAttribute("roles", List.of("ADMIN", "SECRETARIA", "VETERINARIO"));
        model.addAttribute("activePage", "usuarios");

        // Validaciones manuales
        if (usuarioForm.getNombre() == null || usuarioForm.getNombre().trim().isEmpty()) {
            model.addAttribute("errorNombre", "El nombre es obligatorio");
            hasErrors = true;
        }
        if (usuarioForm.getApellido() == null || usuarioForm.getApellido().trim().isEmpty()) {
            model.addAttribute("errorApellido", "El apellido es obligatorio");
            hasErrors = true;
        }
        if (usuarioForm.getCorreo() == null || usuarioForm.getCorreo().trim().isEmpty()) {
            model.addAttribute("errorCorreo", "El correo electrónico es obligatorio");
            hasErrors = true;
        } else {
            Optional<Usuario> existing = usuarioRepository.findByCorreo(usuarioForm.getCorreo().trim());
            if (existing.isPresent() && !existing.get().getId().equals(id)) {
                model.addAttribute("errorCorreo", "El correo electrónico ya está registrado por otro usuario");
                hasErrors = true;
            }
        }
        if (usuarioForm.getUsername() == null || usuarioForm.getUsername().trim().isEmpty()) {
            model.addAttribute("errorUsername", "El usuario es obligatorio");
            hasErrors = true;
        } else {
            Optional<Usuario> existing = usuarioRepository.findByUsername(usuarioForm.getUsername().trim());
            if (existing.isPresent() && !existing.get().getId().equals(id)) {
                model.addAttribute("errorUsername", "El usuario ya está registrado por otro usuario");
                hasErrors = true;
            }
        }

        if (hasErrors) {
            usuarioForm.setId(id); // mantener ID para el enlace del formulario
            return "usuarios/formulario";
        }

        // Actualizar datos
        usuarioDb.setNombre(usuarioForm.getNombre().trim());
        usuarioDb.setApellido(usuarioForm.getApellido().trim());
        usuarioDb.setCorreo(usuarioForm.getCorreo().trim());
        usuarioDb.setUsername(usuarioForm.getUsername().trim());
        usuarioDb.setRol(usuarioForm.getRol());

        usuarioRepository.save(usuarioDb);
        redirectAttributes.addFlashAttribute("mensajeExito", "Usuario actualizado correctamente");
        return "redirect:/usuarios";
    }

    // ==========================================
    // ACTIVAR/DESACTIVAR USUARIO
    // ==========================================
    @GetMapping("/usuarios/toggle/{id}")
    public String toggleEstadoUsuario(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));

        usuario.setEnabled(!usuario.isEnabled());
        usuarioRepository.save(usuario);

        String estado = usuario.isEnabled() ? "reactivado" : "desactivado";
        redirectAttributes.addFlashAttribute("mensajeExito", "El usuario " + usuario.getUsername() + " fue " + estado + " con éxito.");
        return "redirect:/usuarios";
    }

    // ==========================================
    // VISTA DE EXPLICACIÓN DE ROLES
    // ==========================================
    @GetMapping("/usuarios/roles")
    public String mostrarRolesExplicacion(Model model) {
        model.addAttribute("activePage", "usuarios");
        return "usuarios/roles";
    }

    // ==========================================
    // RESTABLECER CONTRASEÑA
    // ==========================================
    @GetMapping("/usuarios/restablecer-password/{id}")
    public String mostrarFormRestablecer(@PathVariable("id") Long id, Model model) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));
        model.addAttribute("usuario", usuario);
        model.addAttribute("activePage", "usuarios");
        return "usuarios/restablecer";
    }

    @PostMapping("/usuarios/restablecer-password/{id}")
    public String restablecerPasswordUsuario(
            @PathVariable("id") Long id,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model,
            RedirectAttributes redirectAttributes) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));

        boolean hasErrors = false;
        model.addAttribute("usuario", usuario);
        model.addAttribute("activePage", "usuarios");

        if (password == null || password.isEmpty()) {
            model.addAttribute("errorPassword", "La contraseña es obligatoria");
            hasErrors = true;
        } else if (password.length() < 6) {
            model.addAttribute("errorPassword", "La contraseña debe tener al menos 6 caracteres");
            hasErrors = true;
        }
        if (confirmPassword == null || !confirmPassword.equals(password)) {
            model.addAttribute("errorConfirmPassword", "Las contraseñas no coinciden");
            hasErrors = true;
        }

        if (hasErrors) {
            return "usuarios/restablecer";
        }

        // Guardar contraseña hasheada
        String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
        usuario.setPassword(hashed);
        usuarioRepository.save(usuario);

        redirectAttributes.addFlashAttribute("mensajeExito", "Contraseña del usuario " + usuario.getUsername() + " restablecida con éxito.");
        return "redirect:/usuarios";
    }
}
