package com.microservicios.mscitas.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ms-salas", url = "http://localhost:8084")
public interface SalaClient {

    @GetMapping("api/salas/{id}")
    SalaResponse obtenerSala(@PathVariable Long id);

    record SalaResponse(
            Long id,
            String nombre,
            String tipo,
            Integer capacidad) {
    }
}