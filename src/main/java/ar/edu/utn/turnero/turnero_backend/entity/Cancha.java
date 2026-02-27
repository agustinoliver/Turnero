package ar.edu.utn.turnero.turnero_backend.entity;

import ar.edu.utn.turnero.turnero_backend.enums.DivisionType;
import ar.edu.utn.turnero.turnero_backend.enums.TipoCancha;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Entidad que representa una cancha de fútbol dentro de un predio.
 *
 * El precio se puede definir de dos maneras (no excluyentes):
 *   - precioPorHora: precio base, aplica cuando no hay un PrecioHorario definido para el slot.
 *   - preciosHorarios: precios diferenciados por franja horaria y día. Tienen prioridad.
 */
@Entity
@Table(name = "canchas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cancha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de la cancha es obligatorio")
    @Column(nullable = false)
    private String nombre;

    @NotNull(message = "El tipo de cancha es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoCancha tipo;

    @Column
    private String descripcion;

    /**
     * Precio base por hora. Se usa cuando no existe un PrecioHorario
     * configurado para el slot horario solicitado.
     */
    @NotNull(message = "El precio por hora es obligatorio")
    @Positive(message = "El precio debe ser positivo")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precioPorHora;

    @Column(nullable = false)
    @Builder.Default
    private boolean activa = true;

    @Column(updatable = false)
    private LocalDateTime creadaEn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "predio_id", nullable = false)
    private Predio predio;

    @OneToMany(mappedBy = "cancha", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Reserva> reservas = new ArrayList<>();

    /**
     * Precios diferenciados por franja horaria.
     * Si está vacía, se usa siempre precioPorHora.
     */
    @OneToMany(mappedBy = "cancha", cascade = CascadeType.ALL,
            fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<PrecioHorario> preciosHorarios = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.creadaEn = LocalDateTime.now();
    }

    public boolean esDivisible() {
        return this.tipo == TipoCancha.NUEVE || this.tipo == TipoCancha.SIETE;
    }

    public List<DivisionType> getDivisionesDisponibles() {
        return switch (this.tipo) {
            case NUEVE -> Arrays.asList(
                    DivisionType.WHOLE,
                    DivisionType.SIETE_CINCO_A,
                    DivisionType.SIETE_CINCO_B,
                    DivisionType.TRES_CINCO_A,
                    DivisionType.TRES_CINCO_B,
                    DivisionType.TRES_CINCO_C
            );
            case SIETE -> Arrays.asList(
                    DivisionType.WHOLE,
                    DivisionType.DOS_CINCO_A,
                    DivisionType.DOS_CINCO_B
            );
            case CINCO -> Collections.singletonList(DivisionType.WHOLE);
        };
    }
}
