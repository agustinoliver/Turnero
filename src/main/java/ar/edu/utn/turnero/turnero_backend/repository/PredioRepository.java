package ar.edu.utn.turnero.turnero_backend.repository;

import ar.edu.utn.turnero.turnero_backend.entity.Predio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PredioRepository extends JpaRepository<Predio, Long> {

    Optional<Predio> findByDuenoId(Long duenoId);

    Optional<Predio> findByDuenoEmail(String email);

    List<Predio> findByActivo(boolean activo);
}
