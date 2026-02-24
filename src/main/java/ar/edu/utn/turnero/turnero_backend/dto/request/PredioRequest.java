package ar.edu.utn.turnero.turnero_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PredioRequest {

    @NotBlank(message = "El nombre del predio es obligatorio")
    private String nombre;

    private String direccion;

    private String telefono;

    private String descripcion;
}
