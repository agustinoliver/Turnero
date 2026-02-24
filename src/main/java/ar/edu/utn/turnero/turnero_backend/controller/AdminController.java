package ar.edu.utn.turnero.turnero_backend.controller;

import ar.edu.utn.turnero.turnero_backend.dto.request.CreateDuenoRequest;
import ar.edu.utn.turnero.turnero_backend.dto.request.UpdateUsuarioRequest;
import ar.edu.utn.turnero.turnero_backend.dto.response.MessageResponse;
import ar.edu.utn.turnero.turnero_backend.dto.response.PredioResponse;
import ar.edu.utn.turnero.turnero_backend.dto.response.ReservaResponse;
import ar.edu.utn.turnero.turnero_backend.dto.response.UsuarioResponse;
import ar.edu.utn.turnero.turnero_backend.service.PredioService;
import ar.edu.utn.turnero.turnero_backend.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para operaciones exclusivas del ADMIN.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Operaciones exclusivas del administrador")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminController {

    private final UsuarioService usuarioService;
    private final PredioService predioService;

    // ─── Gestión de usuarios ────────────────────────────────────────────────────

    @PostMapping("/duenos")
    @Operation(summary = "Crear cuenta de Dueño de Predio")
    public ResponseEntity<UsuarioResponse> crearDueno(@Valid @RequestBody CreateDuenoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crearDueno(request));
    }

    @GetMapping("/usuarios")
    @Operation(summary = "Listar todos los usuarios")
    public ResponseEntity<List<UsuarioResponse>> listarUsuarios() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    @GetMapping("/duenos")
    @Operation(summary = "Listar todos los dueños de predios")
    public ResponseEntity<List<UsuarioResponse>> listarDuenos() {
        return ResponseEntity.ok(usuarioService.listarDuenos());
    }

    @GetMapping("/clientes")
    @Operation(summary = "Listar todos los clientes")
    public ResponseEntity<List<UsuarioResponse>> listarClientes() {
        return ResponseEntity.ok(usuarioService.listarClientes());
    }

    @GetMapping("/usuarios/{id}")
    @Operation(summary = "Obtener usuario por ID")
    public ResponseEntity<UsuarioResponse> obtenerUsuario(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerUsuarioPorId(id));
    }

    @PutMapping("/usuarios/{id}")
    @Operation(summary = "Editar datos de un usuario")
    public ResponseEntity<UsuarioResponse> actualizarUsuario(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUsuarioRequest request) {
        return ResponseEntity.ok(usuarioService.actualizarUsuario(id, request));
    }

    @PutMapping("/usuarios/{id}/activar")
    @Operation(summary = "Activar cuenta de usuario")
    public ResponseEntity<MessageResponse> activarUsuario(@PathVariable Long id) {
        usuarioService.activarDesactivarUsuario(id, true);
        return ResponseEntity.ok(MessageResponse.of("Usuario activado exitosamente"));
    }

    @PutMapping("/usuarios/{id}/desactivar")
    @Operation(summary = "Desactivar cuenta de usuario")
    public ResponseEntity<MessageResponse> desactivarUsuario(@PathVariable Long id) {
        usuarioService.activarDesactivarUsuario(id, false);
        return ResponseEntity.ok(MessageResponse.of("Usuario desactivado exitosamente"));
    }

    @DeleteMapping("/usuarios/{id}")
    @Operation(summary = "Eliminar usuario permanentemente")
    public ResponseEntity<MessageResponse> eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.ok(MessageResponse.of("Usuario eliminado exitosamente"));
    }

    // ─── Vista general del sistema ──────────────────────────────────────────────

    @GetMapping("/predios")
    @Operation(summary = "Ver todos los predios registrados")
    public ResponseEntity<List<PredioResponse>> listarPredios() {
        return ResponseEntity.ok(predioService.listarTodos());
    }

    @GetMapping("/predios/{id}")
    @Operation(summary = "Ver detalle de un predio")
    public ResponseEntity<PredioResponse> obtenerPredio(@PathVariable Long id) {
        return ResponseEntity.ok(predioService.obtenerPorId(id));
    }
}
