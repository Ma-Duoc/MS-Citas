package com.microservicios.mscitas.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ms-historial", url = "http://ms-historial:8085")
public interface HistorialClient {

        /**
         * Crear un registro en el historial médico
         * 
         * @param request DTO con los datos del historial
         * @return Respuesta del servicio de historial
         */
        @PostMapping("/api/historial")
        HistorialResponse crearHistorial(@RequestBody HistorialRequest request);

        /**
         * DTO para la solicitud de creación de historial
         */
        record HistorialRequest(
                        String pacienteRut,
                        Long medicoId,
                        String estado // "AGENDADA", "COMPLETADA", "CANCELADA"
        ) {
        }

        /**
         * DTO para la respuesta del servicio de historial
         */
        record HistorialResponse(
                        Long id,
                        String pacienteRut,
                        Long medicoId,
                        String estado) {
        }
}
