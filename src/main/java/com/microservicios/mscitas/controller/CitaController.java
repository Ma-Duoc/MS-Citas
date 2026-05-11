package com.microservicios.mscitas.controller;

import com.microservicios.mscitas.dto.CitaRequest;
import com.microservicios.mscitas.dto.CitaResponse;
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
     */
    @PostMapping
    public ResponseEntity<CitaResponse> agregarCita(@Valid @RequestBody CitaRequest request) {

        log.info("Recibida solicitud para crear cita: {}", request);

        CitaResponse citaCreada = citaService.crearCita(request);

        log.info("Cita creada exitosamente con ID: {}", citaCreada.id());

        return ResponseEntity.status(HttpStatus.CREATED).body(citaCreada);
    }

    /**
     * Obtener una cita por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<CitaResponse> obtenerCita(@PathVariable Long id) {

        log.info("Buscando cita con ID: {}", id);

        CitaResponse cita = citaService.obtenerCita(id);

        return ResponseEntity.ok(cita);
    }

    /**
     * Obtener citas por usuario
     */
    @GetMapping("/usuario/{userId}")
    public ResponseEntity<List<CitaResponse>> obtenerCitasPorUsuario(@PathVariable String userId) {

        log.info("Buscando citas para el usuario: {}", userId);

        List<CitaResponse> citas = citaService.obtenerCitasPorUsuario(userId);

        return ResponseEntity.ok(citas);
    }

    /**
     * Obtener citas por médico
     */
    @GetMapping("/medico/{medicoId}")
    public ResponseEntity<List<CitaResponse>> obtenerCitasPorMedico(@PathVariable String medicoId) {

        log.info("Buscando citas para el médico: {}", medicoId);

        List<CitaResponse> citas = citaService.obtenerCitasPorMedico(medicoId);

        return ResponseEntity.ok(citas);
    }

    /**
     * Health check
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {

        return ResponseEntity.ok("UP - ms-citas");
    }
}