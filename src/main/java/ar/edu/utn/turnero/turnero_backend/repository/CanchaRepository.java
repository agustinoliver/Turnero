package ar.edu.utn.turnero.turnero_backend.repository;

import ar.edu.utn.turnero.turnero_backend.entity.Cancha;
import ar.edu.utn.turnero.turnero_backend.enums.TipoCancha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CanchaRepository extends JpaRepository<Cancha, Long> {

    List<Cancha> findByPredioId(Long predioId);

    List<Cancha> findByPredioIdAndActiva(Long predioId, boolean activa);

    List<Cancha> findByPredioIdAndTipo(Long predioId, TipoCancha tipo);

    List<Cancha> findByPredioIdAndTipoAndActiva(Long predioId, TipoCancha tipo, boolean activa);
}
