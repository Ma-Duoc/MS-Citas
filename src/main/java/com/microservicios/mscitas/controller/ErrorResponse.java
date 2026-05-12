package com.microservicios.mscitas.controller;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorResponse {
    private String mensaje;
    private String error;
    private int status;
}
