package com.vetexpert.sistema_veterinaria.propietarios.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST para simular la consulta a la API de RENIEC.
 * Módulo: propietarios
 */
@RestController
@RequestMapping("/api/reniec")
public class ReniecRestController {

    /**
     * Simula la búsqueda de un ciudadano por DNI.
     */
    @GetMapping("/{dni}")
    public ResponseEntity<Map<String, String>> consultarReniec(@PathVariable String dni) {
        if (dni == null || dni.length() != 8 || !dni.matches("\\d+")) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "DNI inválido. Debe tener exactamente 8 dígitos numéricos.");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        Map<String, String> persona = new HashMap<>();
        switch (dni) {
            case "12345678":
                persona.put("nombres", "Juan Carlos");
                persona.put("apellidos", "Pérez Rodríguez");
                break;
            case "87654321":
                persona.put("nombres", "María Elena");
                persona.put("apellidos", "Gómez Flores");
                break;
            case "44445555":
                persona.put("nombres", "Luis Alberto");
                persona.put("apellidos", "Sánchez Díaz");
                break;
            default:
                // Generar nombres simulados basados en el DNI
                int hash = dni.hashCode();
                String[] nombresSimulados = {"Diego Alonso", "Sofía Lucía", "Carlos Mateo", "Ana Valeria", "José Luis", "Camila Inés", "Grover Paul", "Milagros Edith", "Jorge Bruno", "Rosa Patricia"};
                String[] apellidosSimulados = {"Alvarado Cruz", "Quispe Mamani", "Flores Ramos", "Castillo Vargas", "Gonzales Silva", "Rojas Mendoza", "García Torres", "Díaz Ortiz", "Vásquez Chávez", "Villanueva Ruiz"};
                
                int indexNom = Math.abs(hash) % nombresSimulados.length;
                int indexApe = Math.abs(hash * 31) % apellidosSimulados.length;
                
                persona.put("nombres", nombresSimulados[indexNom]);
                persona.put("apellidos", apellidosSimulados[indexApe]);
                break;
        }
        return ResponseEntity.ok(persona);
    }
}
