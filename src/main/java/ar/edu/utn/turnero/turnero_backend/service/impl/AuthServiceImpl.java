package ar.edu.utn.turnero.turnero_backend.service.impl;

import ar.edu.utn.turnero.turnero_backend.dto.request.*;
import ar.edu.utn.turnero.turnero_backend.dto.response.AuthResponse;
import ar.edu.utn.turnero.turnero_backend.entity.PasswordResetToken;
import ar.edu.utn.turnero.turnero_backend.entity.Usuario;
import ar.edu.utn.turnero.turnero_backend.enums.RoleName;
import ar.edu.utn.turnero.turnero_backend.exception.BadRequestException;
import ar.edu.utn.turnero.turnero_backend.exception.ResourceConflictException;
import ar.edu.utn.turnero.turnero_backend.exception.ResourceNotFoundException;
import ar.edu.utn.turnero.turnero_backend.repository.PasswordResetTokenRepository;
import ar.edu.utn.turnero.turnero_backend.repository.UsuarioRepository;
import ar.edu.utn.turnero.turnero_backend.security.JwtService;
import ar.edu.utn.turnero.turnero_backend.service.AuthService;
import ar.edu.utn.turnero.turnero_backend.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public AuthResponse registrarCliente(RegistroClienteRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new ResourceConflictException("Ya existe un usuario con el email: " + request.getEmail());
        }
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Las contraseñas no coinciden");
        }

        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .email(request.getEmail())
                .telefono(request.getTelefono())
                .password(passwordEncoder.encode(request.getPassword()))
                .rol(RoleName.ROLE_CLIENTE)
                .activo(true)
                .build();

        Usuario saved = usuarioRepository.save(usuario);
        log.info("Cliente registrado: {}", saved.getEmail());

        String token = jwtService.generateToken(saved.getEmail(), saved.getRol().name().replace("ROLE_", ""));
        return buildAuthResponse(token, saved);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Credenciales inválidas"));

        if (!usuario.isActivo()) {
            throw new BadRequestException("La cuenta está desactivada. Contactá con el administrador.");
        }

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new BadRequestException("Credenciales inválidas");
        }

        String token = jwtService.generateToken(usuario.getEmail(), usuario.getRol().name().replace("ROLE_", ""));
        log.info("Login exitoso: {}", usuario.getEmail());
        return buildAuthResponse(token, usuario);
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("No existe un usuario con el email: " + request.getEmail()));

        if (!usuario.isActivo()) {
            throw new BadRequestException("La cuenta está desactivada");
        }

        // Eliminar tokens anteriores del email
        tokenRepository.deleteByEmail(request.getEmail());

        // Generar token de 6 dígitos
        String tokenStr = String.valueOf(100000 + secureRandom.nextInt(900000));

        PasswordResetToken token = PasswordResetToken.builder()
                .token(tokenStr)
                .email(request.getEmail())
                .expiracion(LocalDateTime.now().plusMinutes(15))
                .usado(false)
                .build();

        tokenRepository.save(token);

        emailService.enviarEmailRecuperacion(request.getEmail(), tokenStr);
        log.info("Token de recuperación enviado a: {}", request.getEmail());
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getNuevaPassword().equals(request.getConfirmNuevaPassword())) {
            throw new BadRequestException("Las contraseñas no coinciden");
        }

        PasswordResetToken token = tokenRepository.findByTokenAndUsadoFalse(request.getToken())
                .orElseThrow(() -> new BadRequestException("El código es inválido o ya fue utilizado"));

        if (token.getExpiracion().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("El código ha expirado. Solicitá uno nuevo.");
        }

        Usuario usuario = usuarioRepository.findByEmail(token.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (passwordEncoder.matches(request.getNuevaPassword(), usuario.getPassword())) {
            throw new BadRequestException("La nueva contraseña no puede ser igual a la actual");
        }

        usuario.setPassword(passwordEncoder.encode(request.getNuevaPassword()));
        usuarioRepository.save(usuario);

        token.setUsado(true);
        tokenRepository.save(token);

        log.info("Contraseña restablecida para: {}", usuario.getEmail());
    }

    private AuthResponse buildAuthResponse(String token, Usuario usuario) {
        return AuthResponse.builder()
                .token(token)
                .tipo("Bearer")
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .email(usuario.getEmail())
                .rol(usuario.getRol())
                .build();
    }
}
