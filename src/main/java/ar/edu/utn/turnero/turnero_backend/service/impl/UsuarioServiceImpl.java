package ar.edu.utn.turnero.turnero_backend.service.impl;

import ar.edu.utn.turnero.turnero_backend.dto.request.ChangePasswordRequest;
import ar.edu.utn.turnero.turnero_backend.dto.request.CreateDuenoRequest;
import ar.edu.utn.turnero.turnero_backend.dto.request.UpdateUsuarioRequest;
import ar.edu.utn.turnero.turnero_backend.dto.response.UsuarioResponse;
import ar.edu.utn.turnero.turnero_backend.entity.Usuario;
import ar.edu.utn.turnero.turnero_backend.enums.RoleName;
import ar.edu.utn.turnero.turnero_backend.exception.BadRequestException;
import ar.edu.utn.turnero.turnero_backend.exception.ResourceConflictException;
import ar.edu.utn.turnero.turnero_backend.exception.ResourceNotFoundException;
import ar.edu.utn.turnero.turnero_backend.repository.UsuarioRepository;
import ar.edu.utn.turnero.turnero_backend.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UsuarioResponse crearDueno(CreateDuenoRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new ResourceConflictException("Ya existe un usuario con el email: " + request.getEmail());
        }

        Usuario dueno = Usuario.builder()
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .email(request.getEmail())
                .telefono(request.getTelefono())
                .password(passwordEncoder.encode(request.getPasswordInicial()))
                .rol(RoleName.ROLE_DUENO)
                .activo(true)
                .build();

        Usuario saved = usuarioRepository.save(dueno);
        log.info("Dueño creado por admin: {}", saved.getEmail());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public UsuarioResponse actualizarUsuario(Long id, UpdateUsuarioRequest request) {
        Usuario usuario = findById(id);

        // Verificar si el nuevo email ya lo usa otro usuario
        if (!usuario.getEmail().equals(request.getEmail()) &&
                usuarioRepository.existsByEmail(request.getEmail())) {
            throw new ResourceConflictException("El email ya está en uso: " + request.getEmail());
        }

        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setEmail(request.getEmail());
        usuario.setTelefono(request.getTelefono());

        return toResponse(usuarioRepository.save(usuario));
    }

    @Override
    @Transactional
    public void activarDesactivarUsuario(Long id, boolean activo) {
        Usuario usuario = findById(id);
        usuario.setActivo(activo);
        usuarioRepository.save(usuario);
        log.info("Usuario {} {}", id, activo ? "activado" : "desactivado");
    }

    @Override
    @Transactional
    public void eliminarUsuario(Long id) {
        Usuario usuario = findById(id);
        usuarioRepository.delete(usuario);
        log.info("Usuario {} eliminado", id);
    }

    @Override
    public UsuarioResponse obtenerUsuarioPorId(Long id) {
        return toResponse(findById(id));
    }

    @Override
    public UsuarioResponse obtenerUsuarioPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + email));
        return toResponse(usuario);
    }

    @Override
    public List<UsuarioResponse> listarTodos() {
        return usuarioRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<UsuarioResponse> listarDuenos() {
        return usuarioRepository.findByRol(RoleName.ROLE_DUENO).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<UsuarioResponse> listarClientes() {
        return usuarioRepository.findByRol(RoleName.ROLE_CLIENTE).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void cambiarPassword(String email, ChangePasswordRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + email));

        if (!passwordEncoder.matches(request.getPasswordActual(), usuario.getPassword())) {
            throw new BadRequestException("La contraseña actual es incorrecta");
        }
        if (!request.getNuevaPassword().equals(request.getConfirmNuevaPassword())) {
            throw new BadRequestException("Las contraseñas nuevas no coinciden");
        }
        if (passwordEncoder.matches(request.getNuevaPassword(), usuario.getPassword())) {
            throw new BadRequestException("La nueva contraseña debe ser diferente a la actual");
        }

        usuario.setPassword(passwordEncoder.encode(request.getNuevaPassword()));
        usuarioRepository.save(usuario);
        log.info("Contraseña cambiada para: {}", email);
    }

    private Usuario findById(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
    }

    public UsuarioResponse toResponse(Usuario u) {
        return UsuarioResponse.builder()
                .id(u.getId())
                .nombre(u.getNombre())
                .apellido(u.getApellido())
                .email(u.getEmail())
                .telefono(u.getTelefono())
                .rol(u.getRol())
                .activo(u.isActivo())
                .creadoEn(u.getCreadoEn())
                .build();
    }
}
