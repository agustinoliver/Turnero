package ar.edu.utn.turnero.turnero_backend.dto.request;

import ar.edu.utn.turnero.turnero_backend.enums.TipoCancha;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CanchaRequest {

    @NotBlank(message = "El nombre de la cancha es obligatorio")
    private String nombre;

    @NotNull(message = "El tipo de cancha es obligatorio")
    private TipoCancha tipo;

    private String descripcion;

    @NotNull(message = "El precio por hora es obligatorio")
    @Positive(message = "El precio debe ser positivo")
    private BigDecimal precioPorHora;
}
