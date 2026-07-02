package com.microservicios.mscitas.Repository;

import com.microservicios.mscitas.model.Cita;
import com.microservicios.mscitas.repository.CitaRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CitaRepositoryTest {

    @Autowired
    private CitaRepository citaRepository;

    private Cita cita;
    private LocalDateTime fechaPrueba;

    @BeforeEach
    void setUp() {

        fechaPrueba = LocalDateTime.of(
                2026,
                10,
                20,
                10,
                30,
                0
        );

        cita = Cita.builder()
                .userId("12345678-9")
                .userNombre("Juan Perez")
                .medicoId("1")
                .medicoNombre("Dr. House")
                .especialidad("Medicina General")
                .salaId(1L)
                .salaNombre("Sala A")
                .fechaHora(fechaPrueba)
                .motivo("Control")
                .build();

        citaRepository.save(cita);
    }

    @Test
    void findByUserId_DebeRetornarCitasDelUsuario() {

        List<Cita> resultado = citaRepository.findByUserId("12345678-9");

        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.size());
        assertEquals("Juan Perez", resultado.get(0).getUserNombre());
    }

    @Test
    void findByMedicoId_DebeRetornarCitasDelMedico() {

        List<Cita> resultado = citaRepository.findByMedicoId("1");

        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.size());
        assertEquals("Dr. House", resultado.get(0).getMedicoNombre());
    }

    @Test
    void findBySalaId_DebeRetornarCitasDeLaSala() {

        List<Cita> resultado = citaRepository.findBySalaId(1L);

        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.size());
    }

    @Test
    void findByEspecialidad_DebeRetornarCitasDeLaEspecialidad() {

        List<Cita> resultado = citaRepository.findByEspecialidad("Medicina General");

        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.size());
    }

    @Test
    void existsByMedicoIdAndFechaHora_DebeRetornarTrue() {

        boolean existe = citaRepository.existsByMedicoIdAndFechaHora(
                "1",
                fechaPrueba);

        assertTrue(existe);
    }

    @Test
    void existsBySalaIdAndFechaHora_DebeRetornarTrue() {

        boolean existe = citaRepository.existsBySalaIdAndFechaHora(
                1L,
                fechaPrueba);

        assertTrue(existe);
    }

    @Test
    void findByMedicoIdAndFechaHoraBetween_DebeRetornarResultados() {

        LocalDateTime inicio = fechaPrueba.minusHours(1);
        LocalDateTime fin = fechaPrueba.plusHours(1);

        List<Cita> resultado = citaRepository.findByMedicoIdAndFechaHoraBetween(
                "1",
                inicio,
                fin);

        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.size());
    }

    @Test
    void findBySalaIdAndFechaHoraBetween_DebeRetornarResultados() {

        LocalDateTime inicio = fechaPrueba.minusHours(1);
        LocalDateTime fin = fechaPrueba.plusHours(1);

        List<Cita> resultado = citaRepository.findBySalaIdAndFechaHoraBetween(
                1L,
                inicio,
                fin);

        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.size());
    }

    @Test
    void countByMedicoIdAndFechaHoraBetween_DebeContarCorrectamente() {

        LocalDateTime inicio = fechaPrueba.minusHours(1);
        LocalDateTime fin = fechaPrueba.plusHours(1);

        long cantidad = citaRepository.countByMedicoIdAndFechaHoraBetween(
                "1",
                inicio,
                fin);

        assertEquals(1, cantidad);
    }

    @Test
    void countBySalaIdAndFechaHoraBetween_DebeContarCorrectamente() {

        LocalDateTime inicio = fechaPrueba.minusHours(1);
        LocalDateTime fin = fechaPrueba.plusHours(1);

        long cantidad = citaRepository.countBySalaIdAndFechaHoraBetween(
                1L,
                inicio,
                fin);

        assertEquals(1, cantidad);
    }

    @Test
    void findCitasProximas_DebeRetornarResultados() {

        List<Cita> resultado = citaRepository.findCitasProximas(
                fechaPrueba.minusHours(1));

        assertFalse(resultado.isEmpty());
    }

    @Test
    void findCitasPasadas_DebeRetornarListaVacia() {

        List<Cita> resultado = citaRepository.findCitasPasadas(
                fechaPrueba.minusHours(1));

        assertTrue(resultado.isEmpty());
    }
}