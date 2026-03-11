package ar.edu.utn.turnero.turnero_backend.controller;

import ar.edu.utn.turnero.turnero_backend.dto.response.DisponibilidadResponse;
import ar.edu.utn.turnero.turnero_backend.dto.response.PredioResponse;
import ar.edu.utn.turnero.turnero_backend.enums.TipoCancha;
import ar.edu.utn.turnero.turnero_backend.service.PredioService;
import ar.edu.utn.turnero.turnero_backend.service.ReservaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Endpoints públicos para consultar disponibilidad de canchas.
 * No requiere autenticación, son accesibles para clientes buscando canchas.
 */
@RestController
@RequestMapping("/api/disponibilidad")
@RequiredArgsConstructor
@Tag(name = "Disponibilidad", description = "Consultas públicas de disponibilidad de canchas (sin auth requerida)")
public class DisponibilidadController {

    private final ReservaService reservaService;
    private final PredioService predioService;

    @GetMapping("/predio/{predioId}")
    @Operation(summary = "Buscar canchas disponibles en un predio para una fecha",
               description = "Filtra por tipo de cancha (opcional). Retorna las canchas disponibles con sus slots horarios.")
    public ResponseEntity<List<DisponibilidadResponse>> buscarDisponibilidad(
            @PathVariable Long predioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) TipoCancha tipo) {
        return ResponseEntity.ok(reservaService.buscarDisponibilidad(predioId, tipo, fecha));
    }

    @GetMapping("/cancha/{canchaId}")
    @Operation(summary = "Ver disponibilidad de una cancha específica para una fecha")
    public ResponseEntity<DisponibilidadResponse> disponibilidadCancha(
            @PathVariable Long canchaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(reservaService.obtenerDisponibilidad(canchaId, fecha));
    }

    @GetMapping("/predios")
    @Operation(summary = "Listar todos los predios activos")
    public ResponseEntity<List<PredioResponse>> listarPredios() {
        return ResponseEntity.ok(predioService.listarActivos());
    }
}
