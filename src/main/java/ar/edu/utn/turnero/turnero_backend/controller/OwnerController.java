package ar.edu.utn.turnero.turnero_backend.controller;

import ar.edu.utn.turnero.turnero_backend.dto.request.*;
import ar.edu.utn.turnero.turnero_backend.dto.response.*;
import ar.edu.utn.turnero.turnero_backend.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/owner")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DUENO')")
@Tag(name = "Dueño", description = "Gestión del predio, canchas, horarios, precios y reservas")
@SecurityRequirement(name = "Bearer Authentication")
public class OwnerController {

    private final PredioService predioService;
    private final CanchaService canchaService;
    private final HorarioService horarioService;
    private final ReservaService reservaService;
    private final UsuarioService usuarioService;

    // ─── Mi Predio ────────────────────────────────────────────────────────────

    @PostMapping("/predio")
    @Operation(summary = "Crear mi predio")
    public ResponseEntity<PredioResponse> crearPredio(
            @Valid @RequestBody PredioRequest request,
            @AuthenticationPrincipal String email) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(predioService.crearPredio(request, email));
    }

    @GetMapping("/predio")
    @Operation(summary = "Ver mi predio")
    public ResponseEntity<PredioResponse> miPredio(@AuthenticationPrincipal String email) {
        return ResponseEntity.ok(predioService.obtenerPorDueno(email));
    }

    @PutMapping("/predio")
    @Operation(summary = "Actualizar mi predio")
    public ResponseEntity<PredioResponse> actualizarPredio(
            @Valid @RequestBody PredioRequest request,
            @AuthenticationPrincipal String email) {
        PredioResponse predio = predioService.obtenerPorDueno(email);
        return ResponseEntity.ok(predioService.actualizarPredio(predio.getId(), request, email));
    }

    // ─── Canchas ──────────────────────────────────────────────────────────────

    @PostMapping("/canchas")
    @Operation(summary = "Crear una cancha en mi predio")
    public ResponseEntity<CanchaResponse> crearCancha(
            @Valid @RequestBody CanchaRequest request,
            @AuthenticationPrincipal String email) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(canchaService.crearCancha(request, email));
    }

    @GetMapping("/canchas")
    @Operation(summary = "Listar canchas de mi predio")
    public ResponseEntity<List<CanchaResponse>> listarCanchas(
            @AuthenticationPrincipal String email) {
        PredioResponse predio = predioService.obtenerPorDueno(email);
        return ResponseEntity.ok(canchaService.listarActivasPorPredio(predio.getId()));
    }

    @GetMapping("/canchas/{id}")
    @Operation(summary = "Ver detalle de una cancha")
    public ResponseEntity<CanchaResponse> obtenerCancha(@PathVariable Long id) {
        return ResponseEntity.ok(canchaService.obtenerPorId(id));
    }

    @PutMapping("/canchas/{id}")
    @Operation(summary = "Actualizar datos base de una cancha")
    public ResponseEntity<CanchaResponse> actualizarCancha(
            @PathVariable Long id,
            @Valid @RequestBody CanchaRequest request,
            @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(canchaService.actualizarCancha(id, request, email));
    }

    @DeleteMapping("/canchas/{id}")
    @Operation(summary = "Eliminar (desactivar) una cancha")
    public ResponseEntity<MessageResponse> eliminarCancha(
            @PathVariable Long id,
            @AuthenticationPrincipal String email) {
        canchaService.eliminarCancha(id, email);
        return ResponseEntity.ok(MessageResponse.of("Cancha eliminada exitosamente"));
    }

    // ─── Precios horarios ─────────────────────────────────────────────────────
    //
    //  Dos modalidades de precio (no excluyentes):
    //
    //  A) Precio base → precioPorHora en CanchaRequest. Aplica a todos los
    //     slots que NO tengan un PrecioHorario definido.
    //
    //  B) Precios horarios → franjas con precio propio por día y hora.
    //     Tienen prioridad sobre el precio base.
    //
    //  Ejemplo sábado escalonado:
    //  PUT /api/owner/canchas/{id}/precios-horarios/configurar
    //  [
    //    { "diaSemana": "SABADO", "horaInicio": "09:00", "horaFin": "17:00", "precio": 5000 },
    //    { "diaSemana": "SABADO", "horaInicio": "17:00", "horaFin": "19:00", "precio": 7000 },
    //    { "diaSemana": "SABADO", "horaInicio": "19:00", "horaFin": "23:00", "precio": 10000 }
    //  ]
    // ─────────────────────────────────────────────────────────────────────────

    @PutMapping("/canchas/{id}/precios-horarios/configurar")
    @Operation(
            summary = "Reemplazar todos los precios horarios de una cancha",
            description = "Reemplaza la configuración completa. Enviá [] para dejar solo precio base."
    )
    public ResponseEntity<List<PrecioHorarioResponse>> configurarPreciosHorarios(
            @PathVariable Long id,
            @Valid @RequestBody List<@Valid PrecioHorarioRequest> precios,
            @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(canchaService.configurarPreciosHorarios(id, precios, email));
    }

    @PostMapping("/canchas/{id}/precios-horarios")
    @Operation(summary = "Agregar un precio horario a una cancha")
    public ResponseEntity<PrecioHorarioResponse> agregarPrecioHorario(
            @PathVariable Long id,
            @Valid @RequestBody PrecioHorarioRequest request,
            @AuthenticationPrincipal String email) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(canchaService.agregarPrecioHorario(id, request, email));
    }

    @GetMapping("/canchas/{id}/precios-horarios")
    @Operation(summary = "Ver precios horarios de una cancha")
    public ResponseEntity<List<PrecioHorarioResponse>> listarPreciosHorarios(
            @PathVariable Long id,
            @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(canchaService.listarPreciosHorarios(id, email));
    }

    @DeleteMapping("/precios-horarios/{precioHorarioId}")
    @Operation(summary = "Eliminar un precio horario puntual")
    public ResponseEntity<MessageResponse> eliminarPrecioHorario(
            @PathVariable Long precioHorarioId,
            @AuthenticationPrincipal String email) {
        canchaService.eliminarPrecioHorario(precioHorarioId, email);
        return ResponseEntity.ok(MessageResponse.of("Precio horario eliminado exitosamente"));
    }

    // ─── Horarios y Días Cerrados ─────────────────────────────────────────────

    @PostMapping("/horarios/configurar")
    @Operation(summary = "Reemplazar todos los horarios del predio")
    public ResponseEntity<List<HorarioDisponibleResponse>> configurarHorarios(
            @Valid @RequestBody List<@Valid HorarioDisponibleRequest> request,
            @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(horarioService.configurarHorarios(request, email));
    }

    @PostMapping("/horarios")
    @Operation(summary = "Agregar un nuevo horario disponible")
    public ResponseEntity<HorarioDisponibleResponse> agregarHorario(
            @Valid @RequestBody HorarioDisponibleRequest request,
            @AuthenticationPrincipal String email) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(horarioService.agregarHorario(request, email));
    }

    @GetMapping("/horarios")
    @Operation(summary = "Ver horarios disponibles del predio")
    public ResponseEntity<List<HorarioDisponibleResponse>> listarHorarios(
            @AuthenticationPrincipal String email) {
        PredioResponse predio = predioService.obtenerPorDueno(email);
        return ResponseEntity.ok(horarioService.listarHorariosPorPredio(predio.getId()));
    }

    @DeleteMapping("/horarios/{id}")
    @Operation(summary = "Eliminar un horario disponible")
    public ResponseEntity<MessageResponse> eliminarHorario(
            @PathVariable Long id,
            @AuthenticationPrincipal String email) {
        horarioService.eliminarHorario(id, email);
        return ResponseEntity.ok(MessageResponse.of("Horario eliminado exitosamente"));
    }

    @PostMapping("/dias-cerrados")
    @Operation(summary = "Marcar un día como cerrado")
    public ResponseEntity<DiaCerradoResponse> agregarDiaCerrado(
            @Valid @RequestBody DiaCerradoRequest request,
            @AuthenticationPrincipal String email) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(horarioService.agregarDiaCerrado(request, email));
    }

    @GetMapping("/dias-cerrados")
    @Operation(summary = "Ver días cerrados del predio")
    public ResponseEntity<List<DiaCerradoResponse>> listarDiasCerrados(
            @AuthenticationPrincipal String email) {
        PredioResponse predio = predioService.obtenerPorDueno(email);
        return ResponseEntity.ok(horarioService.listarDiasCerradosPorPredio(predio.getId()));
    }

    @DeleteMapping("/dias-cerrados/{id}")
    @Operation(summary = "Quitar un día cerrado")
    public ResponseEntity<MessageResponse> eliminarDiaCerrado(
            @PathVariable Long id,
            @AuthenticationPrincipal String email) {
        horarioService.eliminarDiaCerrado(id, email);
        return ResponseEntity.ok(MessageResponse.of("Día cerrado eliminado exitosamente"));
    }

    // ─── Reservas ─────────────────────────────────────────────────────────────

    @PostMapping("/reservas")
    @Operation(summary = "Crear reserva manual en nombre de un cliente")
    public ResponseEntity<ReservaResponse> crearReservaManual(
            @Valid @RequestBody ReservaManualRequest request,
            @AuthenticationPrincipal String email) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservaService.crearReservaManual(request, email));
    }

    @GetMapping("/reservas")
    @Operation(summary = "Listar todas las reservas de mi predio")
    public ResponseEntity<List<ReservaResponse>> listarTodasReservas(
            @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(reservaService.listarTodasReservasDePredio(email));
    }

    @GetMapping("/reservas/hoy")
    @Operation(summary = "Ver reservas de hoy")
    public ResponseEntity<List<ReservaResponse>> reservasHoy(
            @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(reservaService.listarReservasPorPredioYFecha(email, LocalDate.now()));
    }

    @GetMapping("/reservas/fecha")
    @Operation(summary = "Ver reservas de una fecha específica")
    public ResponseEntity<List<ReservaResponse>> reservasPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(reservaService.listarReservasPorPredioYFecha(email, fecha));
    }

    @GetMapping("/reservas/tablero")
    @Operation(summary = "Ver tablero de reservas para una fecha (vista grilla)")
    public ResponseEntity<ReservaTableroResponse> tablero(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(reservaService.obtenerTablero(email, fecha));
    }

    @GetMapping("/reservas/{id}")
    @Operation(summary = "Ver detalle de una reserva")
    public ResponseEntity<ReservaResponse> obtenerReserva(@PathVariable Long id) {
        return ResponseEntity.ok(reservaService.obtenerPorId(id));
    }

    @DeleteMapping("/reservas/{id}")
    @Operation(summary = "Cancelar una reserva")
    public ResponseEntity<MessageResponse> cancelarReserva(
            @PathVariable Long id,
            @RequestParam(required = false) String motivo,
            @AuthenticationPrincipal String email) {
        CancelarReservaRequest request = new CancelarReservaRequest();
        request.setMotivo(motivo);
        reservaService.cancelarReserva(id, email, request);
        return ResponseEntity.ok(MessageResponse.of("Reserva cancelada exitosamente"));
    }

    // ─── Perfil ───────────────────────────────────────────────────────────────

    @GetMapping("/perfil")
    @Operation(summary = "Ver mi perfil")
    public ResponseEntity<UsuarioResponse> miPerfil(@AuthenticationPrincipal String email) {
        return ResponseEntity.ok(usuarioService.obtenerUsuarioPorEmail(email));
    }

    @PutMapping("/perfil/password")
    @Operation(summary = "Cambiar mi contraseña")
    public ResponseEntity<MessageResponse> cambiarPassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal String email) {
        usuarioService.cambiarPassword(email, request);
        return ResponseEntity.ok(MessageResponse.of("Contraseña cambiada exitosamente"));
    }

    @GetMapping("/clientes")
    @Operation(summary = "Listar clientes registrados (para reservas manuales)")
    public ResponseEntity<List<UsuarioResponse>> listarClientes() {
        return ResponseEntity.ok(usuarioService.listarClientes());
    }
}