package com.vetexpert.sistema_veterinaria.usuarios.controller;

import com.vetexpert.sistema_veterinaria.usuarios.entity.Usuario;
import com.vetexpert.sistema_veterinaria.usuarios.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
    public String listarUsuarios(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String rol,
            @RequestParam(required = false) Boolean estado,
            Model model,
            HttpSession session) {
        
        Object loginUser = session.getAttribute("usuario");
        String loginRol = (String) session.getAttribute("rol");
        if (loginUser == null || !"ADMIN".equals(loginRol)) {
            return "redirect:/403";
        }

        List<Usuario> usuarios = usuarioRepository.findAll();

        // Excluir clientes de este listado administrativo de personal
        usuarios = usuarios.stream()
                .filter(u -> !"CLIENTE".equalsIgnoreCase(u.getRol()))
                .collect(java.util.stream.Collectors.toList());

        // Búsqueda por Nombre (nombre/apellido), Correo, Teléfono
        if (search != null && !search.trim().isEmpty()) {
            String query = search.trim().toLowerCase();
            usuarios = usuarios.stream()
                .filter(u -> (u.getNombre() != null && u.getNombre().toLowerCase().contains(query)) ||
                             (u.getApellido() != null && u.getApellido().toLowerCase().contains(query)) ||
                             (u.getCorreo() != null && u.getCorreo().toLowerCase().contains(query)) ||
                             (u.getTelefono() != null && u.getTelefono().contains(query)))
                .collect(java.util.stream.Collectors.toList());
        }

        // Filtro Rol
        if (rol != null && !rol.trim().isEmpty() && !rol.equalsIgnoreCase("TODOS")) {
            usuarios = usuarios.stream()
                .filter(u -> u.getRol() != null && u.getRol().equalsIgnoreCase(rol.trim()))
                .collect(java.util.stream.Collectors.toList());
        }

        // Filtro Estado
        if (estado != null) {
            usuarios = usuarios.stream()
                .filter(u -> u.isEnabled() == estado)
                .collect(java.util.stream.Collectors.toList());
        }

        // Orden por defecto: Nuevos primero
        usuarios.sort((u1, u2) -> {
            java.time.LocalDateTime f1 = u1.getFechaCreacion();
            java.time.LocalDateTime f2 = u2.getFechaCreacion();
            if (f1 == null && f2 == null) return 0;
            if (f1 == null) return 1;
            if (f2 == null) return -1;
            return f2.compareTo(f1);
        });

        model.addAttribute("usuarios", usuarios);
        model.addAttribute("search", search);
        model.addAttribute("rol", rol);
        model.addAttribute("estado", estado);
        model.addAttribute("activePage", "usuarios");
        return "usuarios/lista";
    }

    // ==========================================
    // NUEVO USUARIO
    // ==========================================
    @GetMapping("/usuarios/nuevo")
    public String mostrarFormularioNuevo(Model model, HttpSession session) {
        Object loginUser = session.getAttribute("usuario");
        String loginRol = (String) session.getAttribute("rol");
        if (loginUser == null || !"ADMIN".equals(loginRol)) {
            return "redirect:/403";
        }
        
        Usuario usuario = new Usuario();
        usuario.setEnabled(true);
        model.addAttribute("usuario", usuario);
        model.addAttribute("roles", List.of("ADMIN", "SECRETARIA", "VETERINARIO"));
        model.addAttribute("activePage", "usuarios");
        return "usuarios/formulario";
    }

    @PostMapping("/usuarios/nuevo")
    public String guardarNuevoUsuario(
            @ModelAttribute("usuario") Usuario usuario,
            @RequestParam(value = "confirmPassword", required = false) String confirmPassword,
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Object loginUser = session.getAttribute("usuario");
        String loginRol = (String) session.getAttribute("rol");
        if (loginUser == null || !"ADMIN".equals(loginRol)) {
            return "redirect:/403";
        }

        boolean hasErrors = false;
        model.addAttribute("roles", List.of("ADMIN", "SECRETARIA", "VETERINARIO"));
        model.addAttribute("activePage", "usuarios");

        if (usuario.getNombre() == null || usuario.getNombre().trim().isEmpty()) {
            model.addAttribute("errorNombre", "El nombre es obligatorio");
            hasErrors = true;
        }
        if (usuario.getApellido() == null || usuario.getApellido().trim().isEmpty()) {
            model.addAttribute("errorApellido", "El apellido es obligatorio");
            hasErrors = true;
        }

        // Correo obligatorio y único
        if (usuario.getCorreo() == null || usuario.getCorreo().trim().isEmpty()) {
            model.addAttribute("errorCorreo", "El correo electrónico es obligatorio");
            hasErrors = true;
        } else if (!usuario.getCorreo().trim().matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
            model.addAttribute("errorCorreo", "Ingrese un correo electrónico válido");
            hasErrors = true;
        } else if (usuarioRepository.existsByCorreo(usuario.getCorreo().trim())) {
            model.addAttribute("errorCorreo", "El correo electrónico ya se encuentra registrado");
            hasErrors = true;
        }

        // Usuario (username) único
        if (usuario.getUsername() == null || usuario.getUsername().trim().isEmpty()) {
            model.addAttribute("errorUsername", "El nombre de usuario es obligatorio");
            hasErrors = true;
        } else if (usuarioRepository.existsByUsername(usuario.getUsername().trim())) {
            model.addAttribute("errorUsername", "El usuario ya se encuentra registrado");
            hasErrors = true;
        }

        // Teléfono
        if (usuario.getTelefono() == null || usuario.getTelefono().trim().isEmpty()) {
            model.addAttribute("errorTelefono", "El teléfono es obligatorio");
            hasErrors = true;
        } else if (!usuario.getTelefono().trim().matches("^9[0-9]{8}$")) {
            model.addAttribute("errorTelefono", "El teléfono debe iniciar con 9 y tener exactamente 9 dígitos");
            hasErrors = true;
        }

        // DNI único si aplica
        if (usuario.getDni() != null && !usuario.getDni().trim().isEmpty()) {
            String cleanDni = usuario.getDni().trim();
            if (!cleanDni.matches("^[0-9]{8}$")) {
                model.addAttribute("errorDni", "El DNI debe tener exactamente 8 dígitos numéricos");
                hasErrors = true;
            } else if (usuarioRepository.existsByDni(cleanDni)) {
                model.addAttribute("errorDni", "El DNI ya se encuentra registrado");
                hasErrors = true;
            }
        }

        // Contraseña segura
        if (usuario.getPassword() == null || usuario.getPassword().isEmpty()) {
            model.addAttribute("errorPassword", "La contraseña es obligatoria");
            hasErrors = true;
        } else if (!usuario.getPassword().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&._\\-#/])[A-Za-z\\d@$!%*?&._\\-#/]{8,}$")) {
            model.addAttribute("errorPassword", "Debe tener al menos 8 caracteres, una mayúscula, una minúscula, un número y un carácter especial");
            hasErrors = true;
        }

        if (confirmPassword == null || !confirmPassword.equals(usuario.getPassword())) {
            model.addAttribute("errorConfirmPassword", "Las contraseñas no coinciden");
            hasErrors = true;
        }

        if (hasErrors) {
            return "usuarios/formulario";
        }

        // Encriptar
        String hashed = BCrypt.hashpw(usuario.getPassword(), BCrypt.gensalt());
        usuario.setPassword(hashed);
        
        usuario.setNombre(usuario.getNombre().trim());
        usuario.setApellido(usuario.getApellido().trim());
        usuario.setCorreo(usuario.getCorreo().trim());
        usuario.setUsername(usuario.getUsername().trim());
        usuario.setTelefono(usuario.getTelefono().trim());
        if (usuario.getDni() != null) {
            usuario.setDni(usuario.getDni().trim());
        }

        usuarioRepository.save(usuario);

        redirectAttributes.addFlashAttribute("mensajeExito", "Usuario creado correctamente");
        return "redirect:/usuarios";
    }

    // ==========================================
    // EDITAR USUARIO
    // ==========================================
    @GetMapping("/usuarios/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable("id") Long id, Model model, HttpSession session) {
        Object loginUser = session.getAttribute("usuario");
        String loginRol = (String) session.getAttribute("rol");
        if (loginUser == null || !"ADMIN".equals(loginRol)) {
            return "redirect:/403";
        }

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
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Object loginUser = session.getAttribute("usuario");
        String loginRol = (String) session.getAttribute("rol");
        if (loginUser == null || !"ADMIN".equals(loginRol)) {
            return "redirect:/403";
        }

        Usuario usuarioDb = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));

        boolean hasErrors = false;
        model.addAttribute("roles", List.of("ADMIN", "SECRETARIA", "VETERINARIO"));
        model.addAttribute("activePage", "usuarios");

        if (usuarioForm.getNombre() == null || usuarioForm.getNombre().trim().isEmpty()) {
            model.addAttribute("errorNombre", "El nombre es obligatorio");
            hasErrors = true;
        }
        if (usuarioForm.getApellido() == null || usuarioForm.getApellido().trim().isEmpty()) {
            model.addAttribute("errorApellido", "El apellido es obligatorio");
            hasErrors = true;
        }

        // Correo obligatorio y único
        if (usuarioForm.getCorreo() == null || usuarioForm.getCorreo().trim().isEmpty()) {
            model.addAttribute("errorCorreo", "El correo electrónico es obligatorio");
            hasErrors = true;
        } else if (!usuarioForm.getCorreo().trim().matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
            model.addAttribute("errorCorreo", "Ingrese un correo electrónico válido");
            hasErrors = true;
        } else {
            Optional<Usuario> existing = usuarioRepository.findByCorreo(usuarioForm.getCorreo().trim());
            if (existing.isPresent() && !existing.get().getId().equals(id)) {
                model.addAttribute("errorCorreo", "El correo electrónico ya está registrado por otro usuario");
                hasErrors = true;
            }
        }

        // Usuario (username) único
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

        // Teléfono
        if (usuarioForm.getTelefono() == null || usuarioForm.getTelefono().trim().isEmpty()) {
            model.addAttribute("errorTelefono", "El teléfono es obligatorio");
            hasErrors = true;
        } else if (!usuarioForm.getTelefono().trim().matches("^9[0-9]{8}$")) {
            model.addAttribute("errorTelefono", "El teléfono debe iniciar con 9 y tener exactamente 9 dígitos");
            hasErrors = true;
        }

        // DNI único si aplica
        if (usuarioForm.getDni() != null && !usuarioForm.getDni().trim().isEmpty()) {
            String cleanDni = usuarioForm.getDni().trim();
            if (!cleanDni.matches("^[0-9]{8}$")) {
                model.addAttribute("errorDni", "El DNI debe tener exactamente 8 dígitos numéricos");
                hasErrors = true;
            } else {
                Optional<Usuario> existing = usuarioRepository.findByDni(cleanDni);
                if (existing.isPresent() && !existing.get().getId().equals(id)) {
                    model.addAttribute("errorDni", "El DNI ya está registrado por otro usuario");
                    hasErrors = true;
                }
            }
        }

        if (hasErrors) {
            usuarioForm.setId(id);
            return "usuarios/formulario";
        }

        // Actualizar datos
        usuarioDb.setNombre(usuarioForm.getNombre().trim());
        usuarioDb.setApellido(usuarioForm.getApellido().trim());
        usuarioDb.setCorreo(usuarioForm.getCorreo().trim());
        usuarioDb.setUsername(usuarioForm.getUsername().trim());
        usuarioDb.setRol(usuarioForm.getRol());
        usuarioDb.setTelefono(usuarioForm.getTelefono().trim());
        usuarioDb.setDni(usuarioForm.getDni() != null ? usuarioForm.getDni().trim() : null);
        usuarioDb.setEnabled(usuarioForm.isEnabled());

        usuarioRepository.save(usuarioDb);
        redirectAttributes.addFlashAttribute("mensajeExito", "Usuario actualizado correctamente");
        return "redirect:/usuarios";
    }

    // ==========================================
    // CAMBIAR ROL IMMEDIATAMENTE
    // ==========================================
    @GetMapping("/usuarios/cambiar-rol/{id}")
    public String cambiarRolUsuario(
            @PathVariable("id") Long id,
            @RequestParam("nuevoRol") String nuevoRol,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        Object loginUser = session.getAttribute("usuario");
        String loginRol = (String) session.getAttribute("rol");
        if (loginUser == null || !"ADMIN".equals(loginRol)) {
            return "redirect:/403";
        }

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));
        usuario.setRol(nuevoRol);
        usuarioRepository.save(usuario);

        redirectAttributes.addFlashAttribute("mensajeExito", "El rol del usuario " + usuario.getUsername() + " fue actualizado a " + nuevoRol + " con éxito.");
        return "redirect:/usuarios";
    }

    // ==========================================
    // ACTIVAR/DESACTIVAR USUARIO
    // ==========================================
    @GetMapping("/usuarios/toggle/{id}")
    public String toggleEstadoUsuario(@PathVariable("id") Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        Object loginUser = session.getAttribute("usuario");
        String loginRol = (String) session.getAttribute("rol");
        if (loginUser == null || !"ADMIN".equals(loginRol)) {
            return "redirect:/403";
        }

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));

        usuario.setEnabled(!usuario.isEnabled());
        usuarioRepository.save(usuario);

        String estado = usuario.isEnabled() ? "reactivado" : "desactivado";
        redirectAttributes.addFlashAttribute("mensajeExito", "El usuario " + usuario.getUsername() + " fue " + estado + " con éxito.");
        return "redirect:/usuarios";
    }

    // ==========================================
    // RESET CONTRASEÑA
    // ==========================================
    @GetMapping("/usuarios/reset-password/{id}")
    public String resetPasswordUsuario(@PathVariable("id") Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        Object loginUser = session.getAttribute("usuario");
        String loginRol = (String) session.getAttribute("rol");
        if (loginUser == null || !"ADMIN".equals(loginRol)) {
            return "redirect:/403";
        }

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));

        // Generar contraseña temporal segura: Vet + 4 digitos + @
        String tempPass = "Vet" + String.format("%04d", (int) (Math.random() * 10000)) + "@";
        usuario.setPassword(BCrypt.hashpw(tempPass, BCrypt.gensalt()));
        usuarioRepository.save(usuario);

        redirectAttributes.addFlashAttribute("mensajeExito", "Contraseña restablecida con éxito para " + usuario.getUsername() + ". Nueva contraseña temporal: " + tempPass);
        return "redirect:/usuarios";
    }

    // ==========================================
    // VISTA DE EXPLICACIÓN DE ROLES
    // ==========================================
    @GetMapping("/usuarios/roles")
    public String mostrarRolesExplicacion(Model model, HttpSession session) {
        Object loginUser = session.getAttribute("usuario");
        String loginRol = (String) session.getAttribute("rol");
        if (loginUser == null || !"ADMIN".equals(loginRol)) {
            return "redirect:/403";
        }
        
        model.addAttribute("activePage", "usuarios");
        return "usuarios/roles";
    }
}
