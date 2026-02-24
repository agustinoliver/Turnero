package ar.edu.utn.turnero.turnero_backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

/**
 * Entidad que representa un día específico en que el predio está cerrado
 * (feriados, mantenimiento, etc.).
 */
@Entity
@Table(name = "dias_cerrados",
        uniqueConstraints = @UniqueConstraint(columnNames = {"predio_id", "fecha"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiaCerrado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La fecha es obligatoria")
    @Column(nullable = false)
    private LocalDate fecha;

    @Column
    private String motivo;

    // Relación: un día cerrado pertenece a un predio
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "predio_id", nullable = false)
    private Predio predio;
}
