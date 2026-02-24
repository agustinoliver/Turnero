package ar.edu.utn.turnero.turnero_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageResponse {
    private String mensaje;

    public static MessageResponse of(String mensaje) {
        return new MessageResponse(mensaje);
    }
}
