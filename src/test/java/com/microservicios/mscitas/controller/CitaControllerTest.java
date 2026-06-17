package com.microservicios.mscitas.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservicios.mscitas.dto.CitaRequest;
import com.microservicios.mscitas.dto.CitaResponse;
import com.microservicios.mscitas.service.CitaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CitaController.class)
class CitaControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private CitaService citaService;

        @Autowired
        private ObjectMapper objectMapper;

        private CitaRequest crearRequest() {
                return new CitaRequest(
                                "12345678-9",
                                "Juan Perez",
                                "1",
                                "Dr. House",
                                "Medicina General",
                                1L,
                                "Sala 101",
                                LocalDateTime.now().plusDays(1),
                                "Control médico");
        }

        private CitaResponse crearResponse() {
                return new CitaResponse(
                                1L,
                                "12345678-9",
                                "Juan Perez",
                                "1",
                                "Dr. House",
                                "Medicina General",
                                1L,
                                "Sala 101",
                                LocalDateTime.now().plusDays(1),
                                "PROGRAMADA");
        }

        @Test
        void agregarCita_DebeRetornar201() throws Exception {

                CitaRequest request = crearRequest();
                CitaResponse response = crearResponse();

                when(citaService.crearCita(any(CitaRequest.class)))
                                .thenReturn(response);

                mockMvc.perform(post("/api/citas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.userId").value("12345678-9"))
                                .andExpect(jsonPath("$.estado").value("PROGRAMADA"));
        }

        @Test
        void obtenerCita_DebeRetornar200() throws Exception {

                CitaResponse response = crearResponse();

                when(citaService.obtenerCita(1L))
                                .thenReturn(response);

                mockMvc.perform(get("/api/citas/1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.userId").value("12345678-9"));
        }

        @Test
        void obtenerCitasPorUsuario_DebeRetornar200() throws Exception {

                CitaResponse response = crearResponse();

                when(citaService.obtenerCitasPorUsuario("12345678-9"))
                                .thenReturn(List.of(response));

                mockMvc.perform(get("/api/citas/usuario/12345678-9"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].userId")
                                                .value("12345678-9"));
        }

        @Test
        void obtenerCitasPorMedico_DebeRetornar200() throws Exception {

                CitaResponse response = crearResponse();

                when(citaService.obtenerCitasPorMedico("1"))
                                .thenReturn(List.of(response));

                mockMvc.perform(get("/api/citas/medico/1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].medicoId")
                                                .value("1"));
        }

        @Test
        void healthCheck_DebeRetornar200() throws Exception {

                mockMvc.perform(get("/api/citas/health"))
                                .andExpect(status().isOk())
                                .andExpect(content().string("UP - ms-citas"));
        }
}
