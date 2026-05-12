package com.microservicios.mscitas.service;

import com.microservicios.mscitas.client.DisponibilidadService;
import com.microservicios.mscitas.client.NotificationClient;
import com.microservicios.mscitas.exception.CitaException;
import com.microservicios.mscitas.exception.DisponibilidadException;
import com.microservicios.mscitas.model.Cita;
import com.microservicios.mscitas.model.dto.CitaRequest;
import com.microservicios.mscitas.model.dto.CitaResponse;
import com.microservicios.mscitas.repository.CitaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CitaService {

    private static final Logger logger = LoggerFactory.getLogger(CitaService.class);

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
    public CitaResponse crearCita(CitaRequest request) {

        logger.info(
                "Iniciando creación de cita para usuario {} con médico {} en sala {}",
                request.userId(),
                request.medicoId(),
                request.salaId()
        );

        try {

            // =========================
            // 1. VALIDAR PACIENTE
            // =========================
            logger.info("Validando paciente {}", request.userId());

            boolean pacienteValido =
                    disponibilidadService.validarPaciente(request.userId());

            if (!pacienteValido) {

                String errorMsg = String.format(
                        "El paciente %s no es válido",
                        request.userId()
                );

                logger.error(errorMsg);

                throw new DisponibilidadException(errorMsg);
            }

            // =========================
            // 2. VALIDAR MÉDICO
            // =========================
            logger.info("Validando disponibilidad del médico {}", request.medicoId());

            boolean medicoDisponible =
                    disponibilidadService.validarDisponibilidadMedico(
                            request.medicoId(),
                            request.fechaHora()
                    );

            if (!medicoDisponible) {

                String errorMsg = String.format(
                        "El médico %s no está disponible para la fecha %s",
                        request.medicoId(),
                        request.fechaHora()
                );

                logger.error(errorMsg);

                throw new DisponibilidadException(errorMsg);
            }

            // =========================
            // 3. VALIDAR SALA
            // =========================
            logger.info("Validando disponibilidad de la sala {}", request.salaId());

            boolean salaDisponible =
                    disponibilidadService.validarDisponibilidadSala(
                            request.salaId(),
                            request.fechaHora()
                    );

            if (!salaDisponible) {

                String errorMsg = String.format(
                        "La sala %s no está disponible para la fecha %s",
                        request.salaId(),
                        request.fechaHora()
                );

                logger.error(errorMsg);

                throw new DisponibilidadException(errorMsg);
            }

            // =========================
            // 4. VALIDAR DUPLICADOS
            // =========================
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

            // =========================
            // 5. CREAR ENTIDAD
            // =========================
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

            // =========================
            // 6. ENVIAR NOTIFICACIÓN
            // =========================
            enviarNotificacionCitaCreada(citaGuardada);

            // =========================
            // 7. RESPUESTA
            // =========================
            CitaResponse response =
                    CitaResponse.fromEntity(citaGuardada);

            logger.info("Cita creada exitosamente: {}", response);

            return response;

        } catch (DisponibilidadException e) {

            logger.error(
                    "Error de disponibilidad al crear cita: {}",
                    e.getMessage()
            );

            throw e;

        } catch (Exception e) {

            logger.error(
                    "Error inesperado al crear cita: {}",
                    e.getMessage(),
                    e
            );

            throw new CitaException(
                    "Error al crear la cita: " + e.getMessage(),
                    e
            );
        }
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

    /**
     * Obtener cita por ID
     */
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

    /**
     * Obtener citas por usuario
     */
    @Transactional(readOnly = true)
    public java.util.List<CitaResponse> obtenerCitasPorUsuario(String userId) {

        logger.info("Buscando citas para el usuario: {}", userId);

        return citaRepository.findByUserId(userId)
                .stream()
                .map(CitaResponse::fromEntity)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Obtener citas por médico
     */
    @Transactional(readOnly = true)
    public java.util.List<CitaResponse> obtenerCitasPorMedico(String medicoId) {

        logger.info("Buscando citas para el médico: {}", medicoId);

        return citaRepository.findByMedicoId(medicoId)
                .stream()
                .map(CitaResponse::fromEntity)
                .collect(java.util.stream.Collectors.toList());
    }
}