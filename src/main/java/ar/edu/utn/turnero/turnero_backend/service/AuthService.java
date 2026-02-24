package ar.edu.utn.turnero.turnero_backend.service;

import ar.edu.utn.turnero.turnero_backend.dto.request.*;
import ar.edu.utn.turnero.turnero_backend.dto.response.AuthResponse;
import ar.edu.utn.turnero.turnero_backend.dto.response.MessageResponse;

public interface AuthService {

    AuthResponse registrarCliente(RegistroClienteRequest request);

    AuthResponse login(LoginRequest request);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}
