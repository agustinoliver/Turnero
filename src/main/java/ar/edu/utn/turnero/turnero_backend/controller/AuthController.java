package ar.edu.utn.turnero.turnero_backend.controller;

import ar.edu.utn.turnero.turnero_backend.dto.request.*;
import ar.edu.utn.turnero.turnero_backend.dto.response.AuthResponse;
import ar.edu.utn.turnero.turnero_backend.dto.response.MessageResponse;
import ar.edu.utn.turnero.turnero_backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Registro, login y recuperación de contraseña")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Registrar nuevo cliente")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegistroClienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrarCliente(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Solicitar código de recuperación de contraseña")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(MessageResponse.of("Se envió un código de recuperación al email indicado"));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Restablecer contraseña usando el código recibido")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(MessageResponse.of("Contraseña restablecida exitosamente"));
    }
}