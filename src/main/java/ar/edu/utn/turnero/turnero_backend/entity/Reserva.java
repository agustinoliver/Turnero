package ar.edu.utn.turnero.turnero_backend.entity;

import ar.edu.utn.turnero.turnero_backend.enums.DivisionType;
import ar.edu.utn.turnero.turnero_backend.enums.EstadoReserva;
import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Entidad que representa una reserva de cancha.
 */
@Entity
@Table(name = "reservas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La fecha es obligatoria")
    @Column(nullable = false)
    private LocalDate fecha;

    @NotNull(message = "La hora de inicio es obligatoria")
    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @NotNull(message = "La hora de fin es obligatoria")
    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    /**
     * Indica qué porción de la cancha fue reservada.
     * WHOLE = cancha completa, los demás valores indican una división específica.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "division_type", nullable = false)
    @Builder.Default
    private DivisionType divisionType = DivisionType.WHOLE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoReserva estado = EstadoReserva.ACTIVA;

    @Column
    private String observaciones;

    @Column(updatable = false)
    private LocalDateTime creadaEn;

    @Column
    private LocalDateTime canceladaEn;

    @Column
    private String motivoCancelacion;

    // Relación: una reserva pertenece a una cancha
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cancha_id", nullable = false)
    private Cancha cancha;

    // Relación: una reserva pertenece a un cliente
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente;

    // Relación: creada por (puede ser el dueño haciendo reserva manual, o el mismo cliente)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creada_por_id")
    private Usuario creadaPor;

    @PrePersist
    protected void onCreate() {
        this.creadaEn = LocalDateTime.now();
    }

    /**
     * Verifica si la hora de fin es posterior a la hora de inicio.
     */
    @AssertTrue(message = "La hora de fin debe ser posterior a la hora de inicio")
    public boolean isHoraFinValida() {
        if (horaInicio == null || horaFin == null) return true;
        return horaFin.isAfter(horaInicio);
    }
}
