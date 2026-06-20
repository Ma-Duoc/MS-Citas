package com.microservicios.mscitas.Exception;

import com.microservicios.mscitas.client.MedicoClient;
import com.microservicios.mscitas.client.PacienteClient;
import com.microservicios.mscitas.client.SalaClient;
import com.microservicios.mscitas.exception.CitaException;
import com.microservicios.mscitas.exception.DisponibilidadException;
import com.microservicios.mscitas.service.DisponibilidadService;

import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DisponibilidadServiceTest {

        @Mock
        private MedicoClient medicoClient;

        @Mock
        private SalaClient salaClient;

        @Mock
        private PacienteClient pacienteClient;

        @InjectMocks
        private DisponibilidadService disponibilidadService;

        private LocalDateTime fechaHora;

        @BeforeEach
        void setUp() {
                fechaHora = LocalDateTime.now().plusDays(1);
        }

        @Test
        void validarDisponibilidad_DatosValidos_NoDebeLanzarExcepcion() {

                when(pacienteClient.obtenerPaciente("12345678"))
                                .thenReturn(new PacienteClient.PacienteResponse(
                                                "12345678",
                                                "Juan",
                                                "Perez",
                                                "juan@test.com",
                                                true));

                when(medicoClient.obtenerMedico(1L))
                                .thenReturn(new MedicoClient.MedicoResponse(
                                                1L,
                                                "Carlos",
                                                "Gomez",
                                                "Cardiologia"));

                when(salaClient.obtenerSala(1L))
                                .thenReturn(new SalaClient.SalaResponse(
                                                1L,
                                                "Sala 1",
                                                "Consulta",
                                                10));

                assertDoesNotThrow(() -> disponibilidadService.validarDisponibilidad(
                                "12345678",
                                "1",
                                1L,
                                fechaHora));
        }

        @Test
        void validarDisponibilidad_PacienteNull_DebeLanzarCitaException() {

                when(pacienteClient.obtenerPaciente("12345678"))
                                .thenReturn(null);

                assertThrows(
                                CitaException.class,
                                () -> disponibilidadService.validarDisponibilidad(
                                                "12345678",
                                                "1",
                                                1L,
                                                fechaHora));
        }

        @Test
        void validarDisponibilidad_MedicoNull_DebeLanzarCitaException() {

                when(pacienteClient.obtenerPaciente("12345678"))
                                .thenReturn(new PacienteClient.PacienteResponse(
                                                "12345678",
                                                "Juan",
                                                "Perez",
                                                "correo@test.com",
                                                true));

                when(medicoClient.obtenerMedico(1L))
                                .thenReturn(null);

                assertThrows(
                                CitaException.class,
                                () -> disponibilidadService.validarDisponibilidad(
                                                "12345678",
                                                "1",
                                                1L,
                                                fechaHora));
        }

        @Test
        void validarDisponibilidad_MedicoInactivo_DebeLanzarCitaException() {

                when(pacienteClient.obtenerPaciente("12345678"))
                                .thenReturn(new PacienteClient.PacienteResponse(
                                                "12345678",
                                                "Juan",
                                                "Perez",
                                                "correo@test.com",
                                                true));

                when(medicoClient.obtenerMedico(1L))
                                .thenReturn(new MedicoClient.MedicoResponse(
                                                1L,
                                                "Carlos",
                                                "Gomez",
                                                "Cardiologia"));

                assertThrows(
                                CitaException.class,
                                () -> disponibilidadService.validarDisponibilidad(
                                                "12345678",
                                                "1",
                                                1L,
                                                fechaHora));
        }

        @Test
        void validarDisponibilidad_SalaNull_DebeLanzarCitaException() {

                when(pacienteClient.obtenerPaciente("12345678"))
                                .thenReturn(new PacienteClient.PacienteResponse(
                                                "12345678",
                                                "Juan",
                                                "Perez",
                                                "correo@test.com",
                                                true));

                when(medicoClient.obtenerMedico(1L))
                                .thenReturn(new MedicoClient.MedicoResponse(
                                                1L,
                                                "Carlos",
                                                "Gomez",
                                                "Cardiologia"));

                when(salaClient.obtenerSala(1L))
                                .thenReturn(null);

                assertThrows(
                                CitaException.class,
                                () -> disponibilidadService.validarDisponibilidad(
                                                "12345678",
                                                "1",
                                                1L,
                                                fechaHora));
        }

        @Test
        void validarDisponibilidad_ErrorPaciente_DebeLanzarDisponibilidadException() {

                when(pacienteClient.obtenerPaciente("12345678"))
                                .thenThrow(mock(FeignException.class));

                assertThrows(
                                DisponibilidadException.class,
                                () -> disponibilidadService.validarDisponibilidad(
                                                "12345678",
                                                "1",
                                                1L,
                                                fechaHora));
        }

        @Test
        void validarDisponibilidad_ErrorMedico_DebeLanzarDisponibilidadException() {

                when(pacienteClient.obtenerPaciente("12345678"))
                                .thenReturn(new PacienteClient.PacienteResponse(
                                                "12345678",
                                                "Juan",
                                                "Perez",
                                                "correo@test.com",
                                                true));

                when(medicoClient.obtenerMedico(1L))
                                .thenThrow(mock(FeignException.class));

                assertThrows(
                                DisponibilidadException.class,
                                () -> disponibilidadService.validarDisponibilidad(
                                                "12345678",
                                                "1",
                                                1L,
                                                fechaHora));
        }

        @Test
        void validarDisponibilidad_ErrorSala_DebeLanzarDisponibilidadException() {

                when(pacienteClient.obtenerPaciente("12345678"))
                                .thenReturn(new PacienteClient.PacienteResponse(
                                                "12345678",
                                                "Juan",
                                                "Perez",
                                                "correo@test.com",
                                                true));

                when(medicoClient.obtenerMedico(1L))
                                .thenReturn(new MedicoClient.MedicoResponse(
                                                1L,
                                                "Carlos",
                                                "Gomez",
                                                "Cardiologia"));

                when(salaClient.obtenerSala(1L))
                                .thenThrow(mock(FeignException.class));

                assertThrows(
                                DisponibilidadException.class,
                                () -> disponibilidadService.validarDisponibilidad(
                                                "12345678",
                                                "1",
                                                1L,
                                                fechaHora));
        }
}