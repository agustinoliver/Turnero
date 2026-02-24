package ar.edu.utn.turnero.turnero_backend.dto.response;

import ar.edu.utn.turnero.turnero_backend.enums.DivisionType;
import ar.edu.utn.turnero.turnero_backend.enums.TipoCancha;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CanchaResponse {
    private Long id;
    private String nombre;
    private TipoCancha tipo;
    private String descripcion;
    private BigDecimal precioPorHora;
    private boolean activa;
    private boolean divisible;
    private List<DivisionType> divisionesDisponibles;
    private Long predioId;
    private String predioNombre;
    private LocalDateTime creadaEn;
}
