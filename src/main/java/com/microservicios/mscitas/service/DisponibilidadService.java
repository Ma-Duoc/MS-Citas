package com.microservicios.mscitas.service;

import com.microservicios.mscitas.client.MedicoClient;
import com.microservicios.mscitas.client.PacienteClient;
import com.microservicios.mscitas.client.SalaClient;
import com.microservicios.mscitas.exception.CitaException;
import com.microservicios.mscitas.exception.DisponibilidadException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.circuitbreaker.NoFallbackAvailableException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DisponibilidadService {

    private static final Logger logger =
            LoggerFactory.getLogger(DisponibilidadService.class);

    private final MedicoClient medicoClient;
    private final SalaClient salaClient;
    private final PacienteClient pacienteClient;

    // =========================
    // VALIDAR TODO
    // =========================
    public void validarDisponibilidad(
            String userId,
            String medicoId,
            Long salaId,
            LocalDateTime fechaHora) {

        validarPaciente(userId);
        validarMedico(medicoId);
        validarSala(salaId);
    }

    // =========================
    // PACIENTE
    // =========================
    private void validarPaciente(String userId) {

        try {

            logger.info(
                    "Validando paciente: {}",
                    userId
            );

            PacienteClient.PacienteResponse paciente =
                    pacienteClient.obtenerPaciente(userId);

            if (paciente == null) {

                throw new CitaException(
                        "Paciente no registrado en el sistema"
                );
            }

            logger.info(
                    "Paciente válido: {} {}",
                    paciente.nombre(),
                    paciente.apellido()
            );

        } catch (FeignException.NotFound e) {

            logger.error(
                    "Paciente no encontrado: {}",
                    userId
            );

            throw new CitaException(
                    "Paciente no registrado en el sistema"
            );

        } catch (NoFallbackAvailableException e) {

            logger.error(
                    "CircuitBreaker paciente: {}",
                    e.getMessage()
            );

            if (e.getCause() instanceof FeignException.NotFound) {

                throw new CitaException(
                        "Paciente no registrado en el sistema"
                );
            }

            throw new DisponibilidadException(
                    "Servicio de pacientes no disponible"
            );

        } catch (FeignException e) {

            logger.error(
                    "Error en ms-pacientes: {}",
                    e.getMessage()
            );

            throw new DisponibilidadException(
                    "Servicio de pacientes no disponible"
            );
        }
    }

    // =========================
    // MÉDICO
    // =========================
    private void validarMedico(String medicoId) {

        try {

            logger.info(
                    "Validando médico: {}",
                    medicoId
            );

            MedicoClient.MedicoResponse medico =
                    medicoClient.obtenerMedico(
                            Long.parseLong(medicoId)
                    );

            if (medico == null) {

                throw new CitaException(
                        "Médico no registrado en el sistema"
                );
            }

            if (!medico.activo()) {

                throw new CitaException(
                        "Médico inactivo"
                );
            }

            logger.info(
                    "Médico válido: {} {}",
                    medico.nombre(),
                    medico.apellido()
            );

        } catch (FeignException.NotFound e) {

            logger.error(
                    "Médico no encontrado: {}",
                    medicoId
            );

            throw new CitaException(
                    "Médico no registrado en el sistema"
            );

        } catch (NoFallbackAvailableException e) {

            logger.error(
                    "CircuitBreaker médico: {}",
                    e.getMessage()
            );

            if (e.getCause() instanceof FeignException.NotFound) {

                throw new CitaException(
                        "Médico no registrado en el sistema"
                );
            }

            throw new DisponibilidadException(
                    "Servicio de médicos no disponible"
            );

        } catch (FeignException e) {

            logger.error(
                    "Error en ms-medicos: {}",
                    e.getMessage()
            );

            throw new DisponibilidadException(
                    "Servicio de médicos no disponible"
            );
        }
    }

    // =========================
    // SALA
    // =========================
    private void validarSala(Long salaId) {

        try {

            logger.info(
                    "Validando sala: {}",
                    salaId
            );

            SalaClient.SalaResponse sala =
                    salaClient.obtenerSala(salaId);

            if (sala == null) {

                throw new CitaException(
                        "Sala no registrada en el sistema"
                );
            }

            logger.info(
                    "Sala válida: {}",
                    sala.nombre()
            );

        } catch (FeignException.NotFound e) {

            logger.error(
                    "Sala no encontrada: {}",
                    salaId
            );

            throw new CitaException(
                    "Sala no registrada en el sistema"
            );

        } catch (NoFallbackAvailableException e) {

            logger.error(
                    "CircuitBreaker sala: {}",
                    e.getMessage()
            );

            Throwable cause = e.getCause();

            if (cause instanceof FeignException feignException) {

                if (feignException.status() == 404) {

                    throw new CitaException(
                            "Sala no registrada en el sistema"
                    );
                }
            }

            throw new DisponibilidadException(
                    "Servicio de salas no disponible"
            );

        } catch (FeignException e) {

            logger.error(
                    "Error en ms-salas: {}",
                    e.getMessage()
            );

            throw new DisponibilidadException(
                    "Servicio de salas no disponible"
            );
        }
    }
}