package com.microservicios.mscitas.service;

import com.microservicios.mscitas.client.HistorialClient;
import com.microservicios.mscitas.client.NotificationClient;
import com.microservicios.mscitas.dto.CitaRequest;
import com.microservicios.mscitas.dto.CitaResponse;
import com.microservicios.mscitas.exception.CitaException;
import com.microservicios.mscitas.model.Cita;
import com.microservicios.mscitas.repository.CitaRepository;
import com.microservicios.mscitas.service.CitaService;
import com.microservicios.mscitas.service.DisponibilidadService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CitaServiceTest {

        @Mock
        private CitaRepository citaRepository;

        @Mock
        private DisponibilidadService disponibilidadService;

        @Mock
        private NotificationClient notificationClient;

        @Mock
        private HistorialClient historialClient;

        @InjectMocks
        private CitaService citaService;

        private Cita cita;
        private CitaRequest request;

        @BeforeEach
        void setUp() {

                request = new CitaRequest(
                                "11111111-1",
                                "Juan Perez",
                                "1",
                                "Dr House",
                                "Cardiología",
                                10L,
                                "Sala A",
                                LocalDateTime.now().plusDays(1),
                                "Control médico");

                cita = Cita.builder()
                                .id(1L)
                                .userId("11111111-1")
                                .userNombre("Juan Perez")
                                .medicoId("1")
                                .medicoNombre("Dr House")
                                .especialidad("Cardiología")
                                .salaId(10L)
                                .salaNombre("Sala A")
                                .fechaHora(request.fechaHora())
                                .motivo("Control médico")
                                .build();
        }

        @Test
        void crearCita_DebeCrearCorrectamente() {

                when(citaRepository.existsByMedicoIdAndFechaHora(
                                request.medicoId(),
                                request.fechaHora()))
                                .thenReturn(false);

                when(citaRepository.existsBySalaIdAndFechaHora(
                                request.salaId(),
                                request.fechaHora()))
                                .thenReturn(false);

                when(citaRepository.save(any(Cita.class)))
                                .thenReturn(cita);

                when(notificationClient.sendNotification(any()))
                                .thenReturn(true);

                when(historialClient.crearHistorial(any()))
                                .thenReturn(
                                                new HistorialClient.HistorialResponse(
                                                                1L,
                                                                "11111111-1",
                                                                1L,
                                                                "AGENDADA"));

                CitaResponse resultado = citaService.crearCita(request);

                assertNotNull(resultado);
                assertEquals(1L, resultado.id());
                assertEquals("11111111-1", resultado.userId());

                verify(disponibilidadService, times(1))
                                .validarDisponibilidad(
                                                request.userId(),
                                                request.medicoId(),
                                                request.salaId(),
                                                request.fechaHora());

                verify(citaRepository, times(1))
                                .save(any(Cita.class));
        }

        @Test
        void crearCita_DebeLanzarExcepcionSiMedicoOcupado() {

                when(citaRepository.existsByMedicoIdAndFechaHora(
                                request.medicoId(),
                                request.fechaHora()))
                                .thenReturn(true);

                assertThrows(
                                CitaException.class,
                                () -> citaService.crearCita(request));

                verify(citaRepository, never())
                                .save(any());
        }

        @Test
        void crearCita_DebeLanzarExcepcionSiSalaOcupada() {

                when(citaRepository.existsByMedicoIdAndFechaHora(
                                request.medicoId(),
                                request.fechaHora()))
                                .thenReturn(false);

                when(citaRepository.existsBySalaIdAndFechaHora(
                                request.salaId(),
                                request.fechaHora()))
                                .thenReturn(true);

                assertThrows(
                                CitaException.class,
                                () -> citaService.crearCita(request));

                verify(citaRepository, never())
                                .save(any());
        }

        @Test
        void obtenerCita_DebeRetornarCita() {

                when(citaRepository.findById(1L))
                                .thenReturn(Optional.of(cita));

                CitaResponse resultado = citaService.obtenerCita(1L);

                assertNotNull(resultado);
                assertEquals(1L, resultado.id());

                verify(citaRepository, times(1))
                                .findById(1L);
        }

        @Test
        void obtenerCita_DebeLanzarExcepcionSiNoExiste() {

                when(citaRepository.findById(99L))
                                .thenReturn(Optional.empty());

                assertThrows(
                                CitaException.class,
                                () -> citaService.obtenerCita(99L));

                verify(citaRepository, times(1))
                                .findById(99L);
        }

        @Test
        void obtenerCitasPorUsuario_DebeRetornarLista() {

                when(citaRepository.findByUserId("11111111-1"))
                                .thenReturn(List.of(cita));

                List<CitaResponse> resultado = citaService.obtenerCitasPorUsuario("11111111-1");

                assertEquals(1, resultado.size());
                assertEquals("11111111-1",
                                resultado.get(0).userId());

                verify(citaRepository, times(1))
                                .findByUserId("11111111-1");
        }

        @Test
        void obtenerCitasPorMedico_DebeRetornarLista() {

                when(citaRepository.findByMedicoId("1"))
                                .thenReturn(List.of(cita));

                List<CitaResponse> resultado = citaService.obtenerCitasPorMedico("1");

                assertEquals(1, resultado.size());
                assertEquals("1",
                                resultado.get(0).medicoId());

                verify(citaRepository, times(1))
                                .findByMedicoId("1");
        }
}
