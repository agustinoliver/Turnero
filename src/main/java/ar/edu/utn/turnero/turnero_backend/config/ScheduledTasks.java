package ar.edu.utn.turnero.turnero_backend.config;

import ar.edu.utn.turnero.turnero_backend.repository.PasswordResetTokenRepository;
import ar.edu.utn.turnero.turnero_backend.repository.ReservaRepository;
import ar.edu.utn.turnero.turnero_backend.entity.Reserva;
import ar.edu.utn.turnero.turnero_backend.enums.EstadoReserva;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasks {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final ReservaRepository reservaRepository;

    @Scheduled(fixedRate = 3_600_000)
    @Transactional
    public void limpiarTokensExpirados() {
        log.info("Limpiando tokens de recuperación expirados");
        passwordResetTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }

    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void completarReservasPasadas() {
        log.info("Marcando reservas pasadas como COMPLETADAS");
        LocalDate ayer = LocalDate.now().minusDays(1);
        List<Reserva> reservasPasadas = reservaRepository
                .findActivasByFechaRange(LocalDate.of(2000, 1, 1), ayer);
        reservasPasadas.forEach(r -> r.setEstado(EstadoReserva.COMPLETADA));
        if (!reservasPasadas.isEmpty()) {
            reservaRepository.saveAll(reservasPasadas);
            log.info("Reservas completadas: {}", reservasPasadas.size());
        }
    }
}
