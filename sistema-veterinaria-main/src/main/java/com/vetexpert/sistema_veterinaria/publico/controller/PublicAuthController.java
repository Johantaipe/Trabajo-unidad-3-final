package com.vetexpert.sistema_veterinaria.publico.controller;

import com.vetexpert.sistema_veterinaria.auth.model.Usuario;
import com.vetexpert.sistema_veterinaria.auth.service.AuthService;
import com.vetexpert.sistema_veterinaria.auth.service.EmailService;
import com.vetexpert.sistema_veterinaria.auth.service.EmailVerificationService;
import com.vetexpert.sistema_veterinaria.auth.service.WhatsAppOtpService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
public class PublicAuthController {

    private final AuthService authService;
    private final EmailService emailService;
    private final EmailVerificationService emailVerificationService;
    private final WhatsAppOtpService whatsAppOtpService;

    public PublicAuthController(AuthService authService, 
                                EmailService emailService, 
                                EmailVerificationService emailVerificationService,
                                WhatsAppOtpService whatsAppOtpService) {
        this.authService = authService;
        this.emailService = emailService;
        this.emailVerificationService = emailVerificationService;
        this.whatsAppOtpService = whatsAppOtpService;
    }

    /**
     * Muestra la vista de Login del Cliente
     */
    @GetMapping("/login-cliente")
    public String mostrarLoginCliente(HttpSession session) {
        if (session.getAttribute("usuario") != null) {
            return "redirect:/";
        }
        return "auth/login-cliente";
    }

