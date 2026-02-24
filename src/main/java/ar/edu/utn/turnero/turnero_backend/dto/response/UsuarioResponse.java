package ar.edu.utn.turnero.turnero_backend.dto.response;

import ar.edu.utn.turnero.turnero_backend.enums.RoleName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UsuarioResponse {
    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private RoleName rol;
    private boolean activo;
    private LocalDateTime creadoEn;
}
