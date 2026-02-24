package ar.edu.utn.turnero.turnero_backend.enums;

/**
 * Tipos de división disponibles para las canchas.
 * <p>
 * WHOLE    → La cancha se usa completa (sin división).
 * SIETE_CINCO → Cancha de 9 dividida en 7+5 (ocupa el "lado 7" o el "lado 5").
 * TRES_CINCO  → Cancha de 9 dividida en 3 canchas de 5.
 * DOS_CINCO   → Cancha de 7 dividida en 2 canchas de 5.
 * <p>
 * Para SIETE_CINCO usamos sufijos _A y _B para identificar cada mitad.
 * Para TRES_CINCO usamos sufijos _A, _B y _C.
 * Para DOS_CINCO  usamos sufijos _A y _B.
 */
public enum DivisionType {
    WHOLE,          // Cancha completa, sin división
    SIETE_CINCO_A,  // Cancha 9 → mitad de 7  (1er mitad)
    SIETE_CINCO_B,  // Cancha 9 → mitad de 5  (2da mitad)
    TRES_CINCO_A,   // Cancha 9 → tercio A de 5
    TRES_CINCO_B,   // Cancha 9 → tercio B de 5
    TRES_CINCO_C,   // Cancha 9 → tercio C de 5
    DOS_CINCO_A,    // Cancha 7 → mitad A de 5
    DOS_CINCO_B     // Cancha 7 → mitad B de 5
}
