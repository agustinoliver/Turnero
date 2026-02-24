package ar.edu.utn.turnero.turnero_backend.entity;

import ar.edu.utn.turnero.turnero_backend.enums.RoleName;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa a un usuario del sistema (Admin, Dueño o Cliente).
 */
@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Column(nullable = false)
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Column(nullable = false)
    private String apellido;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Debe ser un email válido")
    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String telefono;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleName rol;

    @Column(nullable = false)
    @Builder.Default
    private boolean activo = true;

    @Column(updatable = false)
    private LocalDateTime creadoEn;

    // Relación: un dueño tiene un predio (puede ser null para clientes/admins)
    @OneToOne(mappedBy = "dueno", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Predio predio;

    // Relación: un cliente puede tener muchas reservas
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Reserva> reservas = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.creadoEn = LocalDateTime.now();
    }
}
