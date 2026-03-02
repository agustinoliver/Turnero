package ar.edu.utn.turnero.turnero_backend.controller;

import ar.edu.utn.turnero.turnero_backend.dto.request.*;
import ar.edu.utn.turnero.turnero_backend.dto.response.*;
import ar.edu.utn.turnero.turnero_backend.service.ReservaService;
import ar.edu.utn.turnero.turnero_backend.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/client")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CLIENTE')")
@Tag(name = "Cliente", description = "Reservas y perfil del cliente")
@SecurityRequirement(name = "Bearer Authentication")
public class ClientController {

    private final ReservaService reservaService;
    private final UsuarioService usuarioService;

    // ─── Reservas ─────────────────────────────────────────────────────────────────

    @PostMapping("/reservas")
    @Operation(summary = "Reservar una cancha")
    public ResponseEntity<ReservaResponse> crearReserva(
            @Valid @RequestBody ReservaRequest request,
            @AuthenticationPrincipal String email) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservaService.crearReserva(request, email));
    }

    @GetMapping("/reservas")
    @Operation(summary = "Ver mis reservas")
    public ResponseEntity<List<ReservaResponse>> misReservas(
            @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(reservaService.listarReservasDeCliente(email));
    }

    @GetMapping("/reservas/{id}")
    @Operation(summary = "Ver detalle de una reserva")
    public ResponseEntity<ReservaResponse> obtenerReserva(@PathVariable Long id) {
        return ResponseEntity.ok(reservaService.obtenerPorId(id));
    }

    @DeleteMapping("/reservas/{id}")
    @Operation(summary = "Cancelar una de mis reservas")
    public ResponseEntity<MessageResponse> cancelarReserva(
            @PathVariable Long id,
            @RequestBody(required = false) CancelarReservaRequest request,
            @AuthenticationPrincipal String email) {
        reservaService.cancelarReserva(id, email, request);
        return ResponseEntity.ok(MessageResponse.of("Reserva cancelada exitosamente"));
    }

    // ─── Perfil ───────────────────────────────────────────────────────────────────

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
}