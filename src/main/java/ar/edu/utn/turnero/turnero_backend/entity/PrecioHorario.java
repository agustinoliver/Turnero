package ar.edu.utn.turnero.turnero_backend.entity;

import ar.edu.utn.turnero.turnero_backend.enums.DiaSemana;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * Define un precio específico para una franja horaria de una cancha.
 *
 * Ejemplo: Cancha 1 - SABADO de 20:00 a 22:00 → $9000/hora
 *
 * Si no existe un PrecioHorario que cubra un slot, se usa el precioPorHora
 * base definido en la entidad Cancha.
 */
@Entity
@Table(
        name = "precios_horarios",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"cancha_id", "dia_semana", "hora_inicio", "hora_fin"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrecioHorario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "dia_semana", nullable = false)
    private DiaSemana diaSemana;

    @NotNull
    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @NotNull
    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    /** Precio por hora para esta franja. Tiene prioridad sobre el precio base de la cancha. */
    @NotNull
    @Positive
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cancha_id", nullable = false)
    private Cancha cancha;
}
