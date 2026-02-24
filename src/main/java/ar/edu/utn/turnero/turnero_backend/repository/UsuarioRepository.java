package ar.edu.utn.turnero.turnero_backend.repository;

import ar.edu.utn.turnero.turnero_backend.entity.Usuario;
import ar.edu.utn.turnero.turnero_backend.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Usuario> findByRol(RoleName rol);

    List<Usuario> findByActivo(boolean activo);
}
