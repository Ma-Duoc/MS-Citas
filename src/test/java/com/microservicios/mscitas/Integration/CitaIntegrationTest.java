package com.microservicios.mscitas.Integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservicios.mscitas.client.HistorialClient;
import com.microservicios.mscitas.client.NotificationClient;
import com.microservicios.mscitas.dto.CitaRequest;
import com.microservicios.mscitas.model.Cita;
import com.microservicios.mscitas.repository.CitaRepository;
import com.microservicios.mscitas.service.DisponibilidadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CitaIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private CitaRepository citaRepository;

        @MockBean
        private DisponibilidadService disponibilidadService;

        @MockBean
        private NotificationClient notificationClient;

        @MockBean
        private HistorialClient historialClient;

        @BeforeEach
        void setUp() {
                citaRepository.deleteAll();
        }

        @Test
        void healthCheck_DebeRetornarUP() throws Exception {

                mockMvc.perform(get("/api/citas/health"))
                                .andExpect(status().isOk())
                                .andExpect(content().string("UP - ms-citas"));
        }

        @Test
        void obtenerCita_DebeRetornarCita() throws Exception {

                Cita cita = Cita.builder()
                                .userId("12345678")
                                .userNombre("Juan Perez")
                                .medicoId("1")
                                .medicoNombre("Dr. Carlos")
                                .especialidad("Cardiologia")
                                .salaId(1L)
                                .salaNombre("Sala 1")
                                .fechaHora(LocalDateTime.now().plusDays(1))
                                .motivo("Control")
                                .build();

                cita = citaRepository.save(cita);

                mockMvc.perform(get("/api/citas/" + cita.getId()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(cita.getId()))
                                .andExpect(jsonPath("$.userId").value("12345678"));
        }

        @Test
        void crearCita_DebeRetornar201() throws Exception {

                doNothing().when(disponibilidadService)
                                .validarDisponibilidad(any(), any(), any(), any());

                when(notificationClient.sendNotification(any()))
                                .thenReturn(true);

                when(historialClient.crearHistorial(any()))
                                .thenReturn(
                                                new HistorialClient.HistorialResponse(
                                                                1L,
                                                                "12345678",
                                                                1L,
                                                                "AGENDADA"));

                CitaRequest request = new CitaRequest(
                                "12345678",
                                "Juan Perez",
                                "1",
                                "Dr. Carlos",
                                "Cardiologia",
                                1L,
                                "Sala 1",
                                LocalDateTime.now().plusDays(1),
                                "Control general");

                mockMvc.perform(
                                post("/api/citas")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.userId").value("12345678"))
                                .andExpect(jsonPath("$.medicoId").value("1"));
        }
}