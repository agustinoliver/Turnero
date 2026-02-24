package ar.edu.utn.turnero.turnero_backend.service;

import ar.edu.utn.turnero.turnero_backend.dto.request.PredioRequest;
import ar.edu.utn.turnero.turnero_backend.dto.response.PredioResponse;

import java.util.List;

public interface PredioService {

    PredioResponse crearPredio(PredioRequest request, String emailDueno);

    PredioResponse actualizarPredio(Long id, PredioRequest request, String emailDueno);

    PredioResponse obtenerPorId(Long id);

    PredioResponse obtenerPorDueno(String emailDueno);

    List<PredioResponse> listarTodos();

    List<PredioResponse> listarActivos();
}
