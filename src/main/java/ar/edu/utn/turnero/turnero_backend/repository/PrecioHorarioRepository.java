package ar.edu.utn.turnero.turnero_backend.repository;

import ar.edu.utn.turnero.turnero_backend.entity.PrecioHorario;
import ar.edu.utn.turnero.turnero_backend.enums.DiaSemana;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PrecioHorarioRepository extends JpaRepository<PrecioHorario, Long> {

    List<PrecioHorario> findByCanchaId(Long canchaId);

    List<PrecioHorario> findByCanchaIdAndDiaSemana(Long canchaId, DiaSemana diaSemana);

    @Modifying
    void deleteByCanchaId(Long canchaId);

    /**
     * Busca el precio configurado para un slot horario de una cancha.
     * Retorna el registro cuya franja cubre exactamente [horaInicio, horaFin).
     */
    @Query("""
    SELECT p FROM PrecioHorario p
    WHERE p.cancha.id  = :canchaId
      AND p.diaSemana  = :dia
      AND p.horaInicio <= :horaInicio
      AND p.horaFin   >= :horaFin
    ORDER BY p.horaInicio DESC
    """)
    List<PrecioHorario> findPreciosParaSlot(
            @Param("canchaId")   Long canchaId,
            @Param("dia")        DiaSemana dia,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin")    LocalTime horaFin
    );
}
