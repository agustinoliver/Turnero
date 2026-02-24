package ar.edu.utn.turnero.turnero_backend.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DiaCerradoRequest {

    @NotNull(message = "La fecha es obligatoria")
    private LocalDate fecha;

    private String motivo;
}
