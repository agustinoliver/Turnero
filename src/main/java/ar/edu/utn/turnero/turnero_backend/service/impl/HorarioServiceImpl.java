package ar.edu.utn.turnero.turnero_backend.service.impl;

import ar.edu.utn.turnero.turnero_backend.dto.request.DiaCerradoRequest;
import ar.edu.utn.turnero.turnero_backend.dto.request.HorarioDisponibleRequest;
import ar.edu.utn.turnero.turnero_backend.dto.response.DiaCerradoResponse;
import ar.edu.utn.turnero.turnero_backend.dto.response.HorarioDisponibleResponse;
import ar.edu.utn.turnero.turnero_backend.entity.DiaCerrado;
import ar.edu.utn.turnero.turnero_backend.entity.HorarioDisponible;
import ar.edu.utn.turnero.turnero_backend.entity.Predio;
import ar.edu.utn.turnero.turnero_backend.exception.BadRequestException;
import ar.edu.utn.turnero.turnero_backend.exception.ResourceConflictException;
import ar.edu.utn.turnero.turnero_backend.exception.ResourceNotFoundException;
import ar.edu.utn.turnero.turnero_backend.repository.DiaCerradoRepository;
import ar.edu.utn.turnero.turnero_backend.repository.HorarioDisponibleRepository;
import ar.edu.utn.turnero.turnero_backend.service.HorarioService;
import ar.edu.utn.turnero.turnero_backend.service.MapperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HorarioServiceImpl implements HorarioService {

    private final HorarioDisponibleRepository horarioRepository;
    private final DiaCerradoRepository diaCerradoRepository;
    private final PredioServiceImpl predioService;
    private final MapperService mapper;

    @Override
    @Transactional
    public List<HorarioDisponibleResponse> configurarHorarios(
            List<HorarioDisponibleRequest> request, String emailDueno) {
        Predio predio = predioService.findPredioByDueno(emailDueno);

        // Reemplazar todos los horarios del predio
        horarioRepository.deleteByPredioId(predio.getId());

        List<HorarioDisponible> horarios = request.stream()
                .map(r -> {
                    if (r.getHoraFin().isBefore(r.getHoraInicio()) || r.getHoraFin().equals(r.getHoraInicio())) {
                        throw new BadRequestException("La hora de fin debe ser posterior a la de inicio");
                    }
                    return HorarioDisponible.builder()
                            .diaSemana(r.getDiaSemana())
                            .horaInicio(r.getHoraInicio())
                            .horaFin(r.getHoraFin())
                            .predio(predio)
                            .build();
                })
                .collect(Collectors.toList());

        return horarioRepository.saveAll(horarios).stream()
                .map(mapper::toHorarioResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public HorarioDisponibleResponse agregarHorario(HorarioDisponibleRequest request, String emailDueno) {
        Predio predio = predioService.findPredioByDueno(emailDueno);

        if (request.getHoraFin().isBefore(request.getHoraInicio()) || request.getHoraFin().equals(request.getHoraInicio())) {
            throw new BadRequestException("La hora de fin debe ser posterior a la de inicio");
        }

        HorarioDisponible horario = HorarioDisponible.builder()
                .diaSemana(request.getDiaSemana())
                .horaInicio(request.getHoraInicio())
                .horaFin(request.getHoraFin())
                .predio(predio)
                .build();

        return mapper.toHorarioResponse(horarioRepository.save(horario));
    }

    @Override
    @Transactional
    public void eliminarHorario(Long horarioId, String emailDueno) {
        HorarioDisponible horario = horarioRepository.findById(horarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Horario no encontrado: " + horarioId));
        if (!horario.getPredio().getDueno().getEmail().equals(emailDueno)) {
            throw new BadRequestException("No tenés permisos sobre este horario");
        }
        horarioRepository.delete(horario);
    }

    @Override
    public List<HorarioDisponibleResponse> listarHorariosPorPredio(Long predioId) {
        return horarioRepository.findByPredioId(predioId).stream()
                .map(mapper::toHorarioResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DiaCerradoResponse agregarDiaCerrado(DiaCerradoRequest request, String emailDueno) {
        Predio predio = predioService.findPredioByDueno(emailDueno);

        if (diaCerradoRepository.existsByPredioIdAndFecha(predio.getId(), request.getFecha())) {
            throw new ResourceConflictException("Ya existe un día cerrado para la fecha: " + request.getFecha());
        }

        DiaCerrado diaCerrado = DiaCerrado.builder()
                .fecha(request.getFecha())
                .motivo(request.getMotivo())
                .predio(predio)
                .build();

        return mapper.toDiaCerradoResponse(diaCerradoRepository.save(diaCerrado));
    }

    @Override
    @Transactional
    public void eliminarDiaCerrado(Long diaCerradoId, String emailDueno) {
        DiaCerrado diaCerrado = diaCerradoRepository.findById(diaCerradoId)
                .orElseThrow(() -> new ResourceNotFoundException("Día cerrado no encontrado: " + diaCerradoId));
        if (!diaCerrado.getPredio().getDueno().getEmail().equals(emailDueno)) {
            throw new BadRequestException("No tenés permisos sobre este día cerrado");
        }
        diaCerradoRepository.delete(diaCerrado);
    }

    @Override
    public List<DiaCerradoResponse> listarDiasCerradosPorPredio(Long predioId) {
        return diaCerradoRepository.findByPredioId(predioId).stream()
                .map(mapper::toDiaCerradoResponse).collect(Collectors.toList());
    }
}
