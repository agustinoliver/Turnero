package ar.edu.utn.turnero.turnero_backend.service;

import ar.edu.utn.turnero.turnero_backend.dto.request.CancelarReservaRequest;
import ar.edu.utn.turnero.turnero_backend.dto.request.ReservaManualRequest;
import ar.edu.utn.turnero.turnero_backend.dto.request.ReservaRequest;
import ar.edu.utn.turnero.turnero_backend.dto.response.DisponibilidadResponse;
import ar.edu.utn.turnero.turnero_backend.dto.response.ReservaResponse;
import ar.edu.utn.turnero.turnero_backend.dto.response.ReservaTableroResponse;
import ar.edu.utn.turnero.turnero_backend.enums.TipoCancha;

import java.time.LocalDate;
import java.util.List;

public interface ReservaService {

    /**
     * Crea una reserva por parte del cliente.
     */
    ReservaResponse crearReserva(ReservaRequest request, String emailCliente);

    /**
     * Crea una reserva manual por parte del dueño del predio.
     */
    ReservaResponse crearReservaManual(ReservaManualRequest request, String emailDueno);

    /**
     * Cancela una reserva. El cliente solo puede cancelar las suyas; el dueño puede cancelar cualquiera de su predio.
     */
    void cancelarReserva(Long reservaId, String emailUsuario, CancelarReservaRequest request);

    /**
     * Obtiene el detalle de una reserva por ID.
     */
    ReservaResponse obtenerPorId(Long id);

    /**
     * Lista las reservas de un cliente.
     */
    List<ReservaResponse> listarReservasDeCliente(String emailCliente);

    /**
     * Lista todas las reservas activas del predio del dueño para una fecha determinada.
     */
    List<ReservaResponse> listarReservasPorPredioYFecha(String emailDueno, LocalDate fecha);

    /**
     * Lista todas las reservas del predio (sin filtro de fecha) con soporte de filtros opcionales.
     */
    List<ReservaResponse> listarTodasReservasDePredio(String emailDueno);

    /**
     * Genera la vista tipo tablero de reservas para una fecha.
     */
    ReservaTableroResponse obtenerTablero(String emailDueno, LocalDate fecha);

    /**
     * Obtiene los slots disponibles de una cancha para una fecha.
     */
    DisponibilidadResponse obtenerDisponibilidad(Long canchaId, LocalDate fecha);

    /**
     * Lista las canchas disponibles para un tipo y fecha determinados.
     */
    List<DisponibilidadResponse> buscarDisponibilidad(Long predioId, TipoCancha tipo, LocalDate fecha);
}
