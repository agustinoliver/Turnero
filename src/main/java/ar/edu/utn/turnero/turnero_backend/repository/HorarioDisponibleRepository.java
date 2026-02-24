package ar.edu.utn.turnero.turnero_backend.repository;

import ar.edu.utn.turnero.turnero_backend.entity.HorarioDisponible;
import ar.edu.utn.turnero.turnero_backend.enums.DiaSemana;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HorarioDisponibleRepository extends JpaRepository<HorarioDisponible, Long> {

    List<HorarioDisponible> findByPredioId(Long predioId);

    List<HorarioDisponible> findByPredioIdAndDiaSemana(Long predioId, DiaSemana diaSemana);

    void deleteByPredioId(Long predioId);
}
