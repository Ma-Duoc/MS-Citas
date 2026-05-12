package com.microservicios.mscitas.fallback;

import com.microservicios.mscitas.exception.DisponibilidadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class FallbackHandler {

    private static final Logger logger = LoggerFactory.getLogger(FallbackHandler.class);

    /**
     * Fallback para validación de disponibilidad de médico
     * IMPORTANTE: NO permite continuar el flujo - siempre lanza excepción
     * 
     * @param medicoId ID del médico que se estaba validando
     * @param fechaHora Fecha y hora de la cita
     * @param exception Excepción original que causó el fallback
     * @throws DisponibilidadException Siempre lanza esta excepción para detener el proceso
     */
    public void fallbackMedico(String medicoId, LocalDateTime fechaHora, Exception exception) {
        logger.error("=== FALLBACK CRÍTICO - SERVICIO MÉDICOS ===");
        logger.error("Médico ID: {}", medicoId);
        logger.error("Fecha/Hora cita: {}", fechaHora);
        logger.error("Tipo de error: {}", exception.getClass().getSimpleName());
        logger.error("Mensaje de error: {}", exception.getMessage());
        logger.error("Causa raíz: {}", exception.getCause() != null ? exception.getCause().getMessage() : "No disponible");
        
        // Logging adicional para troubleshooting
        if (exception.getCause() != null) {
            logger.error("Stack trace de la causa raíz:", exception.getCause());
        }
        
        logger.error("ACCIÓN: DETENIENDO creación de cita - validación de médico falló");
        logger.error("================================================");
        
        // Lanzar excepción controlada para detener el flujo
        String errorMsg = String.format(
            "FALLA CRÍTICA: No se pudo validar disponibilidad del médico %s para la fecha %s. " +
            "Causa: %s. La cita NO será creada por seguridad.",
            medicoId, fechaHora, exception.getMessage()
        );
        
        throw new DisponibilidadException(errorMsg, exception);
    }

    /**
     * Fallback para validación de disponibilidad de sala
     * IMPORTANTE: NO permite continuar el flujo - siempre lanza excepción
     * 
     * @param salaId ID de la sala que se estaba validando
     * @param fechaHora Fecha y hora de la cita
     * @param exception Excepción original que causó el fallback
     * @throws DisponibilidadException Siempre lanza esta excepción para detener el proceso
     */
    public void fallbackSala(Long salaId, LocalDateTime fechaHora, Exception exception) {
        logger.error("=== FALLBACK CRÍTICO - SERVICIO SALAS ===");
        logger.error("Sala ID: {}", salaId);
        logger.error("Fecha/Hora cita: {}", fechaHora);
        logger.error("Tipo de error: {}", exception.getClass().getSimpleName());
        logger.error("Mensaje de error: {}", exception.getMessage());
        logger.error("Causa raíz: {}", exception.getCause() != null ? exception.getCause().getMessage() : "No disponible");
        
        // Logging adicional para troubleshooting
        if (exception.getCause() != null) {
            logger.error("Stack trace de la causa raíz:", exception.getCause());
        }
        
        logger.error("ACCIÓN: DETENIENDO creación de cita - validación de sala falló");
        logger.error("==============================================");
        
        // Lanzar excepción controlada para detener el flujo
        String errorMsg = String.format(
            "FALLA CRÍTICA: No se pudo validar disponibilidad de la sala %s para la fecha %s. " +
            "Causa: %s. La cita NO será creada por seguridad.",
            salaId, fechaHora, exception.getMessage()
        );
        
        throw new DisponibilidadException(errorMsg, exception);
    }

    /**
     * Fallback para obtener información de médico
     * NO permite continuar - lanza excepción
     * 
     * @param medicoId ID del médico
     * @param exception Excepción original
     * @throws DisponibilidadException Siempre lanza excepción
     */
    public void fallbackObtenerMedico(String medicoId, Exception exception) {
        logger.error("=== FALLBACK CRÍTICO - OBTENER MÉDICO ===");
        logger.error("Médico ID: {}", medicoId);
        logger.error("Tipo de error: {}", exception.getClass().getSimpleName());
        logger.error("Mensaje de error: {}", exception.getMessage());
        logger.error("ACCIÓN: Servicio de médicos no disponible");
        logger.error("========================================");
        
        String errorMsg = String.format(
            "FALLA CRÍTICA: No se puede obtener información del médico %s. " +
            "Causa: %s. Servicio no disponible.",
            medicoId, exception.getMessage()
        );
        
        throw new DisponibilidadException(errorMsg, exception);
    }

    /**
     * Fallback para obtener información de sala
     * NO permite continuar - lanza excepción
     * 
     * @param salaId ID de la sala
     * @param exception Excepción original
     * @throws DisponibilidadException Siempre lanza excepción
     */
    public void fallbackObtenerSala(Long salaId, Exception exception) {
        logger.error("=== FALLBACK CRÍTICO - OBTENER SALA ===");
        logger.error("Sala ID: {}", salaId);
        logger.error("Tipo de error: {}", exception.getClass().getSimpleName());
        logger.error("Mensaje de error: {}", exception.getMessage());
        logger.error("ACCIÓN: Servicio de salas no disponible");
        logger.error("=======================================");
        
        String errorMsg = String.format(
            "FALLA CRÍTICA: No se puede obtener información de la sala %s. " +
            "Causa: %s. Servicio no disponible.",
            salaId, exception.getMessage()
        );
        
        throw new DisponibilidadException(errorMsg, exception);
    }

    /**
     * Fallback genérico para cualquier error crítico
     * 
     * @param servicio Nombre del servicio que falló
     * @param identificador Identificador del recurso (médico/sala)
     * @param fechaHora Fecha y hora de la cita
     * @param exception Excepción original
     * @throws DisponibilidadException Siempre lanza excepción
     */
    public void fallbackGenerico(String servicio, String identificador, LocalDateTime fechaHora, Exception exception) {
        logger.error("=== FALLBACK CRÍTICO - SERVICIO GENÉRICO ===");
        logger.error("Servicio: {}", servicio);
        logger.error("Identificador: {}", identificador);
        logger.error("Fecha/Hora: {}", fechaHora);
        logger.error("Tipo de error: {}", exception.getClass().getSimpleName());
        logger.error("Mensaje de error: {}", exception.getMessage());
        logger.error("ACCIÓN: DETENIENDO operación por falla crítica");
        logger.error("============================================");
        
        String errorMsg = String.format(
            "FALLA CRÍTICA: Error en servicio %s para el recurso %s en fecha %s. " +
            "Causa: %s. Operación detenida por seguridad.",
            servicio, identificador, fechaHora, exception.getMessage()
        );
        
        throw new DisponibilidadException(errorMsg, exception);
    }
}
