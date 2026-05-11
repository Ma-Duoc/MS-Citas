package com.microservicios.mscitas.service;

import com.microservicios.mscitas.client.NotificationClient;
import com.microservicios.mscitas.dto.CitaRequest;
import com.microservicios.mscitas.dto.CitaResponse;
import com.microservicios.mscitas.exception.CitaException;
import com.microservicios.mscitas.exception.DisponibilidadException;
import com.microservicios.mscitas.model.Cita;
import com.microservicios.mscitas.repository.CitaRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CitaService {

    private static final Logger logger =
            LoggerFactory.getLogger(CitaService.class);

    private final CitaRepository citaRepository;
    private final DisponibilidadService disponibilidadService;
    private final NotificationClient notificationClient;

    public CitaService(
            CitaRepository citaRepository,
            DisponibilidadService disponibilidadService,
            NotificationClient notificationClient) {

        this.citaRepository = citaRepository;
        this.disponibilidadService = disponibilidadService;
        this.notificationClient = notificationClient;
    }

    /**
     * Crear nueva cita médica
     */
    @CircuitBreaker(
            name = "citasService",
            fallbackMethod = "fallbackCrearCita"
    )
    public CitaResponse crearCita(CitaRequest request) {

        logger.info(
                "Iniciando creación de cita para usuario {} con médico {} en sala {}",
                request.userId(),
                request.medicoId(),
                request.salaId()
        );

        try {

            // =====================================
            // VALIDAR DISPONIBILIDAD GENERAL
            // =====================================
            logger.info("Validando disponibilidad general");

            disponibilidadService.validarDisponibilidad(
                    request.userId(),
                    request.medicoId(),
                    request.salaId(),
                    request.fechaHora()
            );

            // =====================================
            // VALIDAR DUPLICADO MÉDICO
            // =====================================
            if (citaRepository.existsByMedicoIdAndFechaHora(
                    request.medicoId(),
                    request.fechaHora())) {

                String errorMsg = String.format(
                        "Ya existe una cita para el médico %s en la fecha %s",
                        request.medicoId(),
                        request.fechaHora()
                );

                logger.error(errorMsg);

                throw new CitaException(errorMsg);
            }

            // =====================================
            // VALIDAR DUPLICADO SALA
            // =====================================
            if (citaRepository.existsBySalaIdAndFechaHora(
                    request.salaId(),
                    request.fechaHora())) {

                String errorMsg = String.format(
                        "Ya existe una cita para la sala %s en la fecha %s",
                        request.salaId(),
                        request.fechaHora()
                );

                logger.error(errorMsg);

                throw new CitaException(errorMsg);
            }

            // =====================================
            // CREAR ENTIDAD
            // =====================================
            logger.info("Guardando cita en base de datos");

            Cita nuevaCita = Cita.builder()
                    .userId(request.userId())
                    .userNombre(request.userNombre())
                    .medicoId(request.medicoId())
                    .medicoNombre(request.medicoNombre())
                    .especialidad(request.especialidad())
                    .salaId(request.salaId())
                    .salaNombre(request.salaNombre())
                    .fechaHora(request.fechaHora())
                    .build();

            Cita citaGuardada = citaRepository.save(nuevaCita);

            logger.info(
                    "Cita guardada exitosamente con ID {}",
                    citaGuardada.getId()
            );

            // =====================================
            // ENVIAR NOTIFICACIÓN
            // =====================================
            enviarNotificacionCitaCreada(citaGuardada);

            logger.info(
                    "Cita creada exitosamente con ID {}",
                    citaGuardada.getId()
            );

            return CitaResponse.fromEntity(citaGuardada);

        } catch (DisponibilidadException e) {

            logger.error(
                    "Error de disponibilidad: {}",
                    e.getMessage()
            );

            throw e;

        } catch (CitaException e) {

            logger.error(
                    "Error de negocio: {}",
                    e.getMessage()
            );

            throw e;

        } catch (Exception e) {

            logger.error(
                    "Error inesperado al crear cita: {}",
                    e.getMessage(),
                    e
            );

            throw e;
        }
    }

    /**
     * FALLBACK DEL CIRCUIT BREAKER
     */
    public CitaResponse fallbackCrearCita(
            CitaRequest request,
            Throwable t) {

        logger.error(
                "Fallback ejecutado en crearCita: {}",
                t.getMessage(),
                t
        );

        // =====================================
        // RE-LANZAR EXCEPCIONES DE NEGOCIO
        // =====================================
        if (t instanceof DisponibilidadException disponibilidadException) {
            throw disponibilidadException;
        }

        if (t instanceof CitaException citaException) {
            throw citaException;
        }

        // =====================================
        // MICROSERVICIOS CAÍDOS
        // =====================================
        if (t.getMessage() != null &&
                t.getMessage().contains("MS_PACIENTES_UNAVAILABLE")) {

            throw new RuntimeException(
                    "MS_PACIENTES_UNAVAILABLE"
            );
        }

        if (t.getMessage() != null &&
                t.getMessage().contains("MS_MEDICOS_UNAVAILABLE")) {

            throw new RuntimeException(
                    "MS_MEDICOS_UNAVAILABLE"
            );
        }

        if (t.getMessage() != null &&
                t.getMessage().contains("MS_SALAS_UNAVAILABLE")) {

            throw new RuntimeException(
                    "MS_SALAS_UNAVAILABLE"
            );
        }

        // =====================================
        // FALLBACK GENERAL
        // =====================================
        throw new RuntimeException(
                "SERVICIOS_EXTERNOS_UNAVAILABLE"
        );
    }

    /**
     * Enviar notificación
     */
    private void enviarNotificacionCitaCreada(Cita cita) {

        try {

            logger.info(
                    "Enviando notificación para la cita ID: {}",
                    cita.getId()
            );

            String mensaje = String.format(
                    "Nueva cita creada:\n" +
                    "Paciente: %s\n" +
                    "Médico: %s\n" +
                    "Fecha: %s\n" +
                    "Especialidad: %s\n" +
                    "Sala: %s",
                    cita.getUserNombre(),
                    cita.getMedicoNombre(),
                    cita.getFechaHora(),
                    cita.getEspecialidad(),
                    cita.getSalaNombre()
            );

            NotificationClient.NotificationRequest request =
                    new NotificationClient.NotificationRequest(
                            "EMAIL",
                            "test@test.com",
                            mensaje
                    );

            boolean enviado =
                    notificationClient.sendNotification(request);

            if (enviado) {

                logger.info(
                        "Notificación enviada exitosamente para la cita ID: {}",
                        cita.getId()
                );

            } else {

                logger.warn(
                        "No se pudo enviar la notificación para la cita ID: {}",
                        cita.getId()
                );
            }

        } catch (Exception e) {

            logger.error(
                    "Error al enviar notificación para la cita ID {}: {}",
                    cita.getId(),
                    e.getMessage(),
                    e
            );
        }
    }

    @Transactional(readOnly = true)
    public CitaResponse obtenerCita(Long id) {

        logger.info("Buscando cita con ID: {}", id);

        return citaRepository.findById(id)
                .map(CitaResponse::fromEntity)
                .orElseThrow(() -> {

                    String errorMsg =
                            "No se encontró la cita con ID: " + id;

                    logger.error(errorMsg);

                    return new CitaException(errorMsg);
                });
    }

    @Transactional(readOnly = true)
    public List<CitaResponse> obtenerCitasPorUsuario(
            String userId) {

        logger.info(
                "Buscando citas para el usuario: {}",
                userId
        );

        return citaRepository.findByUserId(userId)
                .stream()
                .map(CitaResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CitaResponse> obtenerCitasPorMedico(
            String medicoId) {

        logger.info(
                "Buscando citas para el médico: {}",
                medicoId
        );

        return citaRepository.findByMedicoId(medicoId)
                .stream()
                .map(CitaResponse::fromEntity)
                .collect(Collectors.toList());
    }
}