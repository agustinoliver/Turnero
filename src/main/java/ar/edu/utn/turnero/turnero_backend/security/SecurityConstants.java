package ar.edu.utn.turnero.turnero_backend.security;

/**
 * Constantes de seguridad para JWT.
 * ⚠️ En producción usar variables de entorno para JWT_SECRET.
 */
public class SecurityConstants {

    public static final String JWT_SECRET = "turnero-canchas-futbol-super-secret-key-2024-change-in-production";
    public static final long JWT_EXPIRATION_MS = 1000L * 60 * 60 * 8; // 8 horas
    public static final String JWT_HEADER = "Authorization";
    public static final String JWT_PREFIX = "Bearer ";

    private SecurityConstants() {}
}
