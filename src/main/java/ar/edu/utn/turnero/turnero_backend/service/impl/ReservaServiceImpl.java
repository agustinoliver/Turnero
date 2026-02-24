package ar.edu.utn.turnero.turnero_backend.service.impl;

import ar.edu.utn.turnero.turnero_backend.dto.request.CancelarReservaRequest;
import ar.edu.utn.turnero.turnero_backend.dto.request.ReservaManualRequest;
import ar.edu.utn.turnero.turnero_backend.dto.request.ReservaRequest;
import ar.edu.utn.turnero.turnero_backend.dto.response.DisponibilidadResponse;
import ar.edu.utn.turnero.turnero_backend.dto.response.ReservaResponse;
import ar.edu.utn.turnero.turnero_backend.dto.response.ReservaTableroResponse;
import ar.edu.utn.turnero.turnero_backend.entity.Cancha;
import ar.edu.utn.turnero.turnero_backend.entity.HorarioDisponible;
import ar.edu.utn.turnero.turnero_backend.entity.Predio;
import ar.edu.utn.turnero.turnero_backend.entity.Reserva;
import ar.edu.utn.turnero.turnero_backend.entity.Usuario;
import ar.edu.utn.turnero.turnero_backend.enums.*;
import ar.edu.utn.turnero.turnero_backend.exception.BadRequestException;
import ar.edu.utn.turnero.turnero_backend.exception.ResourceConflictException;
import ar.edu.utn.turnero.turnero_backend.exception.ResourceNotFoundException;
import ar.edu.utn.turnero.turnero_backend.repository.CanchaRepository;
import ar.edu.utn.turnero.turnero_backend.repository.DiaCerradoRepository;
import ar.edu.utn.turnero.turnero_backend.repository.HorarioDisponibleRepository;
import ar.edu.utn.turnero.turnero_backend.repository.ReservaRepository;
import ar.edu.utn.turnero.turnero_backend.repository.UsuarioRepository;
import ar.edu.utn.turnero.turnero_backend.service.EmailService;
import ar.edu.utn.turnero.turnero_backend.service.MapperService;
import ar.edu.utn.turnero.turnero_backend.service.ReservaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservaServiceImpl implements ReservaService {

    private final ReservaRepository reservaRepository;
    private final CanchaRepository canchaRepository;
    private final UsuarioRepository usuarioRepository;
    private final HorarioDisponibleRepository horarioRepository;
    private final DiaCerradoRepository diaCerradoRepository;
    private final PredioServiceImpl predioService;
    private final MapperService mapper;
    private final EmailService emailService;

    // ─────────────────────────────────────────────────────────────────────────────
    // CREAR RESERVA (CLIENTE)
    // ─────────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ReservaResponse crearReserva(ReservaRequest request, String emailCliente) {
        Cancha cancha = findCancha(request.getCanchaId());
        Usuario cliente = findUsuario(emailCliente);

        validarHorarios(request.getHoraInicio(), request.getHoraFin());
        validarDivisionParaTipoCancha(cancha, request.getDivisionType());
        validarDiaAbierto(cancha.getPredio(), request.getFecha());
        validarDentroDeHorarioDisponible(cancha.getPredio(), request.getFecha(),
                request.getHoraInicio(), request.getHoraFin());
        validarSinConflicto(cancha, request.getFecha(), request.getHoraInicio(),
                request.getHoraFin(), request.getDivisionType(), null);

        Reserva reserva = Reserva.builder()
                .cancha(cancha)
                .cliente(cliente)
                .creadaPor(cliente)
                .fecha(request.getFecha())
                .horaInicio(request.getHoraInicio())
                .horaFin(request.getHoraFin())
                .divisionType(request.getDivisionType())
                .observaciones(request.getObservaciones())
                .estado(EstadoReserva.ACTIVA)
                .build();

        Reserva saved = reservaRepository.save(reserva);
        log.info("Reserva creada por cliente {}: ID={}", emailCliente, saved.getId());

        // Enviar confirmación por email
        enviarEmailConfirmacion(saved);

        return mapper.toReservaResponse(saved);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // CREAR RESERVA MANUAL (DUEÑO)
    // ─────────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ReservaResponse crearReservaManual(ReservaManualRequest request, String emailDueno) {
        Cancha cancha = findCancha(request.getCanchaId());

        // Verificar que la cancha pertenece al predio del dueño
        if (!cancha.getPredio().getDueno().getEmail().equals(emailDueno)) {
            throw new BadRequestException("La cancha no pertenece a tu predio");
        }

        Usuario cliente = usuarioRepository.findById(request.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + request.getClienteId()));
        Usuario dueno = findUsuario(emailDueno);

        validarHorarios(request.getHoraInicio(), request.getHoraFin());
        validarDivisionParaTipoCancha(cancha, request.getDivisionType());
        validarDiaAbierto(cancha.getPredio(), request.getFecha());
        validarDentroDeHorarioDisponible(cancha.getPredio(), request.getFecha(),
                request.getHoraInicio(), request.getHoraFin());
        validarSinConflicto(cancha, request.getFecha(), request.getHoraInicio(),
                request.getHoraFin(), request.getDivisionType(), null);

        Reserva reserva = Reserva.builder()
                .cancha(cancha)
                .cliente(cliente)
                .creadaPor(dueno)
                .fecha(request.getFecha())
                .horaInicio(request.getHoraInicio())
                .horaFin(request.getHoraFin())
                .divisionType(request.getDivisionType())
                .observaciones(request.getObservaciones())
                .estado(EstadoReserva.ACTIVA)
                .build();

        Reserva saved = reservaRepository.save(reserva);
        log.info("Reserva manual creada por dueño {} para cliente {}: ID={}",
                emailDueno, cliente.getEmail(), saved.getId());

        enviarEmailConfirmacion(saved);
        return mapper.toReservaResponse(saved);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // CANCELAR RESERVA
    // ─────────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void cancelarReserva(Long reservaId, String emailUsuario, CancelarReservaRequest request) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada: " + reservaId));

        if (reserva.getEstado() != EstadoReserva.ACTIVA) {
            throw new BadRequestException("La reserva no está activa");
        }

        // Verificar permisos: el cliente solo cancela las suyas; el dueño cancela las de su predio
        boolean esDueno = reserva.getCancha().getPredio().getDueno().getEmail().equals(emailUsuario);
        boolean esCliente = reserva.getCliente().getEmail().equals(emailUsuario);

        if (!esDueno && !esCliente) {
            throw new BadRequestException("No tenés permisos para cancelar esta reserva");
        }

        reserva.setEstado(EstadoReserva.CANCELADA);
        reserva.setCanceladaEn(LocalDateTime.now());
        reserva.setMotivoCancelacion(request != null ? request.getMotivo() : null);
        reservaRepository.save(reserva);

        // Notificar por email
        try {
            emailService.enviarCancelacionReserva(
                    reserva.getCliente().getEmail(),
                    reserva.getCliente().getNombre(),
                    reserva.getCancha().getNombre(),
                    reserva.getFecha().toString(),
                    reserva.getHoraInicio().toString(),
                    reserva.getHoraFin().toString()
            );
        } catch (Exception e) {
            log.warn("No se pudo enviar email de cancelación: {}", e.getMessage());
        }

        log.info("Reserva {} cancelada por {}", reservaId, emailUsuario);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // CONSULTAS
    // ─────────────────────────────────────────────────────────────────────────────

    @Override
    public ReservaResponse obtenerPorId(Long id) {
        return mapper.toReservaResponse(reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada: " + id)));
    }

    @Override
    public List<ReservaResponse> listarReservasDeCliente(String emailCliente) {
        Usuario cliente = findUsuario(emailCliente);
        return reservaRepository.findByClienteId(cliente.getId()).stream()
                .map(mapper::toReservaResponse).collect(Collectors.toList());
    }

    @Override
    public List<ReservaResponse> listarReservasPorPredioYFecha(String emailDueno, LocalDate fecha) {
        Predio predio = predioService.findPredioByDueno(emailDueno);
        return reservaRepository.findActivasByPredioAndFecha(predio.getId(), fecha).stream()
                .map(mapper::toReservaResponse).collect(Collectors.toList());
    }

    @Override
    public List<ReservaResponse> listarTodasReservasDePredio(String emailDueno) {
        Predio predio = predioService.findPredioByDueno(emailDueno);
        return reservaRepository.findAllByPredioId(predio.getId()).stream()
                .map(mapper::toReservaResponse).collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // VISTA TABLERO
    // ─────────────────────────────────────────────────────────────────────────────

    @Override
    public ReservaTableroResponse obtenerTablero(String emailDueno, LocalDate fecha) {
        Predio predio = predioService.findPredioByDueno(emailDueno);
        List<Cancha> canchas = canchaRepository.findByPredioIdAndActiva(predio.getId(), true);
        List<Reserva> reservasDia = reservaRepository.findActivasByPredioAndFecha(predio.getId(), fecha);

        // Obtener horarios disponibles del predio para el día
        DiaSemana diaSemana = mapDayOfWeek(fecha.getDayOfWeek());
        List<HorarioDisponible> horarios = horarioRepository.findByPredioIdAndDiaSemana(predio.getId(), diaSemana);

        List<ReservaTableroResponse.CanchaTablero> canchasTablero = canchas.stream()
                .map(cancha -> {
                    List<Reserva> reservasCancha = reservasDia.stream()
                            .filter(r -> r.getCancha().getId().equals(cancha.getId()))
                            .collect(Collectors.toList());

                    List<ReservaTableroResponse.SlotHorario> slots = new ArrayList<>();

                    // Generar slots de 1 hora para cada franja horaria
                    for (HorarioDisponible h : horarios) {
                        LocalTime cursor = h.getHoraInicio();
                        while (cursor.plusHours(1).compareTo(h.getHoraFin()) <= 0) {
                            final LocalTime slotInicio = cursor;
                            final LocalTime slotFin = cursor.plusHours(1);

                            // Buscar si hay reserva en este slot
                            Optional<Reserva> reservaSlot = reservasCancha.stream()
                                    .filter(r -> r.getHoraInicio().isBefore(slotFin)
                                            && r.getHoraFin().isAfter(slotInicio))
                                    .findFirst();

                            boolean disponible = reservaSlot.isEmpty();
                            slots.add(ReservaTableroResponse.SlotHorario.builder()
                                    .horaInicio(slotInicio)
                                    .horaFin(slotFin)
                                    .disponible(disponible)
                                    .reserva(disponible ? null : mapper.toReservaResponse(reservaSlot.get()))
                                    .build());

                            cursor = cursor.plusHours(1);
                        }
                    }

                    return ReservaTableroResponse.CanchaTablero.builder()
                            .canchaId(cancha.getId())
                            .canchaNombre(cancha.getNombre())
                            .tipo(cancha.getTipo().name())
                            .precioPorHora(cancha.getPrecioPorHora())
                            .slots(slots)
                            .build();
                })
                .collect(Collectors.toList());

        return ReservaTableroResponse.builder()
                .fecha(fecha)
                .canchas(canchasTablero)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // DISPONIBILIDAD
    // ─────────────────────────────────────────────────────────────────────────────

    @Override
    public DisponibilidadResponse obtenerDisponibilidad(Long canchaId, LocalDate fecha) {
        Cancha cancha = findCancha(canchaId);
        List<DisponibilidadResponse.SlotDisponible> slotsDisponibles =
                calcularSlotsDisponibles(cancha, fecha);

        return DisponibilidadResponse.builder()
                .canchaId(cancha.getId())
                .canchaNombre(cancha.getNombre())
                .tipo(cancha.getTipo())
                .precioPorHora(cancha.getPrecioPorHora())
                .fecha(fecha)
                .slotsDisponibles(slotsDisponibles)
                .build();
    }

    @Override
    public List<DisponibilidadResponse> buscarDisponibilidad(Long predioId, TipoCancha tipo, LocalDate fecha) {
        List<Cancha> canchas;
        if (tipo != null) {
            canchas = canchaRepository.findByPredioIdAndTipoAndActiva(predioId, tipo, true);
        } else {
            canchas = canchaRepository.findByPredioIdAndActiva(predioId, true);
        }

        return canchas.stream()
                .map(c -> {
                    List<DisponibilidadResponse.SlotDisponible> slots = calcularSlotsDisponibles(c, fecha);
                    return DisponibilidadResponse.builder()
                            .canchaId(c.getId())
                            .canchaNombre(c.getNombre())
                            .tipo(c.getTipo())
                            .precioPorHora(c.getPrecioPorHora())
                            .fecha(fecha)
                            .slotsDisponibles(slots)
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // LÓGICA DE NEGOCIO - VALIDACIONES Y CONFLICTOS DE DIVISIÓN
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Verifica que la hora de fin sea posterior a la de inicio.
     */
    private void validarHorarios(LocalTime inicio, LocalTime fin) {
        if (!fin.isAfter(inicio)) {
            throw new BadRequestException("La hora de fin debe ser posterior a la hora de inicio");
        }
    }

    /**
     * Verifica que el tipo de división sea válido para el tipo de cancha.
     */
    private void validarDivisionParaTipoCancha(Cancha cancha, DivisionType division) {
        if (division == null || division == DivisionType.WHOLE) return;

        List<DivisionType> permitidas = cancha.getDivisionesDisponibles();
        if (!permitidas.contains(division)) {
            throw new BadRequestException(
                    "El tipo de división '" + division + "' no es válido para una cancha de tipo " + cancha.getTipo());
        }
    }

    /**
     * Verifica que la fecha no sea un día cerrado del predio.
     */
    private void validarDiaAbierto(Predio predio, LocalDate fecha) {
        if (diaCerradoRepository.existsByPredioIdAndFecha(predio.getId(), fecha)) {
            throw new BadRequestException("El predio está cerrado el día: " + fecha);
        }
    }

    /**
     * Verifica que el horario solicitado esté dentro de los horarios disponibles del predio.
     */
    private void validarDentroDeHorarioDisponible(Predio predio, LocalDate fecha,
                                                   LocalTime horaInicio, LocalTime horaFin) {
        DiaSemana dia = mapDayOfWeek(fecha.getDayOfWeek());
        List<HorarioDisponible> horarios = horarioRepository.findByPredioIdAndDiaSemana(predio.getId(), dia);

        if (horarios.isEmpty()) {
            throw new BadRequestException("El predio no está abierto el día: " + dia);
        }

        boolean dentroDeHorario = horarios.stream().anyMatch(h ->
                !horaInicio.isBefore(h.getHoraInicio()) && !horaFin.isAfter(h.getHoraFin()));

        if (!dentroDeHorario) {
            throw new BadRequestException(
                    "El horario solicitado (" + horaInicio + " - " + horaFin +
                    ") está fuera del horario de apertura del predio");
        }
    }

    /**
     * Núcleo de la lógica de conflictos.
     * <p>
     * Reglas:
     * 1. Una reserva WHOLE bloquea todo el horario, sin importar divisiones.
     * 2. Una nueva reserva WHOLE no puede crearse si ya hay cualquier reserva en ese horario.
     * 3. Para divisiones específicas se verifica compatibilidad:
     *    - SIETE_CINCO_A y SIETE_CINCO_B son excluyentes entre sí (no pueden coincidir).
     *    - TRES_CINCO_A, _B y _C son excluyentes entre sí (cada tercio es único).
     *    - DOS_CINCO_A y DOS_CINCO_B son excluyentes entre sí.
     *    - Una reserva SIETE_CINCO bloquea la TRES_CINCO (no pueden coexistir en la misma cancha de 9).
     * </p>
     *
     * @param excludeReservaId ID de reserva a excluir (para edición futura).
     */
    private void validarSinConflicto(Cancha cancha, LocalDate fecha, LocalTime horaInicio,
                                      LocalTime horaFin, DivisionType nuevaDivision, Long excludeReservaId) {
        List<Reserva> existentes = reservaRepository.findActivasByCanchaAndFecha(cancha.getId(), fecha);

        for (Reserva existente : existentes) {
            if (excludeReservaId != null && existente.getId().equals(excludeReservaId)) continue;

            // Solo analizar si hay solapamiento temporal
            if (!hayConflictoTemporal(existente.getHoraInicio(), existente.getHoraFin(), horaInicio, horaFin)) {
                continue;
            }

            DivisionType existenteDivision = existente.getDivisionType();

            // Regla 1: Si la existente es WHOLE, bloquea todo
            if (existenteDivision == DivisionType.WHOLE) {
                throw new ResourceConflictException(
                        "La cancha ya está reservada completa en ese horario (" +
                        existente.getHoraInicio() + " - " + existente.getHoraFin() + ")");
            }

            // Regla 2: Si la nueva es WHOLE, no puede haber ninguna reserva en ese horario
            if (nuevaDivision == DivisionType.WHOLE) {
                throw new ResourceConflictException(
                        "No se puede reservar la cancha completa: ya existe una reserva de la división " +
                        existenteDivision + " en ese horario");
            }

            // Regla 3: Verificar compatibilidad de divisiones
            if (hayConflictoDivision(existenteDivision, nuevaDivision, cancha.getTipo())) {
                throw new ResourceConflictException(
                        "La división solicitada (" + nuevaDivision + ") ya está ocupada en ese horario. " +
                        "División en conflicto: " + existenteDivision);
            }
        }
    }

    /**
     * Determina si dos intervalos horarios se superponen.
     */
    private boolean hayConflictoTemporal(LocalTime inicio1, LocalTime fin1,
                                          LocalTime inicio2, LocalTime fin2) {
        return inicio2.isBefore(fin1) && fin2.isAfter(inicio1);
    }

    /**
     * Determina si dos divisiones son incompatibles entre sí para el tipo de cancha dado.
     * <p>
     * Grupos de colisión:
     * - SIETE_CINCO_A ↔ SIETE_CINCO_B  → incompatibles (cancha de 9 dividida en 7+5)
     * - SIETE_CINCO_*  ↔ TRES_CINCO_*  → incompatibles (no se pueden mezclar formas de dividir una 9)
     * - TRES_CINCO_A ↔ TRES_CINCO_B ↔ TRES_CINCO_C → cada tercio es único (A vs B vs C → incompatible)
     * - DOS_CINCO_A ↔ DOS_CINCO_B → incompatibles (cancha de 7 dividida en 2)
     */
    private boolean hayConflictoDivision(DivisionType existente, DivisionType nueva, TipoCancha tipo) {
        if (existente == nueva) return true; // Misma sección exacta → conflicto

        // Cancha de 9
        if (tipo == TipoCancha.NUEVE) {
            Set<DivisionType> grupoCinco_SieteCinco = Set.of(DivisionType.SIETE_CINCO_A, DivisionType.SIETE_CINCO_B);
            Set<DivisionType> grupoTresCinco = Set.of(DivisionType.TRES_CINCO_A, DivisionType.TRES_CINCO_B, DivisionType.TRES_CINCO_C);

            boolean existenteEsSieteCinco = grupoCinco_SieteCinco.contains(existente);
            boolean nuevaEsSieteCinco = grupoCinco_SieteCinco.contains(nueva);
            boolean existenteEsTresCinco = grupoTresCinco.contains(existente);
            boolean nuevaEsTresCinco = grupoTresCinco.contains(nueva);

            // Mezcla SIETE_CINCO con TRES_CINCO → siempre conflicto
            if ((existenteEsSieteCinco && nuevaEsTresCinco) ||
                (existenteEsTresCinco && nuevaEsSieteCinco)) {
                return true;
            }

            // Dentro de TRES_CINCO: cada letra es una sección diferente → solo conflicto si es la misma
            if (existenteEsTresCinco && nuevaEsTresCinco) {
                return existente == nueva; // ya cubierto arriba, pero explicitado
            }

            // SIETE_CINCO_A y SIETE_CINCO_B son dos secciones distintas → no colisionan entre sí
            // (A es la mitad de 7, B es la mitad de 5)
            if (existenteEsSieteCinco && nuevaEsSieteCinco) {
                return existente == nueva;
            }
        }

        // Cancha de 7: DOS_CINCO_A y DOS_CINCO_B son secciones distintas
        if (tipo == TipoCancha.SIETE) {
            if (existente == DivisionType.DOS_CINCO_A && nueva == DivisionType.DOS_CINCO_B) return false;
            if (existente == DivisionType.DOS_CINCO_B && nueva == DivisionType.DOS_CINCO_A) return false;
        }

        return false;
    }

    /**
     * Calcula los slots disponibles de 1 hora para una cancha en una fecha.
     */
    private List<DisponibilidadResponse.SlotDisponible> calcularSlotsDisponibles(Cancha cancha, LocalDate fecha) {
        // Verificar si el día está cerrado
        if (diaCerradoRepository.existsByPredioIdAndFecha(cancha.getPredio().getId(), fecha)) {
            return List.of();
        }

        DiaSemana dia = mapDayOfWeek(fecha.getDayOfWeek());
        List<HorarioDisponible> horarios = horarioRepository.findByPredioIdAndDiaSemana(cancha.getPredio().getId(), dia);

        if (horarios.isEmpty()) return List.of();

        List<Reserva> reservasExistentes = reservaRepository.findActivasByCanchaAndFecha(cancha.getId(), fecha);
        List<DisponibilidadResponse.SlotDisponible> slotsDisponibles = new ArrayList<>();

        for (HorarioDisponible horario : horarios) {
            LocalTime cursor = horario.getHoraInicio();
            while (cursor.plusHours(1).compareTo(horario.getHoraFin()) <= 0) {
                final LocalTime slotInicio = cursor;
                final LocalTime slotFin = cursor.plusHours(1);

                List<DivisionType> divisionesDisponibles = calcularDivisionesDisponiblesParaSlot(
                        cancha, slotInicio, slotFin, reservasExistentes);

                if (!divisionesDisponibles.isEmpty()) {
                    slotsDisponibles.add(DisponibilidadResponse.SlotDisponible.builder()
                            .horaInicio(slotInicio)
                            .horaFin(slotFin)
                            .divisionesDisponibles(divisionesDisponibles)
                            .build());
                }

                cursor = cursor.plusHours(1);
            }
        }

        return slotsDisponibles;
    }

    /**
     * Determina qué divisiones están disponibles para un slot horario dado,
     * teniendo en cuenta las reservas existentes en esa cancha.
     */
    private List<DivisionType> calcularDivisionesDisponiblesParaSlot(
            Cancha cancha, LocalTime slotInicio, LocalTime slotFin,
            List<Reserva> reservasExistentes) {

        List<Reserva> reservasEnSlot = reservasExistentes.stream()
                .filter(r -> hayConflictoTemporal(r.getHoraInicio(), r.getHoraFin(), slotInicio, slotFin))
                .collect(Collectors.toList());

        List<DivisionType> todasLasDivisiones = cancha.getDivisionesDisponibles();
        List<DivisionType> disponibles = new ArrayList<>(todasLasDivisiones);

        for (Reserva reserva : reservasEnSlot) {
            DivisionType ocupada = reserva.getDivisionType();

            // Si alguna reserva ocupa el WHOLE, no hay nada disponible
            if (ocupada == DivisionType.WHOLE) {
                return List.of();
            }

            // Remover las divisiones que entran en conflicto con la reserva existente
            disponibles.removeIf(d -> {
                if (d == DivisionType.WHOLE) return true; // WHOLE no disponible si hay alguna reserva
                return hayConflictoDivision(ocupada, d, cancha.getTipo());
            });
        }

        return disponibles;
    }

    private DiaSemana mapDayOfWeek(DayOfWeek dow) {
        return switch (dow) {
            case MONDAY -> DiaSemana.LUNES;
            case TUESDAY -> DiaSemana.MARTES;
            case WEDNESDAY -> DiaSemana.MIERCOLES;
            case THURSDAY -> DiaSemana.JUEVES;
            case FRIDAY -> DiaSemana.VIERNES;
            case SATURDAY -> DiaSemana.SABADO;
            case SUNDAY -> DiaSemana.DOMINGO;
        };
    }

    private Cancha findCancha(Long id) {
        return canchaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cancha no encontrada: " + id));
    }

    private Usuario findUsuario(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + email));
    }

    private void enviarEmailConfirmacion(Reserva reserva) {
        try {
            emailService.enviarConfirmacionReserva(
                    reserva.getCliente().getEmail(),
                    reserva.getCliente().getNombre(),
                    reserva.getCancha().getNombre(),
                    reserva.getFecha().toString(),
                    reserva.getHoraInicio().toString(),
                    reserva.getHoraFin().toString(),
                    reserva.getCancha().getPredio().getNombre()
            );
        } catch (Exception e) {
            log.warn("No se pudo enviar email de confirmación: {}", e.getMessage());
        }
    }
}
