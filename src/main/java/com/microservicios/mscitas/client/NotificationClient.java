package com.microservicios.mscitas.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ms-notificaciones", url = "http://localhost:8084")
public interface NotificationClient {

    /**
     * Envía una notificación
     * 
     * @param request DTO con los datos de la notificación
     * @return true si el envío fue exitoso
     */
    @PostMapping("/api/notificaciones/enviar")
    boolean sendNotification(@RequestBody NotificationRequest request);

    // DTO para la solicitud de notificación
    record NotificationRequest(
            String tipo,
            String destino,
            String mensaje) {
    }
}
