package ar.edu.utn.turnero.turnero_backend.service;

import ar.edu.utn.turnero.turnero_backend.dto.request.CanchaRequest;
import ar.edu.utn.turnero.turnero_backend.dto.response.CanchaResponse;
import ar.edu.utn.turnero.turnero_backend.enums.TipoCancha;

import java.util.List;

public interface CanchaService {

    CanchaResponse crearCancha(CanchaRequest request, String emailDueno);

    CanchaResponse actualizarCancha(Long canchaId, CanchaRequest request, String emailDueno);

    void eliminarCancha(Long canchaId, String emailDueno);

    CanchaResponse obtenerPorId(Long id);

    List<CanchaResponse> listarPorPredio(Long predioId);

    List<CanchaResponse> listarPorPredioYTipo(Long predioId, TipoCancha tipo);

    List<CanchaResponse> listarActivasPorPredio(Long predioId);
}
