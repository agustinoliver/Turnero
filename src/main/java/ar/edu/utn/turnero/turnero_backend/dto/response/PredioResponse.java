package ar.edu.utn.turnero.turnero_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PredioResponse {
    private Long id;
    private String nombre;
    private String direccion;
    private String telefono;
    private String descripcion;
    private boolean activo;
    private LocalDateTime creadoEn;
    private UsuarioResponse dueno;
    private List<CanchaResponse> canchas;
    private List<HorarioDisponibleResponse> horariosDisponibles;
}
