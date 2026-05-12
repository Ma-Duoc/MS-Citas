package com.microservicios.mscitas.controller;

import com.microservicios.mscitas.model.dto.CitaRequest;
import com.microservicios.mscitas.model.dto.CitaResponse;
import com.microservicios.mscitas.service.CitaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/citas")
@RequiredArgsConstructor
@Validated
@Slf4j
public class CitaController {

    private final CitaService citaService;

    /**
     * Crear una nueva cita
     * Endpoint: POST /
     * 
     * @param request Datos de la cita a crear
     * @return Respuesta con la cita creada
     */
    @PostMapping
    public ResponseEntity<CitaResponseWrapper> agregarCita(@Valid @RequestBody CitaRequest request) {
        log.info("Recibida solicitud para crear cita: {}", request);

        CitaResponse citaCreada = citaService.crearCita(request);

        CitaResponseWrapper response = new CitaResponseWrapper(citaCreada, "Cita creada exitosamente");

        log.info("Cita creada exitosamente con ID: {}", citaCreada.id());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtener una cita por su ID
     * Endpoint: GET /{id}
     * 
     * @param id ID de la cita
     * @return Datos de la cita
     */
    @GetMapping("/{id}")
    public ResponseEntity<CitaResponseWrapper> obtenerCita(@PathVariable Long id) {
        log.info("Buscando cita con ID: {}", id);

        CitaResponse cita = citaService.obtenerCita(id);

        CitaResponseWrapper response = new CitaResponseWrapper(cita, "Cita encontrada exitosamente");

        return ResponseEntity.ok(response);
    }

    /**
     * Obtener todas las citas de un usuario
     * Endpoint: GET /api/citas/usuario/{userId}
     * 
     * @param userId ID del usuario
     * @return Lista de citas del usuario
     */
    @GetMapping("/usuario/{userId}")
    public ResponseEntity<CitasResponseWrapper> obtenerCitasPorUsuario(@PathVariable String userId) {
        log.info("Buscando citas para el usuario: {}", userId);

        List<CitaResponse> citas = citaService.obtenerCitasPorUsuario(userId);

        CitasResponseWrapper response = new CitasResponseWrapper(citas, citas.size(),
                "Citas del usuario obtenidas exitosamente");

        return ResponseEntity.ok(response);
    }

    /**
     * Obtener todas las citas de un médico
     * Endpoint: GET /api/citas/medico/{medicoId}
     * 
     * @param medicoId ID del médico
     * @return Lista de citas del médico
     */
    @GetMapping("/medico/{medicoId}")
    public ResponseEntity<CitasResponseWrapper> obtenerCitasPorMedico(@PathVariable String medicoId) {
        log.info("Buscando citas para el médico: {}", medicoId);

        List<CitaResponse> citas = citaService.obtenerCitasPorMedico(medicoId);

        CitasResponseWrapper response = new CitasResponseWrapper(citas, citas.size(),
                "Citas del médico obtenidas exitosamente");

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint de health check
     * Endpoint: GET /api/citas/health
     * 
     * @return Estado del servicio
     */
    @GetMapping("/health")
    public ResponseEntity<HealthResponse> healthCheck() {
        HealthResponse response = new HealthResponse("UP", "ms-citas", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }
}
