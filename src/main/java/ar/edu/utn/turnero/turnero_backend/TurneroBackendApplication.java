package ar.edu.utn.turnero.turnero_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Clase principal del sistema de Turnero para Canchas de Fútbol.
 */
@SpringBootApplication
@EnableScheduling
public class TurneroBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(TurneroBackendApplication.class, args);
    }
}
