package com.microservicios.mscitas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Future;
import java.time.LocalDateTime;

public record CitaRequest(
        @NotBlank(message = "El ID del usuario es obligatorio")
        String userId,
        
        @NotBlank(message = "El nombre del usuario es obligatorio")
        String userNombre,
        
        @NotBlank(message = "El ID del médico es obligatorio")
        String medicoId,
        
        @NotBlank(message = "El nombre del médico es obligatorio")
        String medicoNombre,
        
        @NotBlank(message = "La especialidad es obligatoria")
        String especialidad,
        
        @NotNull(message = "El ID de la sala es obligatorio")
        Long salaId,
        
        @NotBlank(message = "El nombre de la sala es obligatorio")
        String salaNombre,
        
        @NotNull(message = "La fecha y hora son obligatorias")
        @Future(message = "La fecha y hora deben ser futuras")
        LocalDateTime fechaHora
) {}
