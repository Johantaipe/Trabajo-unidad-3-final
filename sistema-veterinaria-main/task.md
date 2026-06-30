# Task List — FASE 4 (Mejora del Header y Login del Personal)

## 1. Eliminar Dashboard
- `[x]` Modificar `/dashboard` en `HomeController.java` para redirección automática
- `[x]` Eliminar el archivo de plantilla `dashboard.html`
- `[x]` Remover la opción "Dashboard" del sidebar en `layout.html`

## 2. Modificaciones de Entidad y Base de Datos
- `[x]` Agregar campo `dni` (String, nullable, length 8) a la entidad `Usuario.java` (con getters/setters y unique constraint en BD)
- `[x]` Agregar métodos `existsByDni` y `findByDni` en `UsuarioRepository.java`

## 3. Módulo de Usuarios (Backend & Seguridad)
- `[x]` Agregar validación de rol `ADMIN` en todos los métodos de `UsuarioController.java`
- `[x]` Excluir usuarios con rol `CLIENTE` de la lista de usuarios en `listarUsuarios`
- `[x]` Implementar ordenamiento por fecha descendente (nuevos primero) por defecto
- `[x]` Agregar endpoints específicos para Cambiar Rol (`/usuarios/cambiar-rol/{id}`) y Reset Contraseña (`/usuarios/reset-password/{id}`)
- `[x]` Actualizar `guardarNuevoUsuario` con validaciones (correo y DNI únicos, teléfono correcto, contraseña segura)
- `[x]` Actualizar `guardarEditarUsuario` con validaciones correspondientes

## 4. Módulo de Usuarios (Frontend)
- `[x]` Actualizar formulario de creación/edición (`formulario.html`) con campos DNI, teléfono y estado activo/inactivo
- `[x]` Actualizar listado de usuarios (`lista.html`) con columnas completas, dropdown de cambio de rol, reset e indicación de estado
- `[x]` Implementar el Modal Bootstrap para "Ver Detalle" del usuario en `lista.html`

## 5. Header y Login del Personal (Fase 4)
- `[x]` Eliminar icono de WhatsApp (contacto) del menú superior de la Landing Page
- `[x]` Agregar icono de Acceso Personal (`fa-user-lock`) entre los botones "Ingresar" y "Crear Cuenta"
- `[x]` Habilitar el path `/login` en la interceptación de seguridad (`SecurityInterceptor.java`)
- `[x]` Rediseñar el Login del Personal (`staff-login.html`) con una tarjeta moderna, degradados limpios, campos modernos, botón Google y ocultar/mostrar contraseña.
- `[x]` Compilar y verificar visualmente en el navegador. ✅

## 6. Corrección Paso 4 (Reservar Cita)
- `[x]` Modificar `SecurityInterceptor.java` para permitir `/api/agenda/` para el rol `CLIENTE` ✅
- `[x]` Agregar método `findByRolAndEnabledTrue` en `UsuarioRepository.java` ✅
- `[x]` Actualizar `PortalClienteController.java` para cargar veterinarios reales activos ✅
- `[x]` Modificar `CitaRestController.java` para calcular horarios dinámicamente y validar fecha/veterinario activo/días laborables ✅
- `[x]` Modificar `CitaServiceImpl.java` para validar fechas pasadas en registrar y actualizar ✅
- `[x]` Modificar `dashboard.html` para cambiar el texto de `#noSlotsMessage` ✅
