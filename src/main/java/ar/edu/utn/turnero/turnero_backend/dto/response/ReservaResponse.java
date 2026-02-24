package ar.edu.utn.turnero.turnero_backend.dto.response;

import ar.edu.utn.turnero.turnero_backend.enums.DivisionType;
import ar.edu.utn.turnero.turnero_backend.enums.EstadoReserva;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReservaResponse {
    private Long id;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private DivisionType divisionType;
    private EstadoReserva estado;
    private String observaciones;
    private LocalDateTime creadaEn;
    private String motivoCancelacion;

    // Datos de la cancha
    private Long canchaId;
    private String canchaNombre;
    private BigDecimal precioPorHora;

    // Datos del predio
    private Long predioId;
    private String predioNombre;

    // Datos del cliente
    private Long clienteId;
    private String clienteNombre;
    private String clienteApellido;
    private String clienteEmail;
    private String clienteTelefono;
}
