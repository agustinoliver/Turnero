package ar.edu.utn.turnero.turnero_backend.dto.request;

import ar.edu.utn.turnero.turnero_backend.enums.DivisionType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO para que el dueño cree una reserva manual en nombre de un cliente.
 */
@Data
public class ReservaManualRequest {

    @NotNull(message = "El ID de la cancha es obligatorio")
    private Long canchaId;

    @NotNull(message = "El ID del cliente es obligatorio")
    private Long clienteId;

    @NotNull(message = "La fecha es obligatoria")
    @FutureOrPresent(message = "La fecha debe ser hoy o en el futuro")
    private LocalDate fecha;

    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime horaInicio;

    @NotNull(message = "La hora de fin es obligatoria")
    private LocalTime horaFin;

    private DivisionType divisionType = DivisionType.WHOLE;

    private String observaciones;
}
