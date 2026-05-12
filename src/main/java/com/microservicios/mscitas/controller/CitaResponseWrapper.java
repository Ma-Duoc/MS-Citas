package com.microservicios.mscitas.controller;

import com.microservicios.mscitas.model.dto.CitaResponse;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CitaResponseWrapper {
    private CitaResponse cita;
    private String mensaje;
}
