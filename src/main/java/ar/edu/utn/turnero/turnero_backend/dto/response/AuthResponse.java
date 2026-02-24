package ar.edu.utn.turnero.turnero_backend.dto.response;

import ar.edu.utn.turnero.turnero_backend.enums.RoleName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    private String tipo;
    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private RoleName rol;
}
