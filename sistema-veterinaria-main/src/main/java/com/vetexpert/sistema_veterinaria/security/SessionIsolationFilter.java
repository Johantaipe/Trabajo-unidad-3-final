package com.vetexpert.sistema_veterinaria.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

/**
 * Filtro de aislamiento de sesiones (Fase 1 - Arquitectura de Sesión Segura).
 * Evita la interferencia de sesiones entre el Portal de Clientes y el Portal de Personal (Backoffice)
 * aplicando un espacio de nombres (namespace) aislado sobre los atributos de la sesión.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SessionIsolationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Inicialización
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            chain.doFilter(new IsolatedSessionRequestWrapper((HttpServletRequest) request), response);
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
        // Destrucción
    }

    public static class IsolatedSessionRequestWrapper extends HttpServletRequestWrapper {

        public IsolatedSessionRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        private String getSessionPrefix() {
            String path = getRequestURI();
            if (isClientPath(path)) {
                return "client_";
            }
            if (isStaffPath(path)) {
                return "staff_";
            }
            return "public_";
        }

        private boolean isClientPath(String path) {
            return path.startsWith("/portal-cliente") || 
                   path.equals("/login-cliente") || 
                   path.equals("/logout-cliente") || 
                   path.equals("/registro-cliente") || 
                   path.equals("/registro-exito") || 
                   path.equals("/recuperar-acceso") || 
                   path.equals("/restablecer-contrasena") || 
                   path.equals("/auth/google-login") || 
                   path.startsWith("/api/auth/");
        }

        private boolean isStaffPath(String path) {
            return path.equals("/dashboard") || 
                   path.startsWith("/usuarios") || 
                   path.startsWith("/propietarios") || 
                   path.startsWith("/mascotas") || 
                   path.startsWith("/historias") || 
                   path.startsWith("/vacunas") || 
                   path.startsWith("/desparasitaciones") || 
                   path.startsWith("/hospitalizacion") || 
                   path.startsWith("/caja") || 
                   path.startsWith("/reportes") || 
                   path.startsWith("/alertas") || 
                   path.startsWith("/promociones") || 
                   path.startsWith("/inventario") || 
                   path.startsWith("/configuracion") || 
                   path.equals("/login-admin") || 
                   path.equals("/login-secretaria") || 
                   path.equals("/login-veterinario") || 
                   path.equals("/login-personal") || 
                   path.equals("/staff/login") || 
                   path.equals("/auth/login") || 
                   path.equals("/auth/logout") || 
                   path.equals("/logout") ||
                   path.equals("/admin/login") ||
                   path.equals("/admin");
        }

        @Override
        public HttpSession getSession(boolean create) {
            HttpSession session = super.getSession(create);
            if (session == null) {
                return null;
            }
            return new IsolatedSession(session, getSessionPrefix());
        }

        @Override
        public HttpSession getSession() {
            return getSession(true);
        }
    }

    public static class IsolatedSession implements HttpSession {
        private final HttpSession delegate;
        private final String prefix;

        public IsolatedSession(HttpSession delegate, String prefix) {
            this.delegate = delegate;
            this.prefix = prefix;
        }

        private String getRealKey(String name) {
            return prefix + name;
        }

        @Override
        public Object getAttribute(String name) {
            if ("public_".equals(prefix)) {
                // En páginas públicas, priorizar rol cliente si existe, si no rol de personal
                if ("usuario".equals(name) || "rol".equals(name) || "username".equals(name)) {
                    Object clientVal = delegate.getAttribute("client_" + name);
                    if (clientVal != null) {
                        return clientVal;
                    }
                    return delegate.getAttribute("staff_" + name);
                }
                return delegate.getAttribute("public_" + name);
            }
            return delegate.getAttribute(getRealKey(name));
        }

        @Override
        public void setAttribute(String name, Object value) {
            if ("public_".equals(prefix)) {
                delegate.setAttribute("public_" + name, value);
            } else {
                delegate.setAttribute(getRealKey(name), value);
            }
        }

        @Override
        public void removeAttribute(String name) {
            if ("public_".equals(prefix)) {
                delegate.removeAttribute("public_" + name);
            } else {
                delegate.removeAttribute(getRealKey(name));
            }
        }

        @Override
        public Enumeration<String> getAttributeNames() {
            List<String> names = new ArrayList<>();
            Enumeration<String> allNames = delegate.getAttributeNames();
            while (allNames.hasMoreElements()) {
                String name = allNames.nextElement();
                if (name.startsWith(prefix)) {
                    names.add(name.substring(prefix.length()));
                }
            }
            return Collections.enumeration(names);
        }

        @Override
        public void invalidate() {
            // No destruimos la sesión física para que el otro portal no pierda la sesión
            // En su lugar, removemos únicamente los atributos de este espacio de nombres
            Enumeration<String> allNames = delegate.getAttributeNames();
            List<String> toRemove = new ArrayList<>();
            while (allNames.hasMoreElements()) {
                String name = allNames.nextElement();
                if (name.startsWith(prefix)) {
                    toRemove.add(name);
                }
            }
            for (String name : toRemove) {
                delegate.removeAttribute(name);
            }
        }

        @Override
        public long getCreationTime() { return delegate.getCreationTime(); }
        @Override
        public String getId() { return delegate.getId(); }
        @Override
        public long getLastAccessedTime() { return delegate.getLastAccessedTime(); }
        @Override
        public ServletContext getServletContext() { return delegate.getServletContext(); }
        @Override
        public void setMaxInactiveInterval(int interval) { delegate.setMaxInactiveInterval(interval); }
        @Override
        public int getMaxInactiveInterval() { return delegate.getMaxInactiveInterval(); }
        @Override
        public boolean isNew() { return delegate.isNew(); }
    }
}
