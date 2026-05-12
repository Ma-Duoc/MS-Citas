package com.microservicios.mscitas.controller;

import com.microservicios.mscitas.model.dto.CitaResponse;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CitasResponseWrapper {
    private List<CitaResponse> citas;
    private int cantidad;
    private String mensaje;
}
