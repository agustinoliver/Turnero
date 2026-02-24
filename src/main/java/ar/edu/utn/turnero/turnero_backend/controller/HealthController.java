package ar.edu.utn.turnero.turnero_backend.controller;

import ar.edu.utn.turnero.turnero_backend.dto.response.MessageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<MessageResponse> health() {
        return ResponseEntity.ok(MessageResponse.of("Turnero Backend is running!"));
    }
}
