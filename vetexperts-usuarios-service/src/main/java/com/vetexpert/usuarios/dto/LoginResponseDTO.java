package com.vetexpert.usuarios.dto;

/**
 * Response DTO que contiene el token JWT y los datos del usuario autenticado.
 */
public class LoginResponseDTO {

    private String token;
    private String tipo = "Bearer";
    private Long id;
    private String username;
    private String nombre;
    private String apellido;
    private String rol;
    private String correo;
    private String fotoUrl;

    public LoginResponseDTO() {}

    public LoginResponseDTO(String token, Long id, String username, String nombre,
                            String apellido, String rol, String correo, String fotoUrl) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.nombre = nombre;
        this.apellido = apellido;
        this.rol = rol;
        this.correo = correo;
        this.fotoUrl = fotoUrl;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }
}
