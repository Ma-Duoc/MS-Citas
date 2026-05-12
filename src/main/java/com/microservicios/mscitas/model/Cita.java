package com.microservicios.mscitas.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Future;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Entity
@Table(name = "citas", 
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"medico_id", "fecha_hora"}, 
                           name = "uk_medico_fecha"),
           @UniqueConstraint(columnNames = {"sala_id", "fecha_hora"}, 
                           name = "uk_sala_fecha")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El ID del usuario es obligatorio")
    @Column(name = "user_id", nullable = false)
    private String userId;

    @NotBlank(message = "El nombre del usuario es obligatorio")
    @Column(name = "user_nombre", nullable = false)
    private String userNombre;

    @NotBlank(message = "El ID del médico es obligatorio")
    @Column(name = "medico_id", nullable = false)
    private String medicoId;

    @NotBlank(message = "El nombre del médico es obligatorio")
    @Column(name = "medico_nombre", nullable = false)
    private String medicoNombre;

    @NotBlank(message = "La especialidad es obligatoria")
    @Column(name = "especialidad", nullable = false)
    private String especialidad;

    @NotNull(message = "El ID de la sala es obligatorio")
    @Column(name = "sala_id", nullable = false)
    private Long salaId;

    @NotBlank(message = "El nombre de la sala es obligatorio")
    @Column(name = "sala_nombre", nullable = false)
    private String salaNombre;

    @NotNull(message = "La fecha y hora son obligatorias")
    @Future(message = "La fecha y hora deben ser futuras")
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;
}
