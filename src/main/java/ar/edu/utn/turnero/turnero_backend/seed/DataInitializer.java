package ar.edu.utn.turnero.turnero_backend.seed;

import ar.edu.utn.turnero.turnero_backend.entity.Usuario;
import ar.edu.utn.turnero.turnero_backend.enums.RoleName;
import ar.edu.utn.turnero.turnero_backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Inicializador de datos: crea el usuario ADMIN si no existe al arrancar la aplicación.
 * Credenciales por defecto:
 *   Email:    admin@turnero.com
 *   Password: Admin1234!
 *
 * ⚠️ Cambiar las credenciales en producción.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        crearAdminSiNoExiste();
    }

    private void crearAdminSiNoExiste() {
        final String adminEmail = "admin@turnero.com";

        if (usuarioRepository.existsByEmail(adminEmail)) {
            log.info("✅ Usuario ADMIN ya existe: {}", adminEmail);
            return;
        }

        Usuario admin = Usuario.builder()
                .nombre("Administrador")
                .apellido("Sistema")
                .email(adminEmail)
                .telefono("0000000000")
                .password(passwordEncoder.encode("Admin1234!"))
                .rol(RoleName.ROLE_ADMIN)
                .activo(true)
                .build();

        usuarioRepository.save(admin);
        log.info("✅ Usuario ADMIN creado:");
        log.info("   Email:    {}", adminEmail);
        log.info("   Password: Admin1234!");
        log.info("   ⚠️  Cambiá estas credenciales en producción!");
    }
}
