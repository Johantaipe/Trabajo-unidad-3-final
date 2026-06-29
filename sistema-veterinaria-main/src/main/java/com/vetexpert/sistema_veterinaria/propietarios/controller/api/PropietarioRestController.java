package com.vetexpert.sistema_veterinaria.propietarios.controller.api;

import com.vetexpert.sistema_veterinaria.propietarios.model.Propietario;
import com.vetexpert.sistema_veterinaria.propietarios.service.PropietarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controlador REST para el autocompletado inteligente y búsqueda de propietarios.
 * Módulo: propietarios
 */
@RestController
@RequestMapping("/api/propietarios")
public class PropietarioRestController {

    private final PropietarioService propietarioService;

    public PropietarioRestController(PropietarioService propietarioService) {
        this.propietarioService = propietarioService;
    }

    /**
     * Busca propietarios de forma inteligente y los retorna en formato JSON.
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<Map<String, Object>>> buscar(@RequestParam("query") String query) {
        List<Propietario> resultados = propietarioService.buscarInteligente(query);
        List<Map<String, Object>> response = resultados.stream().map(p -> Map.<String, Object>of(
                "id", p.getId(),
                "dni", p.getDni(),
                "nombre", p.getNombre(),
                "apellido", p.getApellido(),
                "nombreCompleto", p.getNombre() + " " + p.getApellido(),
                "telefono", p.getTelefono() != null ? p.getTelefono() : "",
                "correo", p.getCorreo(),
                "direccion", p.getDireccion() != null ? p.getDireccion() : ""
        )).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
