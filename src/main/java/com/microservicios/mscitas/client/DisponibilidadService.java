package com.microservicios.mscitas.client;

import com.microservicios.mscitas.exception.DisponibilidadException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DisponibilidadService {

    private static final Logger logger = LoggerFactory.getLogger(DisponibilidadService.class);

    private final MedicoClient medicoClient;
    private final SalaClient salaClient;
    private final PacienteClient pacienteClient;

    public DisponibilidadService(
            MedicoClient medicoClient,
            SalaClient salaClient,
            PacienteClient pacienteClient) {

        this.medicoClient = medicoClient;
        this.salaClient = salaClient;
        this.pacienteClient = pacienteClient;
    }

    // =========================
    // VALIDAR PACIENTE
    // =========================
    @CircuitBreaker(name = "pacientes", fallbackMethod = "fallbackPaciente")
    @Retry(name = "pacientes")
    public boolean validarPaciente(String userId) {

        logger.info("Validando paciente RUT: {}", userId);

        PacienteClient.PacienteResponse paciente =
                pacienteClient.obtenerPaciente(userId);

        if (paciente == null) {
            throw new DisponibilidadException("El paciente no existe: " + userId);
        }

        logger.info("Paciente válido: {} {}",
                paciente.nombre(),
                paciente.apellido());

        return true;
    }

    // =========================
    // VALIDAR MÉDICO
    // =========================
    @CircuitBreaker(name = "medicos", fallbackMethod = "fallbackMedico")
    @Retry(name = "medicos")
    public boolean validarDisponibilidadMedico(String medicoId, LocalDateTime fechaHora) {

        logger.info("Validando médico ID: {}", medicoId);

        MedicoClient.MedicoResponse medico =
                medicoClient.obtenerMedico(Long.parseLong(medicoId));

        if (medico == null) {
            throw new DisponibilidadException("El médico no existe: " + medicoId);
        }

        if (!medico.activo()) {
            throw new DisponibilidadException("El médico está inactivo");
        }

        logger.info("Médico válido: {} {}",
                medico.nombre(),
                medico.apellido());

        return true;
    }

    // =========================
    // VALIDAR SALA
    // =========================
    @CircuitBreaker(name = "salas", fallbackMethod = "fallbackSala")
    @Retry(name = "salas")
    public boolean validarDisponibilidadSala(Long salaId, LocalDateTime fechaHora) {

        logger.info("Validando sala ID: {}", salaId);

        SalaClient.SalaResponse sala =
                salaClient.obtenerSala(salaId);

        if (sala == null) {
            throw new DisponibilidadException("La sala no existe: " + salaId);
        }

        logger.info("Sala válida: {}", sala.nombre());

        return true;
    }

    // =========================
    // FALLBACK PACIENTE
    // =========================
    private boolean fallbackPaciente(String userId, Exception ex) {

        logger.error("Error paciente service: {}", ex.getMessage());

        throw new DisponibilidadException(
                "Error al validar paciente: " + userId,
                ex
        );
    }

    // =========================
    // FALLBACK MÉDICO
    // =========================
    private boolean fallbackMedico(
            String medicoId,
            LocalDateTime fechaHora,
            Exception ex) {

        logger.error("Error médico service: {}", ex.getMessage());

        throw new DisponibilidadException(
                "Error al validar médico: " + medicoId,
                ex
        );
    }

    // =========================
    // FALLBACK SALA
    // =========================
    private boolean fallbackSala(
            Long salaId,
            LocalDateTime fechaHora,
            Exception ex) {

        logger.error("Error sala service: {}", ex.getMessage());

        throw new DisponibilidadException(
                "Error al validar sala: " + salaId,
                ex
        );
    }
}