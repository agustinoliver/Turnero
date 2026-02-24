package ar.edu.utn.turnero.turnero_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * DTO para la vista tipo tablero de reservas de una fecha específica.
 * Agrupa las reservas por cancha con los slots horarios disponibles y ocupados.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReservaTableroResponse {

    private LocalDate fecha;
    private List<CanchaTablero> canchas;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class CanchaTablero {
        private Long canchaId;
        private String canchaNombre;
        private String tipo;
        private BigDecimal precioPorHora;
        private List<SlotHorario> slots;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class SlotHorario {
        private LocalTime horaInicio;
        private LocalTime horaFin;
        private boolean disponible;
        private ReservaResponse reserva; // null si disponible
    }
}
