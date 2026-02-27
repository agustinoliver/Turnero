package ar.edu.utn.turnero.turnero_backend.dto.request;

import ar.edu.utn.turnero.turnero_backend.enums.DiaSemana;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * DTO para configurar un precio específico en una franja horaria de una cancha.
 *
 * Si no se define precio para un slot, se usa el precioPorHora base de la cancha.
 */
@Data
public class PrecioHorarioRequest {

    @NotNull(message = "El día de la semana es obligatorio")
    private DiaSemana diaSemana;

    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime horaInicio;

    @NotNull(message = "La hora de fin es obligatoria")
    private LocalTime horaFin;

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser positivo")
    private BigDecimal precio;
}
