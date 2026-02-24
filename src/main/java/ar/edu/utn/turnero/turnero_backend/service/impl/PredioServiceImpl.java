package ar.edu.utn.turnero.turnero_backend.service.impl;

import ar.edu.utn.turnero.turnero_backend.dto.request.PredioRequest;
import ar.edu.utn.turnero.turnero_backend.dto.response.PredioResponse;
import ar.edu.utn.turnero.turnero_backend.entity.Predio;
import ar.edu.utn.turnero.turnero_backend.entity.Usuario;
import ar.edu.utn.turnero.turnero_backend.exception.BadRequestException;
import ar.edu.utn.turnero.turnero_backend.exception.ResourceConflictException;
import ar.edu.utn.turnero.turnero_backend.exception.ResourceNotFoundException;
import ar.edu.utn.turnero.turnero_backend.repository.PredioRepository;
import ar.edu.utn.turnero.turnero_backend.repository.UsuarioRepository;
import ar.edu.utn.turnero.turnero_backend.service.MapperService;
import ar.edu.utn.turnero.turnero_backend.service.PredioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PredioServiceImpl implements PredioService {

    private final PredioRepository predioRepository;
    private final UsuarioRepository usuarioRepository;
    private final MapperService mapper;

    @Override
    @Transactional
    public PredioResponse crearPredio(PredioRequest request, String emailDueno) {
        Usuario dueno = findDueno(emailDueno);
        if (predioRepository.findByDuenoId(dueno.getId()).isPresent()) {
            throw new ResourceConflictException("El dueño ya tiene un predio registrado");
        }
        Predio predio = Predio.builder()
                .nombre(request.getNombre())
                .direccion(request.getDireccion())
                .telefono(request.getTelefono())
                .descripcion(request.getDescripcion())
                .dueno(dueno)
                .activo(true)
                .build();
        return mapper.toPredioResponse(predioRepository.save(predio));
    }

    @Override
    @Transactional
    public PredioResponse actualizarPredio(Long id, PredioRequest request, String emailDueno) {
        Predio predio = findByIdAndVerifyDueno(id, emailDueno);
        predio.setNombre(request.getNombre());
        predio.setDireccion(request.getDireccion());
        predio.setTelefono(request.getTelefono());
        predio.setDescripcion(request.getDescripcion());
        return mapper.toPredioResponse(predioRepository.save(predio));
    }

    @Override
    public PredioResponse obtenerPorId(Long id) {
        return mapper.toPredioResponse(predioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Predio no encontrado: " + id)));
    }

    @Override
    public PredioResponse obtenerPorDueno(String emailDueno) {
        return mapper.toPredioResponse(predioRepository.findByDuenoEmail(emailDueno)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe predio para el dueño: " + emailDueno)));
    }

    @Override
    public List<PredioResponse> listarTodos() {
        return predioRepository.findAll().stream().map(mapper::toPredioResponse).collect(Collectors.toList());
    }

    @Override
    public List<PredioResponse> listarActivos() {
        return predioRepository.findByActivo(true).stream().map(mapper::toPredioResponse).collect(Collectors.toList());
    }

    Predio findByIdAndVerifyDueno(Long id, String emailDueno) {
        Predio predio = predioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Predio no encontrado: " + id));
        if (!predio.getDueno().getEmail().equals(emailDueno)) {
            throw new BadRequestException("No tenés permisos sobre este predio");
        }
        return predio;
    }

    Predio findPredioByDueno(String emailDueno) {
        return predioRepository.findByDuenoEmail(emailDueno)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe predio para el dueño: " + emailDueno));
    }

    private Usuario findDueno(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Dueño no encontrado: " + email));
    }
}
