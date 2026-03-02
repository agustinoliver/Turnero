package ar.edu.utn.turnero.turnero_backend.service.impl;

import ar.edu.utn.turnero.turnero_backend.dto.request.CancelarReservaRequest;
import ar.edu.utn.turnero.turnero_backend.dto.request.ReservaManualRequest;
import ar.edu.utn.turnero.turnero_backend.dto.request.ReservaRequest;
import ar.edu.utn.turnero.turnero_backend.dto.response.DisponibilidadResponse;
import ar.edu.utn.turnero.turnero_backend.dto.response.ReservaResponse;
import ar.edu.utn.turnero.turnero_backend.dto.response.ReservaTableroResponse;
import ar.edu.utn.turnero.turnero_backend.entity.*;
import ar.edu.utn.turnero.turnero_backend.enums.*;
import ar.edu.utn.turnero.turnero_backend.exception.BadRequestException;
import ar.edu.utn.turnero.turnero_backend.exception.ResourceConflictException;
import ar.edu.utn.turnero.turnero_backend.exception.ResourceNotFoundException;
import ar.edu.utn.turnero.turnero_backend.repository.*;
import ar.edu.utn.turnero.turnero_backend.service.EmailService;
import ar.edu.utn.turnero.turnero_backend.service.MapperService;
import ar.edu.utn.turnero.turnero_backend.service.ReservaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private final PrecioHorarioRepository precioHorarioRepository;
    private final PredioServiceImpl predioService;
    private final MapperService mapper;
    private final EmailService emailService;

    // ─────────────────────────────────────────────────────────────────────────
    // CREAR RESERVA (CLIENTE)
    // ─────────────────────────────────────────────────────────────────────────

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
        enviarEmailConfirmacion(saved);
        return mapper.toReservaResponse(saved);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CREAR RESERVA MANUAL (DUEÑO)
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ReservaResponse crearReservaManual(ReservaManualRequest request, String emailDueno) {
        Cancha cancha = findCancha(request.getCanchaId());

        if (!cancha.getPredio().getDueno().getEmail().equals(emailDueno)) {
            throw new BadRequestException("La cancha no pertenece a tu predio");
        }

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
                .cliente(null)
                .creadaPor(dueno)
                .nombreClienteManual(request.getNombreCliente())
                .fecha(request.getFecha())
                .horaInicio(request.getHoraInicio())
                .horaFin(request.getHoraFin())
                .divisionType(request.getDivisionType())
                .observaciones(request.getObservaciones())
                .estado(EstadoReserva.ACTIVA)
                .build();

        Reserva saved = reservaRepository.save(reserva);
        log.info("Reserva manual creada por dueño {} para cliente {}: ID={}",
                emailDueno, request.getNombreCliente(), saved.getId());
        enviarEmailConfirmacion(saved);
        return mapper.toReservaResponse(saved);
    }

    @Override
    @Transactional
    public void cancelarReserva(Long reservaId, String emailUsuario,
                                CancelarReservaRequest request) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada: " + reservaId));

        if (reserva.getEstado() != EstadoReserva.ACTIVA) {
            throw new BadRequestException("La reserva no está activa");
        }

        boolean esDueno = reserva.getCancha().getPredio().getDueno().getEmail().equals(emailUsuario);
        boolean esCliente = reserva.getCliente() != null &&
                reserva.getCliente().getEmail().equals(emailUsuario);

        if (!esDueno && !esCliente) {
            throw new BadRequestException("No tenés permisos para cancelar esta reserva");
        }

        reserva.setEstado(EstadoReserva.CANCELADA);
        reserva.setCanceladaEn(LocalDateTime.now());
        reserva.setMotivoCancelacion(request != null ? request.getMotivo() : null);
        reservaRepository.save(reserva);

        try {
            if (reserva.getCliente() != null) {
                emailService.enviarCancelacionReserva(
                        reserva.getCliente().getEmail(),
                        reserva.getCliente().getNombre(),
                        reserva.getCancha().getNombre(),
                        reserva.getFecha().toString(),
                        reserva.getHoraInicio().toString(),
                        reserva.getHoraFin().toString()
                );
            }
        } catch (Exception e) {
            log.warn("No se pudo enviar email de cancelación: {}", e.getMessage());
        }
    }

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

    // ─────────────────────────────────────────────────────────────────────────
    // TABLERO (CORREGIDO LOOP INFINITO)
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public ReservaTableroResponse obtenerTablero(String emailDueno, LocalDate fecha) {
        Predio predio = predioService.findPredioByDueno(emailDueno);
        List<Cancha> canchas = canchaRepository.findByPredioIdAndActiva(predio.getId(), true);
        List<Reserva> reservasDia = reservaRepository.findActivasByPredioAndFecha(predio.getId(), fecha);

        DiaSemana diaSemana = mapDayOfWeek(fecha.getDayOfWeek());
        List<HorarioDisponible> horarios =
                horarioRepository.findByPredioIdAndDiaSemana(predio.getId(), diaSemana);

        List<ReservaTableroResponse.CanchaTablero> canchasTablero = canchas.stream()
                .map(cancha -> {
                    List<Reserva> reservasCancha = reservasDia.stream()
                            .filter(r -> r.getCancha().getId().equals(cancha.getId()))
                            .collect(Collectors.toList());

                    List<ReservaTableroResponse.SlotHorario> slots = new ArrayList<>();
                    for (HorarioDisponible h : horarios) {
                        LocalTime cursor = h.getHoraInicio();
                        LocalTime limiteFin = h.getHoraFin();

                        // Loop seguro que detecta el fin de jornada (incluido medianoche)
                        while (true) {
                            LocalTime slotInicio = cursor;
                            LocalTime slotFin = cursor.plusHours(1);

                            // Si el slot excede el cierre (considerando medianoche como 24:00)
                            if (!limiteFin.equals(LocalTime.MIDNIGHT)) {
                                if (slotFin.isAfter(limiteFin) || (slotFin.equals(LocalTime.MIDNIGHT) && !limiteFin.equals(LocalTime.MIDNIGHT))) {
                                    break;
                                }
                            }

                            Optional<Reserva> reservaSlot = reservasCancha.stream()
                                    .filter(r -> hayConflictoTemporal(r.getHoraInicio(), r.getHoraFin(), slotInicio, slotFin))
                                    .findFirst();

                            boolean disponible = reservaSlot.isEmpty();
                            slots.add(ReservaTableroResponse.SlotHorario.builder()
                                    .horaInicio(slotInicio)
                                    .horaFin(slotFin)
                                    .disponible(disponible)
                                    .reserva(disponible ? null : mapper.toReservaResponse(reservaSlot.get()))
                                    .build());

                            cursor = slotFin;
                            // Si el cursor llegó al límite o dio la vuelta a medianoche, frenamos
                            if (cursor.equals(limiteFin) || cursor.equals(LocalTime.MIDNIGHT)) break;
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

    // ─────────────────────────────────────────────────────────────────────────
    // DISPONIBILIDAD (CORREGIDO LOOP INFINITO)
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public DisponibilidadResponse obtenerDisponibilidad(Long canchaId, LocalDate fecha) {
        Cancha cancha = findCancha(canchaId);
        return DisponibilidadResponse.builder()
                .canchaId(cancha.getId())
                .canchaNombre(cancha.getNombre())
                .tipo(cancha.getTipo())
                .precioPorHora(cancha.getPrecioPorHora())
                .fecha(fecha)
                .slotsDisponibles(calcularSlotsDisponibles(cancha, fecha))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DisponibilidadResponse> buscarDisponibilidad(Long predioId,
                                                             TipoCancha tipo,
                                                             LocalDate fecha) {
        List<Cancha> canchas = (tipo != null)
                ? canchaRepository.findByPredioIdAndTipoAndActiva(predioId, tipo, true)
                : canchaRepository.findByPredioIdAndActiva(predioId, true);

        return canchas.stream()
                .map(c -> DisponibilidadResponse.builder()
                        .canchaId(c.getId())
                        .canchaNombre(c.getNombre())
                        .tipo(c.getTipo())
                        .precioPorHora(c.getPrecioPorHora())
                        .fecha(fecha)
                        .slotsDisponibles(calcularSlotsDisponibles(c, fecha))
                        .build())
                .collect(Collectors.toList());
    }

    private List<DisponibilidadResponse.SlotDisponible> calcularSlotsDisponibles(
            Cancha cancha, LocalDate fecha) {

        if (diaCerradoRepository.existsByPredioIdAndFecha(cancha.getPredio().getId(), fecha)) {
            return List.of();
        }

        DiaSemana dia = mapDayOfWeek(fecha.getDayOfWeek());
        List<HorarioDisponible> horarios =
                horarioRepository.findByPredioIdAndDiaSemana(cancha.getPredio().getId(), dia);

        if (horarios.isEmpty()) return List.of();

        List<Reserva> reservasExistentes =
                reservaRepository.findActivasByCanchaAndFecha(cancha.getId(), fecha);

        List<DisponibilidadResponse.SlotDisponible> slotsDisponibles = new ArrayList<>();

        for (HorarioDisponible horario : horarios) {
            LocalTime cursor = horario.getHoraInicio();
            LocalTime limiteFin = horario.getHoraFin();

            while (true) {
                LocalTime slotInicio = cursor;
                LocalTime slotFin = cursor.plusHours(1);

                // Control de fin de jornada
                if (!limiteFin.equals(LocalTime.MIDNIGHT)) {
                    if (slotFin.isAfter(limiteFin) || (slotFin.equals(LocalTime.MIDNIGHT) && !limiteFin.equals(LocalTime.MIDNIGHT))) {
                        break;
                    }
                }

                List<DivisionType> divisionesDisponibles =
                        calcularDivisionesDisponiblesParaSlot(cancha, slotInicio, slotFin, reservasExistentes);

                if (!divisionesDisponibles.isEmpty()) {
                    BigDecimal precioEfectivo = resolverPrecioSlot(cancha, dia, slotInicio, slotFin);
                    slotsDisponibles.add(DisponibilidadResponse.SlotDisponible.builder()
                            .horaInicio(slotInicio)
                            .horaFin(slotFin)
                            .precioEfectivo(precioEfectivo)
                            .divisionesDisponibles(divisionesDisponibles)
                            .build());
                }

                cursor = slotFin;
                if (cursor.equals(limiteFin) || cursor.equals(LocalTime.MIDNIGHT)) break;
            }
        }

        return slotsDisponibles;
    }

    private BigDecimal resolverPrecioSlot(Cancha cancha, DiaSemana dia,
                                          LocalTime horaInicio, LocalTime horaFin) {
        List<PrecioHorario> precios = precioHorarioRepository
                .findPreciosParaSlot(cancha.getId(), dia, horaInicio, horaFin);
        return precios.isEmpty() ? cancha.getPrecioPorHora() : precios.get(0).getPrecio();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // VALIDACIONES (CORREGIDAS PARA MEDIANOCHE)
    // ─────────────────────────────────────────────────────────────────────────

    private void validarHorarios(LocalTime inicio, LocalTime fin) {
        // Permitir que el fin sea 00:00 (interpretado como el fin del día)
        if (!fin.equals(LocalTime.MIDNIGHT) && !fin.isAfter(inicio)) {
            throw new BadRequestException("La hora de fin debe ser posterior a la hora de inicio");
        }
    }

    private void validarDentroDeHorarioDisponible(Predio predio, LocalDate fecha,
                                                  LocalTime horaInicio, LocalTime horaFin) {
        DiaSemana dia = mapDayOfWeek(fecha.getDayOfWeek());
        List<HorarioDisponible> horarios =
                horarioRepository.findByPredioIdAndDiaSemana(predio.getId(), dia);

        if (horarios.isEmpty()) {
            throw new BadRequestException("El predio no está abierto el día: " + dia);
        }

        boolean dentroDeHorario = horarios.stream().anyMatch(h -> {
            boolean inicioOk = !horaInicio.isBefore(h.getHoraInicio());
            boolean finOk;

            if (h.getHoraFin().equals(LocalTime.MIDNIGHT)) {
                // Si el predio cierra a medianoche, cualquier horaFin es válida (ya que LocalTime.MAX es antes que medianoche)
                finOk = true;
            } else {
                // Si el predio NO cierra a medianoche, pero el usuario pide hasta la medianoche, está fuera de rango
                if (horaFin.equals(LocalTime.MIDNIGHT)) return false;
                finOk = !horaFin.isAfter(h.getHoraFin());
            }
            return inicioOk && finOk;
        });

        if (!dentroDeHorario) {
            throw new BadRequestException(
                    "El horario solicitado (" + horaInicio + " - " + horaFin
                            + ") está fuera del horario de apertura del predio");
        }
    }

    private boolean hayConflictoTemporal(LocalTime inicio1, LocalTime fin1,
                                         LocalTime inicio2, LocalTime fin2) {
        // Manejo de medianoche en conflictos: si fin es 00:00, lo tratamos como "después de cualquier inicio"
        boolean cruzado = inicio2.isBefore(fin1.equals(LocalTime.MIDNIGHT) ? LocalTime.MAX : fin1)
                && (fin2.equals(LocalTime.MIDNIGHT) ? LocalTime.MAX : fin2).isAfter(inicio1);
        return cruzado;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // EL RESTO SE MANTIENE IGUAL
    // ─────────────────────────────────────────────────────────────────────────

    private void validarDivisionParaTipoCancha(Cancha cancha, DivisionType division) {
        if (division == null || division == DivisionType.WHOLE) return;
        if (!cancha.getDivisionesDisponibles().contains(division)) {
            throw new BadRequestException(
                    "El tipo de división '" + division
                            + "' no es válido para una cancha de tipo " + cancha.getTipo());
        }
    }

    private void validarDiaAbierto(Predio predio, LocalDate fecha) {
        if (diaCerradoRepository.existsByPredioIdAndFecha(predio.getId(), fecha)) {
            throw new BadRequestException("El predio está cerrado el día: " + fecha);
        }
    }

    private void validarSinConflicto(Cancha cancha, LocalDate fecha, LocalTime horaInicio,
                                     LocalTime horaFin, DivisionType nuevaDivision,
                                     Long excludeReservaId) {
        List<Reserva> existentes =
                reservaRepository.findActivasByCanchaAndFecha(cancha.getId(), fecha);

        for (Reserva existente : existentes) {
            if (excludeReservaId != null && existente.getId().equals(excludeReservaId)) continue;
            if (!hayConflictoTemporal(existente.getHoraInicio(), existente.getHoraFin(),
                    horaInicio, horaFin)) continue;

            DivisionType existenteDivision = existente.getDivisionType();

            if (existenteDivision == DivisionType.WHOLE) {
                throw new ResourceConflictException(
                        "La cancha ya está reservada completa en ese horario ("
                                + existente.getHoraInicio() + " - " + existente.getHoraFin() + ")");
            }
            if (nuevaDivision == DivisionType.WHOLE) {
                throw new ResourceConflictException(
                        "No se puede reservar la cancha completa: ya existe una reserva de la división "
                                + existenteDivision + " en ese horario");
            }
            if (hayConflictoDivision(existenteDivision, nuevaDivision, cancha.getTipo())) {
                throw new ResourceConflictException(
                        "La división solicitada (" + nuevaDivision
                                + ") ya está ocupada en ese horario. División en conflicto: "
                                + existenteDivision);
            }
        }
    }

    private boolean hayConflictoDivision(DivisionType existente, DivisionType nueva,
                                         TipoCancha tipo) {
        if (existente == nueva) return true;
        if (tipo == TipoCancha.NUEVE) {
            Set<DivisionType> sieteCinco = Set.of(DivisionType.SIETE_CINCO_A, DivisionType.SIETE_CINCO_B);
            Set<DivisionType> tresCinco = Set.of(DivisionType.TRES_CINCO_A, DivisionType.TRES_CINCO_B, DivisionType.TRES_CINCO_C);
            if ((sieteCinco.contains(existente) && tresCinco.contains(nueva)) || (tresCinco.contains(existente) && sieteCinco.contains(nueva))) return true;
        }
        if (tipo == TipoCancha.SIETE) {
            if (existente == DivisionType.DOS_CINCO_A && nueva == DivisionType.DOS_CINCO_B) return false;
            if (existente == DivisionType.DOS_CINCO_B && nueva == DivisionType.DOS_CINCO_A) return false;
        }
        return false;
    }

    private List<DivisionType> calcularDivisionesDisponiblesParaSlot(
            Cancha cancha, LocalTime slotInicio, LocalTime slotFin,
            List<Reserva> reservasExistentes) {

        List<Reserva> reservasEnSlot = reservasExistentes.stream()
                .filter(r -> hayConflictoTemporal(r.getHoraInicio(), r.getHoraFin(),
                        slotInicio, slotFin))
                .collect(Collectors.toList());

        List<DivisionType> disponibles = new ArrayList<>(cancha.getDivisionesDisponibles());

        for (Reserva reserva : reservasEnSlot) {
            DivisionType ocupada = reserva.getDivisionType();
            if (ocupada == DivisionType.WHOLE) return List.of();
            disponibles.removeIf(d -> {
                if (d == DivisionType.WHOLE) return true;
                return hayConflictoDivision(ocupada, d, cancha.getTipo());
            });
        }

        return disponibles;
    }

    private DiaSemana mapDayOfWeek(DayOfWeek dow) {
        return switch (dow) {
            case MONDAY    -> DiaSemana.LUNES;
            case TUESDAY   -> DiaSemana.MARTES;
            case WEDNESDAY -> DiaSemana.MIERCOLES;
            case THURSDAY  -> DiaSemana.JUEVES;
            case FRIDAY    -> DiaSemana.VIERNES;
            case SATURDAY  -> DiaSemana.SABADO;
            case SUNDAY    -> DiaSemana.DOMINGO;
        };
    }

    private Cancha findCancha(Long id) {
        return canchaRepository.findByIdWithPredio(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cancha no encontrada: " + id));
    }

    private Usuario findUsuario(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + email));
    }

    private void enviarEmailConfirmacion(Reserva reserva) {
        try {
            if (reserva.getCliente() == null) return;
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