package com.microservicios.mscitas.events;

import java.time.LocalDateTime;

public record CitaCreadaEvent(
        Long citaId,
        String userId,
        String userNombre,
        String medicoId,
        String medicoNombre,
        String especialidad,
        Long salaId,
        String salaNombre,
        LocalDateTime fechaHora,
        LocalDateTime fechaCreacion
) {}
