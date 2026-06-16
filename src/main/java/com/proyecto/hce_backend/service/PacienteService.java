package com.proyecto.hce_backend.service;

import com.proyecto.hce_backend.dto.PacienteDTO;
import com.proyecto.hce_backend.model.Paciente;
import com.proyecto.hce_backend.repository.PacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PacienteService {

    @Autowired
    private PacienteRepository pacienteRepository;

    public PacienteDTO convertirAPacienteDTO(Paciente p) {
        return new PacienteDTO(
                p.getId(),
                p.getTipoDocumento(),
                p.getNumeroDocumento(),
                p.getNombres(),
                p.getApellidos(),
                p.getFechaNacimiento().toString(),
                p.getEdad(),
                p.getSexo(),
                p.getEstadoCivil(),
                p.getTelefono(),
                p.getDireccion(),
                p.getAntecedentes(),
                p.getEstado(),
                p.getUuidLocal()
        );
    }

    public PacienteDTO registrarPaciente(Paciente paciente) {

        // VALIDAR DOCUMENTO DUPLICADO
        if (pacienteRepository.existsByNumeroDocumento(
                paciente.getNumeroDocumento())) {

            throw new RuntimeException(
                    "El número de documento ya está registrado"
            );
        }

        // VALIDACIONES
        validarDocumentoPaciente(paciente);

        // GUARDAR
        Paciente guardado = pacienteRepository.save(paciente);

        // RETORNAR DTO
        return convertirAPacienteDTO(guardado);
    }

    public List<PacienteDTO> listarPacientes() {
        return pacienteRepository.findAll()
                .stream()
                .map(this::convertirAPacienteDTO)
                .toList();
    }

    public PacienteDTO obtenerPacientePorId(Long id) {
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        return convertirAPacienteDTO(paciente);
    }

    public List<PacienteDTO> buscarPacientes(String criterio) {
        return pacienteRepository
                .findByNombresContainingIgnoreCaseOrApellidosContainingIgnoreCaseOrNumeroDocumentoContainingIgnoreCase(
                        criterio, criterio, criterio
                )
                .stream()
                .map(this::convertirAPacienteDTO)
                .toList();
    }

    public PacienteDTO editarPaciente(Long id, Paciente pacienteActualizado) {
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        validarDocumentoPaciente(pacienteActualizado);

        paciente.setTipoDocumento(pacienteActualizado.getTipoDocumento());
        paciente.setNumeroDocumento(pacienteActualizado.getNumeroDocumento());
        paciente.setNombres(pacienteActualizado.getNombres());
        paciente.setApellidos(pacienteActualizado.getApellidos());
        paciente.setFechaNacimiento(pacienteActualizado.getFechaNacimiento());
        paciente.setSexo(pacienteActualizado.getSexo());
        paciente.setEstadoCivil(pacienteActualizado.getEstadoCivil());
        paciente.setTelefono(pacienteActualizado.getTelefono());
        paciente.setDireccion(pacienteActualizado.getDireccion());
        paciente.setGrupoSanguineo(pacienteActualizado.getGrupoSanguineo());
        paciente.setAntecedentes(pacienteActualizado.getAntecedentes());
        paciente.setEstado(pacienteActualizado.getEstado());

        return convertirAPacienteDTO(pacienteRepository.save(paciente));
    }

    public PacienteDTO darDeBajaPaciente(Long id) {
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        paciente.setEstado(false);

        return convertirAPacienteDTO(pacienteRepository.save(paciente));
    }

    private void validarDocumentoPaciente(Paciente paciente) {
        if (paciente.getTipoDocumento() == null) {
            throw new RuntimeException("El tipo de documento es obligatorio");
        }

        if (paciente.getNumeroDocumento() == null || paciente.getNumeroDocumento().isBlank()) {
            throw new RuntimeException("El número de documento es obligatorio");
        }

        switch (paciente.getTipoDocumento()) {
            case DNI -> {
                if (!paciente.getNumeroDocumento().matches("\\d{8}")) {
                    throw new RuntimeException("El DNI debe tener 8 dígitos numéricos");
                }
            }
            case CARNET_EXTRANJERIA -> {
                if (!paciente.getNumeroDocumento().matches("[A-Za-z0-9]{9,12}")) {
                    throw new RuntimeException("El carnet de extranjería debe tener entre 9 y 12 caracteres alfanuméricos");
                }
            }
        }
    }

    public Long totalPacientes() {
        return pacienteRepository.count();
    }

}
