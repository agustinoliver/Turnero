package ar.edu.utn.turnero.turnero_backend.dto.response;

import ar.edu.utn.turnero.turnero_backend.enums.DivisionType;
import ar.edu.utn.turnero.turnero_backend.enums.TipoCancha;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * DTO con los horarios disponibles de una cancha para una fecha determinada.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DisponibilidadResponse {

    private Long canchaId;
    private String canchaNombre;
    private TipoCancha tipo;
    private BigDecimal precioPorHora;
    private LocalDate fecha;
    private List<SlotDisponible> slotsDisponibles;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class SlotDisponible {
        private LocalTime horaInicio;
        private LocalTime horaFin;
        private List<DivisionType> divisionesDisponibles;
    }
}
