package com.microservicios.mscitas.repository;

import com.microservicios.mscitas.model.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    /**
     * Busca citas por ID de usuario
     */
    List<Cita> findByUserId(String userId);

    /**
     * Busca citas por ID de médico
     */
    List<Cita> findByMedicoId(String medicoId);

    /**
     * Busca citas por ID de sala
     */
    List<Cita> findBySalaId(Long salaId);

    /**
     * Busca citas por especialidad
     */
    List<Cita> findByEspecialidad(String especialidad);

    /**
     * Verifica si existe una cita para un médico en una fecha y hora específicas
     */
    boolean existsByMedicoIdAndFechaHora(String medicoId, LocalDateTime fechaHora);

    /**
     * Verifica si existe una cita para una sala en una fecha y hora específicas
     */
    boolean existsBySalaIdAndFechaHora(Long salaId, LocalDateTime fechaHora);

    /**
     * Busca citas en un rango de fechas para un médico específico
     */
    @Query("SELECT c FROM Cita c WHERE c.medicoId = :medicoId AND c.fechaHora BETWEEN :fechaInicio AND :fechaFin")
    List<Cita> findByMedicoIdAndFechaHoraBetween(
            @Param("medicoId") String medicoId,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );

    /**
     * Busca citas en un rango de fechas para una sala específica
     */
    @Query("SELECT c FROM Cita c WHERE c.salaId = :salaId AND c.fechaHora BETWEEN :fechaInicio AND :fechaFin")
    List<Cita> findBySalaIdAndFechaHoraBetween(
            @Param("salaId") Long salaId,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );

    /**
     * Busca citas para un usuario en una fecha específica
     */
    @Query("SELECT c FROM Cita c WHERE c.userId = :userId AND DATE(c.fechaHora) = DATE(:fecha)")
    List<Cita> findByUserIdAndFecha(
            @Param("userId") String userId,
            @Param("fecha") LocalDateTime fecha
    );

    /**
     * Busca citas próximas (a partir de ahora)
     */
    @Query("SELECT c FROM Cita c WHERE c.fechaHora >= :ahora ORDER BY c.fechaHora")
    List<Cita> findCitasProximas(@Param("ahora") LocalDateTime ahora);

    /**
     * Busca citas pasadas (anteriores a ahora)
     */
    @Query("SELECT c FROM Cita c WHERE c.fechaHora < :ahora ORDER BY c.fechaHora DESC")
    List<Cita> findCitasPasadas(@Param("ahora") LocalDateTime ahora);

    /**
     * Cuenta citas por médico en un rango de fechas
     */
    @Query("SELECT COUNT(c) FROM Cita c WHERE c.medicoId = :medicoId AND c.fechaHora BETWEEN :fechaInicio AND :fechaFin")
    long countByMedicoIdAndFechaHoraBetween(
            @Param("medicoId") String medicoId,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );

    /**
     * Cuenta citas por sala en un rango de fechas
     */
    @Query("SELECT COUNT(c) FROM Cita c WHERE c.salaId = :salaId AND c.fechaHora BETWEEN :fechaInicio AND :fechaFin")
    long countBySalaIdAndFechaHoraBetween(
            @Param("salaId") Long salaId,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );
}
