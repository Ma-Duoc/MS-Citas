package com.microservicios.mscitas.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * =========================
     * ERRORES DE NEGOCIO
     * =========================
     *
     * Incluye:
     * - DisponibilidadException
     * - CitaException
     *
     * Ya que DisponibilidadException
     * extiende de CitaException
     */
    @ExceptionHandler(CitaException.class)
    public ResponseEntity<Map<String, Object>> handleCitaException(
            CitaException ex) {

        log.error("CitaException: {}", ex.getMessage(), ex);

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Bad Request");
        response.put("message", ex.getMessage());
        response.put("path", "/api/citas");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * =========================
     * ERRORES DE MICROSERVICIOS
     * =========================
     *
     * Maneja:
     * - MS_PACIENTES_UNAVAILABLE
     * - MS_MEDICOS_UNAVAILABLE
     * - MS_SALAS_UNAVAILABLE
     * - SERVICIOS_EXTERNOS_UNAVAILABLE
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(
            RuntimeException ex) {

        log.error("RuntimeException: {}", ex.getMessage(), ex);

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());

        if (ex.getMessage() != null &&
                (ex.getMessage().contains("MS_PACIENTES_UNAVAILABLE")
                || ex.getMessage().contains("MS_MEDICOS_UNAVAILABLE")
                || ex.getMessage().contains("MS_SALAS_UNAVAILABLE")
                || ex.getMessage().contains("SERVICIOS_EXTERNOS_UNAVAILABLE"))) {

            response.put(
                    "status",
                    HttpStatus.SERVICE_UNAVAILABLE.value()
            );

            response.put(
                    "error",
                    "Service Unavailable"
            );

            response.put(
                    "message",
                    "Servicios externos no disponibles"
            );

            response.put("path", "/api/citas");

            return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(response);
        }

        response.put(
                "status",
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );

        response.put(
                "error",
                "Internal Server Error"
        );

        response.put(
                "message",
                ex.getMessage()
        );

        response.put("path", "/api/citas");

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    /**
     * =========================
     * VALIDACIONES @VALID
     * =========================
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        log.error("Validation error: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult()
                .getAllErrors()
                .forEach(error -> {

                    String fieldName =
                            ((FieldError) error).getField();

                    String errorMessage =
                            error.getDefaultMessage();

                    errors.put(fieldName, errorMessage);
                });

        Map<String, Object> response = new HashMap<>();

        response.put("timestamp", LocalDateTime.now());

        response.put(
                "status",
                HttpStatus.BAD_REQUEST.value()
        );

        response.put(
                "error",
                "Validation Error"
        );

        response.put(
                "message",
                "Error de validación en los datos de entrada"
        );

        response.put("validationErrors", errors);

        response.put("path", "/api/citas");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * =========================
     * ARGUMENTOS INVÁLIDOS
     * =========================
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>>
    handleIllegalArgumentException(
            IllegalArgumentException ex) {

        log.error(
                "IllegalArgumentException: {}",
                ex.getMessage(),
                ex
        );

        Map<String, Object> response = new HashMap<>();

        response.put("timestamp", LocalDateTime.now());

        response.put(
                "status",
                HttpStatus.BAD_REQUEST.value()
        );

        response.put(
                "error",
                "Bad Request"
        );

        response.put(
                "message",
                ex.getMessage()
        );

        response.put("path", "/api/citas");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * =========================
     * FALLBACK GLOBAL
     * =========================
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex) {

        log.error(
                "Unexpected error: {}",
                ex.getMessage(),
                ex
        );

        Map<String, Object> response = new HashMap<>();

        response.put("timestamp", LocalDateTime.now());

        response.put(
                "status",
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );

        response.put(
                "error",
                "Internal Server Error"
        );

        response.put(
                "message",
                "Error interno del servidor"
        );

        response.put("path", "/api/citas");

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}