package ar.edu.utn.turnero.turnero_backend.repository;

import ar.edu.utn.turnero.turnero_backend.entity.Reserva;
import ar.edu.utn.turnero.turnero_backend.enums.EstadoReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findByCanchaIdAndFechaAndEstado(Long canchaId, LocalDate fecha, EstadoReserva estado);

    List<Reserva> findByCanchaIdAndFecha(Long canchaId, LocalDate fecha);

    List<Reserva> findByClienteId(Long clienteId);

    List<Reserva> findByClienteIdAndEstado(Long clienteId, EstadoReserva estado);

    @Query("SELECT r FROM Reserva r WHERE r.cancha.predio.id = :predioId AND r.fecha = :fecha AND r.estado = 'ACTIVA' ORDER BY r.cancha.id, r.horaInicio")
    List<Reserva> findActivasByPredioAndFecha(@Param("predioId") Long predioId, @Param("fecha") LocalDate fecha);

    @Query("SELECT r FROM Reserva r WHERE r.fecha BETWEEN :desde AND :hasta AND r.estado = 'ACTIVA' ORDER BY r.fecha, r.horaInicio")
    List<Reserva> findActivasByFechaRange(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

    @Query("SELECT r FROM Reserva r WHERE r.cancha.predio.id = :predioId AND r.fecha BETWEEN :desde AND :hasta AND r.estado = 'ACTIVA' ORDER BY r.fecha, r.horaInicio")
    List<Reserva> findActivasByPredioAndFechaRange(@Param("predioId") Long predioId, @Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

    @Query("SELECT r FROM Reserva r WHERE r.cancha.predio.id = :predioId ORDER BY r.fecha DESC, r.horaInicio DESC")
    List<Reserva> findAllByPredioId(@Param("predioId") Long predioId);

    @Query("SELECT r FROM Reserva r WHERE r.cancha.id = :canchaId AND r.fecha = :fecha AND r.estado = 'ACTIVA'")
    List<Reserva> findActivasByCanchaAndFecha(@Param("canchaId") Long canchaId, @Param("fecha") LocalDate fecha);
}
