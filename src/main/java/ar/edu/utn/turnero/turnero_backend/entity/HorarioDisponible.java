package ar.edu.utn.turnero.turnero_backend.entity;

import ar.edu.utn.turnero.turnero_backend.enums.DiaSemana;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalTime;

/**
 * Entidad que define la franja horaria disponible para un día de la semana en un predio.
 * Un predio puede tener múltiples franjas horarias por día (ej: 09:00-13:00 y 16:00-23:00).
 */
@Entity
@Table(name = "horarios_disponibles",
        uniqueConstraints = @UniqueConstraint(columnNames = {"predio_id", "dia_semana", "hora_inicio", "hora_fin"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HorarioDisponible {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El día de la semana es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "dia_semana", nullable = false)
    private DiaSemana diaSemana;

    @NotNull(message = "La hora de inicio es obligatoria")
    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @NotNull(message = "La hora de fin es obligatoria")
    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    // Relación: el horario pertenece a un predio
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "predio_id", nullable = false)
    private Predio predio;
}
