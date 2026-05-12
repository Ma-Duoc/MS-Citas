package com.microservicios.mscitas.exception;

public class DisponibilidadException extends CitaException {
    
    public DisponibilidadException(String message) {
        super(message);
    }
    
    public DisponibilidadException(String message, Throwable cause) {
        super(message, cause);
    }
}
