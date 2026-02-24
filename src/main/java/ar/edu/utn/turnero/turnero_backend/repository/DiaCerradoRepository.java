package ar.edu.utn.turnero.turnero_backend.repository;

import ar.edu.utn.turnero.turnero_backend.entity.DiaCerrado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiaCerradoRepository extends JpaRepository<DiaCerrado, Long> {

    List<DiaCerrado> findByPredioId(Long predioId);

    Optional<DiaCerrado> findByPredioIdAndFecha(Long predioId, LocalDate fecha);

    boolean existsByPredioIdAndFecha(Long predioId, LocalDate fecha);

    List<DiaCerrado> findByPredioIdAndFechaBetween(Long predioId, LocalDate desde, LocalDate hasta);
}
