package ar.edu.utn.turnero.turnero_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DiaCerradoResponse {
    private Long id;
    private LocalDate fecha;
    private String motivo;
    private Long predioId;
}
