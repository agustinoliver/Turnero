package ar.edu.utn.turnero.turnero_backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un predio deportivo con sus canchas y configuración de horarios.
 */
@Entity
@Table(name = "predios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Predio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del predio es obligatorio")
    @Column(nullable = false)
    private String nombre;

    @Column
    private String direccion;

    @Column
    private String telefono;

    @Column
    private String descripcion;

    @Column(nullable = false)
    @Builder.Default
    private boolean activo = true;

    @Column(updatable = false)
    private LocalDateTime creadoEn;

    // Relación: un predio pertenece a un dueño (one-to-one)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dueno_id", nullable = false, unique = true)
    private Usuario dueno;

    // Relación: un predio tiene múltiples canchas
    @OneToMany(mappedBy = "predio", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<Cancha> canchas = new ArrayList<>();

    // Relación: un predio tiene múltiples horarios disponibles configurados
    @OneToMany(mappedBy = "predio", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<HorarioDisponible> horariosDisponibles = new ArrayList<>();

    // Relación: un predio puede tener días cerrados
    @OneToMany(mappedBy = "predio", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<DiaCerrado> diasCerrados = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.creadoEn = LocalDateTime.now();
    }
}
