package com.vetexpert.sistema_veterinaria.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class SecurityInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        HttpSession session = request.getSession(false);

        // 1. Excluir recursos estáticos y páginas públicas
        if (path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/img/") || path.startsWith("/uploads/") || path.equals("/error")) {
            return true;
        }

        if (path.equals("/") || path.equals("/contacto/enviar") || path.equals("/terminos-de-uso") || path.equals("/politica-privacidad")) {
            return true;
        }

        // 2. Excluir rutas públicas de login, registro, OTP, Google OAuth
        if (path.equals("/login-admin") || path.equals("/login-secretaria") || path.equals("/login-veterinario") || path.equals("/login-cliente") ||
            path.equals("/registro-cliente") || path.equals("/registro-exito") || path.equals("/recuperar-acceso") || path.equals("/restablecer-contrasena") ||
            path.startsWith("/api/auth/") || path.equals("/auth/google-login") || path.equals("/auth/logout") || path.equals("/logout-cliente") ||
            path.equals("/auth/registro") || path.equals("/login-personal") || path.equals("/staff/login") || path.equals("/login") || path.equals("/403")) {
            return true;
        }

        // Redirigir accesos legados de login
        if (path.equals("/admin/login") || path.equals("/auth/login") || path.equals("/admin")) {
            response.sendRedirect("/");
            return false;
        }

        // 3. Validación de sesión activa
        if (session == null || session.getAttribute("usuario") == null) {
            if (path.startsWith("/portal-cliente")) {
                response.sendRedirect("/login-cliente");
            } else {
                response.sendRedirect("/");
            }
            return false;
        }

        String rol = (String) session.getAttribute("rol");

        // 4. Control de Acceso por Roles

        // ROL CLIENTE
        if ("CLIENTE".equals(rol)) {
            if (!path.startsWith("/portal-cliente") && !path.startsWith("/api/agenda/")) {
                response.sendRedirect("/portal-cliente/dashboard");
                return false;
            }
            return true;
        }

        // Personal interno no debe ingresar al portal de clientes
        if (path.startsWith("/portal-cliente")) {
            response.sendRedirect("/dashboard");
            return false;
        }

        // ROL ADMINISTRADOR (ADMIN)
        if ("ADMIN".equals(rol)) {
            return true; // Acceso total
        }

        // ROL SECRETARIA
        if ("SECRETARIA".equals(rol)) {
            // Rutas permitidas: dashboard, agenda, propietarios, mascotas, caja
            // NO permitidas: usuarios, configuracion, hospitalizacion, historias, vacunas, desparasitaciones, reportes, mensajes
            if (path.startsWith("/usuarios") || path.startsWith("/configuracion") || path.startsWith("/hospitalizacion") ||
                path.startsWith("/historias") || path.startsWith("/vacunas") || path.startsWith("/desparasitaciones") ||
                path.startsWith("/reportes") || path.startsWith("/alertas") ||
                path.startsWith("/promociones")) {
                response.sendRedirect("/403");
                return false;
            }

            // Inventario: solo consulta (solo métodos GET)
            if (path.startsWith("/inventario")) {
                String method = request.getMethod();
                if (!"GET".equalsIgnoreCase(method)) {
                    response.sendRedirect("/403");
                    return false;
                }
            }
            return true;
        }

        // ROL VETERINARIO
        if ("VETERINARIO".equals(rol)) {
            // Rutas permitidas: dashboard, agenda, historias (consultas), vacunas, desparasitaciones, hospitalizacion
            // NO permitidas: caja, usuarios, configuracion, propietarios, mascotas, inventario, reportes, alertas, promociones
            if (path.startsWith("/caja") || path.startsWith("/usuarios") || path.startsWith("/configuracion") ||
                path.startsWith("/propietarios") || path.startsWith("/mascotas") || path.startsWith("/inventario") ||
                path.startsWith("/reportes") || path.startsWith("/alertas") ||
                path.startsWith("/promociones")) {
                response.sendRedirect("/403");
                return false;
            }
            return true;
        }

        // ROL RECEPCIONISTA
        if ("RECEPCIONISTA".equals(rol)) {
            // Rutas permitidas: dashboard, agenda, propietarios, mascotas, caja
            // NO permitidas: usuarios, configuracion, hospitalizacion, historias, vacunas, desparasitaciones, reportes, mensajes
            if (path.startsWith("/usuarios") || path.startsWith("/configuracion") || path.startsWith("/hospitalizacion") ||
                path.startsWith("/historias") || path.startsWith("/vacunas") || path.startsWith("/desparasitaciones") ||
                path.startsWith("/reportes") || path.startsWith("/alertas") ||
                path.startsWith("/promociones")) {
                response.sendRedirect("/403");
                return false;
            }

            // Inventario: solo consulta (solo métodos GET)
            if (path.startsWith("/inventario")) {
                String method = request.getMethod();
                if (!"GET".equalsIgnoreCase(method)) {
                    response.sendRedirect("/403");
                    return false;
                }
            }
            return true;
        }

        // ROL PRACTICANTE
        if ("PRACTICANTE".equals(rol)) {
            // Rutas permitidas: dashboard, agenda, historias, vacunas, desparasitaciones, hospitalizacion
            // NO permitidas: caja, usuarios, configuracion, propietarios, mascotas, inventario, reportes, alertas, promociones
            if (path.startsWith("/caja") || path.startsWith("/usuarios") || path.startsWith("/configuracion") ||
                path.startsWith("/propietarios") || path.startsWith("/mascotas") || path.startsWith("/inventario") ||
                path.startsWith("/reportes") || path.startsWith("/alertas") ||
                path.startsWith("/promociones")) {
                response.sendRedirect("/403");
                return false;
            }
            return true;
        }

        // Rol desconocido: invalidar y redirigir
        session.invalidate();
        response.sendRedirect("/");
        return false;
    }
}
