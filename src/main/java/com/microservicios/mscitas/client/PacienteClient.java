package com.microservicios.mscitas.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ms-pacientes", url = "http://localhost:8082")
public interface PacienteClient {

    @GetMapping("/api/pacientes/{rut}")
    PacienteResponse obtenerPaciente(@PathVariable String rut);

    record PacienteResponse(
            String rut,
            String nombre,
            String apellido,
            String email,
            boolean activo
    ) {
    }
}