package ar.edu.utn.turnero.turnero_backend.service;

import ar.edu.utn.turnero.turnero_backend.dto.request.ChangePasswordRequest;
import ar.edu.utn.turnero.turnero_backend.dto.request.CreateDuenoRequest;
import ar.edu.utn.turnero.turnero_backend.dto.request.UpdateUsuarioRequest;
import ar.edu.utn.turnero.turnero_backend.dto.response.UsuarioResponse;

import java.util.List;

public interface UsuarioService {

    UsuarioResponse crearDueno(CreateDuenoRequest request);

    UsuarioResponse actualizarUsuario(Long id, UpdateUsuarioRequest request);

    void activarDesactivarUsuario(Long id, boolean activo);

    void eliminarUsuario(Long id);

    UsuarioResponse obtenerUsuarioPorId(Long id);

    UsuarioResponse obtenerUsuarioPorEmail(String email);

    List<UsuarioResponse> listarTodos();

    List<UsuarioResponse> listarDuenos();

    List<UsuarioResponse> listarClientes();

    void cambiarPassword(String email, ChangePasswordRequest request);
}