    /**
     * Procesa el inicio de sesión del cliente
     */
    @PostMapping("/login-cliente")
    public String procesarLoginCliente(@RequestParam(required = false) String email,
                                       @RequestParam(required = false) String password,
                                       Model model,
                                       HttpSession session,
                                       RedirectAttributes redirectAttributes) {
        if (email == null || email.trim().isEmpty()) {
            model.addAttribute("errorEmail", "El correo electrónico es obligatorio");
            return "auth/login-cliente";
        }
        if (password == null || password.isEmpty()) {
            model.addAttribute("errorPassword", "La contraseña es obligatoria");
            model.addAttribute("email", email);
            return "auth/login-cliente";
        }

        // Intentar autenticar al usuario
        Optional<Usuario> usuarioOpt = authService.autenticar(email.trim(), password);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            if (!"CLIENTE".equals(usuario.getRol())) {
                model.addAttribute("errorEmail", "No tiene permisos para acceder a este portal");
                model.addAttribute("email", email);
                return "auth/login-cliente";
            }

            session.setAttribute("usuario", usuario);
            session.setAttribute("username", usuario.getUsername());
            session.setAttribute("rol", usuario.getRol());
            session.setMaxInactiveInterval(1800); // 30 minutos

            redirectAttributes.addFlashAttribute("mensajeExito", "¡Bienvenido de vuelta, " + usuario.getNombre() + "!");
            return "redirect:/portal-cliente/cargando";
        } else {
            model.addAttribute("errorPassword", "Credenciales incorrectas o cuenta no activa");
            model.addAttribute("email", email);
            return "auth/login-cliente";
        }
    }

    /**
     * Muestra la vista de Registro del Cliente
     */
    @GetMapping("/registro-cliente")
    public String mostrarRegistroCliente(HttpSession session) {
        if (session.getAttribute("usuario") != null) {
            return "redirect:/";
        }
        return "auth/registro-cliente";
    }

    /**
     * Muestra la pantalla de éxito al registrar la cuenta
     */
    @GetMapping("/registro-exito")
    public String mostrarRegistroExito() {
        return "auth/registro-exito";
    }

    /**
     * Procesa el formulario de registro y realiza validaciones rigurosas
     */
    @PostMapping("/registro-cliente")
    public String procesarRegistroCliente(@RequestParam(required = false) String nombre,
                                          @RequestParam(required = false) String apellidoPaterno,
                                          @RequestParam(required = false) String apellidoMaterno,
                                          @RequestParam(required = false) String dni,
                                          @RequestParam(required = false) String direccion,
                                          @RequestParam(required = false) String email,
                                          @RequestParam(required = false) String telefono,
                                          @RequestParam(required = false) String distrito,
                                          @RequestParam(required = false) String password,
                                          @RequestParam(required = false) String confirmPassword,
                                          @RequestParam(required = false, defaultValue = "false") Boolean aceptoTerminos,
                                          Model model,
                                          HttpSession session,
                                          RedirectAttributes redirectAttributes) {

        boolean hasErrors = false;

        // Mantener campos rellenados en caso de error
        model.addAttribute("nombre", nombre);
        model.addAttribute("apellidoPaterno", apellidoPaterno);
        model.addAttribute("apellidoMaterno", apellidoMaterno);
        model.addAttribute("dni", dni);
        model.addAttribute("direccion", direccion);
        model.addAttribute("email", email);
        model.addAttribute("telefono", telefono);
        model.addAttribute("distrito", distrito);
        model.addAttribute("aceptoTerminos", aceptoTerminos);

        // 1. Nombres: Solo letras
        if (nombre == null || nombre.trim().isEmpty()) {
            model.addAttribute("errorNombre", "El nombre es obligatorio");
            hasErrors = true;
        } else if (!nombre.trim().matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s]+$")) {
            model.addAttribute("errorNombre", "El nombre solo debe contener letras");
            hasErrors = true;
        }

        // 2. Apellido Paterno: Solo letras
        if (apellidoPaterno == null || apellidoPaterno.trim().isEmpty()) {
            model.addAttribute("errorApellidoPaterno", "El apellido paterno es obligatorio");
            hasErrors = true;
        } else if (!apellidoPaterno.trim().matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s]+$")) {
            model.addAttribute("errorApellidoPaterno", "El apellido paterno solo debe contener letras");
            hasErrors = true;
        }

        // 3. Apellido Materno: Solo letras
        if (apellidoMaterno == null || apellidoMaterno.trim().isEmpty()) {
            model.addAttribute("errorApellidoMaterno", "El apellido materno es obligatorio");
            hasErrors = true;
        } else if (!apellidoMaterno.trim().matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s]+$")) {
            model.addAttribute("errorApellidoMaterno", "El apellido materno solo debe contener letras");
            hasErrors = true;
        }

        // 4. DNI (Opcional): Si se ingresa, debe ser de 8 dígitos y no estar registrado
        if (dni != null && !dni.trim().isEmpty()) {
            if (!dni.trim().matches("^[0-9]{8}$")) {
                model.addAttribute("errorDni", "El DNI debe tener exactamente 8 dígitos numéricos");
                hasErrors = true;
            } else if (authService.existePropietarioPorDni(dni.trim())) {
                model.addAttribute("errorDni", "El DNI ingresado ya se encuentra registrado");
                hasErrors = true;
            }
        }

        // 5. Correo: Formato válido y único
        if (email == null || email.trim().isEmpty()) {
            model.addAttribute("errorEmail", "El correo es obligatorio");
            hasErrors = true;
        } else if (!email.trim().matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
            model.addAttribute("errorEmail", "Ingrese un correo electrónico válido");
            hasErrors = true;
        } else if (authService.existePorUsername(email.trim())) {
            model.addAttribute("errorEmail", "El correo ya se encuentra registrado");
            hasErrors = true;
        }

        // 6. Teléfono: Opcional, pero si se ingresa debe iniciar con 9 y tener exactamente 9 dígitos
        if (telefono != null && !telefono.trim().isEmpty()) {
            if (!telefono.trim().matches("^9[0-9]{8}$")) {
                model.addAttribute("errorTelefono", "El teléfono debe iniciar con 9 y tener exactamente 9 dígitos");
                hasErrors = true;
            }
        }

        // 7. Distrito
        if (distrito == null || distrito.trim().isEmpty()) {
            model.addAttribute("errorDistrito", "El distrito es obligatorio");
            hasErrors = true;
        }

        // 8. Contraseña: Mínimo 8 caracteres, Mayúscula, Minúscula, Número, Carácter especial
        if (password == null || password.isEmpty()) {
            model.addAttribute("errorPassword", "La contraseña es obligatoria");
            hasErrors = true;
        } else if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&._\\-#/])[A-Za-z\\d@$!%*?&._\\-#/]{8,}$")) {
            model.addAttribute("errorPassword", "Debe tener mínimo 8 caracteres, una mayúscula, una minúscula, un número y un carácter especial");
            hasErrors = true;
        }

        // 9. Confirmación de contraseña
        if (confirmPassword == null || confirmPassword.isEmpty()) {
            model.addAttribute("errorConfirmPassword", "Debe confirmar la contraseña");
            hasErrors = true;
        } else if (!confirmPassword.equals(password)) {
            model.addAttribute("errorConfirmPassword", "Las contraseñas no coinciden");
            hasErrors = true;
        }

        // 10. Verificar que el correo haya sido validado por AJAX en el formulario
        Boolean emailVerified = (Boolean) session.getAttribute("regEmailVerified");
        String verifiedEmail = (String) session.getAttribute("regEmail");
        if (emailVerified == null || !emailVerified || verifiedEmail == null || !verifiedEmail.equalsIgnoreCase(email.trim())) {
            model.addAttribute("errorEmail", "Debe validar y verificar este correo electrónico");
            hasErrors = true;
        }

        // 11. Aceptación de términos y políticas
        if (aceptoTerminos == null || !aceptoTerminos) {
            model.addAttribute("errorAceptoTerminos", "Debe aceptar los términos y políticas para continuar.");
            hasErrors = true;
        }

        if (hasErrors) {
            return "auth/registro-cliente";
        }

        // Crear la entidad Usuario (ya verificado, se guarda enabled=true)
        String phoneToSave = (telefono != null) ? telefono.trim() : "";
        String apellidoCompleto = apellidoPaterno.trim() + " " + apellidoMaterno.trim();
        Usuario cliente = new Usuario(email.trim(), password, "CLIENTE", nombre.trim(), apellidoCompleto, phoneToSave, distrito.trim());
        
        try {
            authService.registrarCliente(cliente, dni, direccion);
            
            // Limpiar datos de verificación en sesión
            session.removeAttribute("regEmailVerified");
            session.removeAttribute("regEmail");
            session.removeAttribute("regEmailOtp");

            return "redirect:/registro-exito";
        } catch (Exception e) {
            model.addAttribute("errorEmail", "Error en el registro: " + e.getMessage());
            return "auth/registro-cliente";
        }
    }

    /**
     * Mapeos REST para Validaciones AJAX en tiempo real (Registro)
     */

    @PostMapping("/api/auth/send-email-otp")
    @ResponseBody
    public Map<String, Object> sendEmailOtp(@RequestParam String email, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (email == null || email.trim().isEmpty() || !email.trim().matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
                response.put("success", false);
                response.put("message", "Ingrese un correo electrónico válido");
                return response;
            }
            if (authService.existePorUsername(email.trim())) {
                response.put("success", false);
                response.put("message", "El correo ya se encuentra registrado");
                return response;
            }

            // Control de spam (Fase 6): 60 segundos de espera
            Long lastSentTime = (Long) session.getAttribute("regEmailOtpTime");
            if (lastSentTime != null && (System.currentTimeMillis() - lastSentTime < 60000)) {
                long secondsLeft = 60 - ((System.currentTimeMillis() - lastSentTime) / 1000);
                response.put("success", false);
                response.put("message", "Espere " + secondsLeft + " segundos antes de reenviar el código.");
                return response;
            }

            String otp = emailVerificationService.generarCodigoVerificacion();
            emailVerificationService.enviarCodigoVerificacion(email.trim(), otp, session);

            boolean isDev = emailVerificationService.isDevMode();
            response.put("success", true);
            response.put("message", "✓ Código enviado correctamente.");
            response.put("devMode", isDev);
            if (isDev) {
                response.put("otp", otp);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al enviar código: " + e.getMessage());
        }
        return response;
    }

    @PostMapping("/api/auth/verify-email-otp")
    @ResponseBody
    public Map<String, Object> verifyEmailOtp(@RequestParam String otp, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        String email = (String) session.getAttribute("regEmail");
        if (email == null) {
            response.put("success", false);
            response.put("message", "Sesión de verificación expirada. Por favor, reenvíe el código.");
            return response;
        }

        boolean verificado = emailVerificationService.verificarCodigo(email, otp);
        if (verificado) {
            session.setAttribute("regEmailVerified", true);
            response.put("success", true);
            response.put("message", "Correo verificado");
        } else {
            response.put("success", false);
            response.put("message", "Código de verificación incorrecto, vencido o ya utilizado.");
        }
        return response;
    }

    /**
     * Muestra la solicitud de recuperación de contraseña
     */
    @GetMapping("/recuperar-acceso")
    public String mostrarRecuperarAcceso() {
        return "auth/recuperar-acceso";
    }

    /**
     * Procesa la solicitud y genera el resetToken
     */
    @PostMapping("/recuperar-acceso")
    public String procesarRecuperarAcceso(@RequestParam String email, Model model, RedirectAttributes redirectAttributes) {
        if (email == null || email.trim().isEmpty()) {
            model.addAttribute("errorEmail", "El correo es obligatorio");
            return "auth/recuperar-acceso";
        }

        boolean enviado = authService.generarTokenRecuperacion(email.trim());
        if (enviado) {
            redirectAttributes.addFlashAttribute("mensajeExito", "Se ha enviado un enlace de recuperación de contraseña al correo proporcionado.");
            return "redirect:/login-cliente";
        } else {
            model.addAttribute("errorEmail", "El correo ingresado no se encuentra registrado");
            return "auth/recuperar-acceso";
        }
    }

    /**
     * Muestra la vista para restablecer la contraseña
     */
    @GetMapping("/restablecer-contrasena")
    public String mostrarRestablecerContrasena(@RequestParam String token, Model model) {
        model.addAttribute("token", token);
        return "auth/restablecer-contrasena";
    }

    /**
     * Guarda la nueva contraseña
     */
    @PostMapping("/restablecer-contrasena")
    public String procesarRestablecerContrasena(@RequestParam String token,
                                                @RequestParam String password,
                                                @RequestParam String confirmPassword,
                                                Model model,
                                                RedirectAttributes redirectAttributes) {
        model.addAttribute("token", token);
        boolean hasErrors = false;

        if (password == null || password.isEmpty()) {
            model.addAttribute("errorPassword", "La contraseña es obligatoria");
            hasErrors = true;
        } else if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&._\\-#/])[A-Za-z\\d@$!%*?&._\\-#/]{8,}$")) {
            model.addAttribute("errorPassword", "Debe tener mínimo 8 caracteres, una mayúscula, una minúscula, un número y un carácter especial");
            hasErrors = true;
        }

        if (confirmPassword == null || confirmPassword.isEmpty()) {
            model.addAttribute("errorConfirmPassword", "Confirme la contraseña");
            hasErrors = true;
        } else if (!confirmPassword.equals(password)) {
            model.addAttribute("errorConfirmPassword", "Las contraseñas no coinciden");
            hasErrors = true;
        }

        if (hasErrors) {
            return "auth/restablecer-contrasena";
        }

        boolean actualizado = authService.restablecerPassword(token, password);
        if (actualizado) {
            redirectAttributes.addFlashAttribute("mensajeExito", "Su contraseña ha sido actualizada con éxito. Inicie sesión ahora.");
            return "redirect:/login-cliente";
        } else {
            model.addAttribute("errorPassword", "El enlace de recuperación es inválido o ha expirado.");
            return "auth/restablecer-contrasena";
        }
    }

    /**
     * Gestiona la respuesta de Google OAuth2 (Redirect / POST payload)
     */
    @PostMapping("/auth/google-login")
    public String googleLogin(@RequestParam("credential") String credential,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        try {
            if (credential != null && !credential.isEmpty()) {
                String[] parts = credential.split("\\.");
                if (parts.length >= 2) {
                    String payloadJson = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
                    
                    String email = extraerJsonField(payloadJson, "email");
                    String nombre = extraerJsonField(payloadJson, "given_name");
                    String apellido = extraerJsonField(payloadJson, "family_name");
                    
                    if (email != null && !email.isEmpty()) {
                        Usuario usuario = authService.loginOauthGoogle(email, nombre, apellido);
                        
                        session.setAttribute("usuario", usuario);
                        session.setAttribute("username", usuario.getUsername());
                        session.setAttribute("rol", usuario.getRol());
                        session.setMaxInactiveInterval(1800);
                        
                        redirectAttributes.addFlashAttribute("mensajeExito", "Sesión iniciada con Google correctamente.");
                        return "redirect:/portal-cliente/cargando";
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        redirectAttributes.addFlashAttribute("mensajeError", "Error al autenticar con Google. Intente nuevamente.");
        return "redirect:/login-cliente";
    }

    /**
     * Cierra la sesión del cliente
     */
    @GetMapping("/logout-cliente")
    public String logoutCliente(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("mensajeExito", "Sesión cerrada correctamente.");
        return "redirect:/";
    }

    /**
     * Auxiliar para extraer campos de un JSON simple plano
     */
    private String extraerJsonField(String json, String field) {
        String key = "\"" + field + "\":";
        int index = json.indexOf(key);
        if (index == -1) return "";
        int valStart = index + key.length();
        // Omitir espacios y comillas iniciales
        while (valStart < json.length() && (json.charAt(valStart) == ' ' || json.charAt(valStart) == '"')) {
            valStart++;
        }
        int valEnd = valStart;
        while (valEnd < json.length() && json.charAt(valEnd) != '"' && json.charAt(valEnd) != ',' && json.charAt(valEnd) != '}') {
            valEnd++;
        }
        return json.substring(valStart, valEnd).trim();
    }
}
