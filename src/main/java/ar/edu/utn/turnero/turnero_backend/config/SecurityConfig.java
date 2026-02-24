package ar.edu.utn.turnero.turnero_backend.config;

import ar.edu.utn.turnero.turnero_backend.entity.Usuario;
import ar.edu.utn.turnero.turnero_backend.enums.RoleName;
import ar.edu.utn.turnero.turnero_backend.repository.UsuarioRepository;
import ar.edu.utn.turnero.turnero_backend.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración principal de seguridad Spring Security + JWT.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth

                // ─── Públicos ────────────────────────────────────────────────
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**",
                        "/swagger-resources/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/health").permitAll()

                // ─── ADMIN ───────────────────────────────────────────────────
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // ─── DUEÑO ───────────────────────────────────────────────────
                .requestMatchers("/api/owner/**").hasRole("DUENO")

                // ─── CLIENTE ─────────────────────────────────────────────────
                .requestMatchers("/api/client/**").hasRole("CLIENTE")

                // ─── Disponibilidad (pública para búsquedas) ─────────────────
                .requestMatchers(HttpMethod.GET, "/api/disponibilidad/**").permitAll()

                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public CommandLineRunner initAdmin(UsuarioRepository usuarioRepository,
                                       PasswordEncoder passwordEncoder) {
        return args -> {

            if (usuarioRepository.findByEmail("admin@turnero.com").isEmpty()) {

                Usuario admin = new Usuario();
                admin.setNombre("Administrador");
                admin.setApellido("Sistema");
                admin.setEmail("admin@turnero.com");
                admin.setTelefono("0000000000");
                admin.setPassword(passwordEncoder.encode("Admin1234!"));
                admin.setRol(RoleName.ROLE_ADMIN); // IMPORTANTE: sin ROLE_
                admin.setActivo(true);

                usuarioRepository.save(admin);

                System.out.println(">>> ADMIN CREADO AUTOMÁTICAMENTE <<<");
            }
        };
    }
}
