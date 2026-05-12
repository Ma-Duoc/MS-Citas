package com.microservicios.mscitas.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ms-medicos", url = "${ms.medicos.url}")
public interface MedicoClient {

        @GetMapping("/api/medicos/{id}")
        MedicoResponse obtenerMedico(@PathVariable Long id);

        record MedicoResponse(
                        Long id,
                        String nombre,
                        String apellido,
                        String especialidad,
                        boolean activo) {
        }
}