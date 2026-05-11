package com.microservicios.mscitas.dto;

import java.time.LocalDateTime;

public record CitaResponse(
        Long id,
        String userId,
        String userNombre,
        String medicoId,
        String medicoNombre,
        String especialidad,
        Long salaId,
        String salaNombre,
        LocalDateTime fechaHora,
        String estado
) {
    public static CitaResponse fromEntity(com.microservicios.mscitas.model.Cita cita) {
        return new CitaResponse(
                cita.getId(),
                cita.getUserId(),
                cita.getUserNombre(),
                cita.getMedicoId(),
                cita.getMedicoNombre(),
                cita.getEspecialidad(),
                cita.getSalaId(),
                cita.getSalaNombre(),
                cita.getFechaHora(),
                "PROGRAMADA"
        );
    }
}
