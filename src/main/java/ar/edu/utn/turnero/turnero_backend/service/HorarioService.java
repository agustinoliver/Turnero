package ar.edu.utn.turnero.turnero_backend.service;

import ar.edu.utn.turnero.turnero_backend.dto.request.DiaCerradoRequest;
import ar.edu.utn.turnero.turnero_backend.dto.request.HorarioDisponibleRequest;
import ar.edu.utn.turnero.turnero_backend.dto.response.DiaCerradoResponse;
import ar.edu.utn.turnero.turnero_backend.dto.response.HorarioDisponibleResponse;

import java.util.List;

public interface HorarioService {

    List<HorarioDisponibleResponse> configurarHorarios(
            List<HorarioDisponibleRequest> request, String emailDueno);

    HorarioDisponibleResponse agregarHorario(HorarioDisponibleRequest request, String emailDueno);

    void eliminarHorario(Long horarioId, String emailDueno);

    List<HorarioDisponibleResponse> listarHorariosPorPredio(Long predioId);

    DiaCerradoResponse agregarDiaCerrado(DiaCerradoRequest request, String emailDueno);

    void eliminarDiaCerrado(Long diaCerradoId, String emailDueno);

    List<DiaCerradoResponse> listarDiasCerradosPorPredio(Long predioId);
}
