package com.proyecto.hce_backend.service;

import com.proyecto.hce_backend.model.AtencionMedica;
import com.proyecto.hce_backend.model.Cita;
import com.proyecto.hce_backend.model.Paciente;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;

@Service
public class HashService {

    public String generarHashEntidad(String entityType, Object entity) {
        String tipo = normalizarTipoEntidad(entityType);

        if ("ATENCION".equals(tipo) || "HISTORIA_CLINICA".equals(tipo)) {
            return generarHashAtencion((AtencionMedica) entity);
        }

        if ("PACIENTE".equals(tipo)) {
            return generarHashPaciente((Paciente) entity);
        }

        if ("CITA".equals(tipo)) {
            return generarHashCita((Cita) entity);
        }

        throw new RuntimeException("Tipo de entidad no soportado para hash blockchain");
    }

    public String generarHashAtencion(AtencionMedica atencion) {
        String contenidoNormalizado = String.join("|",
                valor(atencion.getId()),
                valor(atencion.getPaciente() != null ? atencion.getPaciente().getId() : null),
                valor(atencion.getUsuario() != null ? atencion.getUsuario().getId() : null),
                "ATENCION",
                valor(atencion.getFechaHora()),
                valor(atencion.getTipoAtencion()),
                valor(atencion.getMotivoConsulta()),
                valor(atencion.getPresionArterial()),
                valor(atencion.getTemperatura()),
                valor(atencion.getSaturacion()),
                valor(atencion.getTalla()),
                valor(atencion.getPeso()),
                valor(atencion.getDiagnostico()),
                valor(atencion.getObservaciones()),
                valor(atencion.getTratamientoIndicado()),
                valor(atencion.getMedicamentos()),
                valor(atencion.getEstado())
        );

        return sha256(contenidoNormalizado);
    }

    public String generarHashPaciente(Paciente paciente) {
        String contenidoNormalizado = String.join("|",
                valor(paciente.getId()),
                "PACIENTE",
                valor(paciente.getTipoDocumento()),
                valor(paciente.getNumeroDocumento()),
                valor(paciente.getNombres()),
                valor(paciente.getApellidos()),
                valor(paciente.getFechaNacimiento()),
                valor(paciente.getSexo()),
                valor(paciente.getEstadoCivil()),
                valor(paciente.getTelefono()),
                valor(paciente.getDireccion()),
                valor(paciente.getGrupoSanguineo()),
                valor(paciente.getAntecedentes()),
                valor(paciente.getEstado())
        );

        return sha256(contenidoNormalizado);
    }

    public String generarHashCita(Cita cita) {
        String contenidoNormalizado = String.join("|",
                valor(cita.getId()),
                valor(cita.getPaciente() != null ? cita.getPaciente().getId() : null),
                valor(cita.getMedico() != null ? cita.getMedico().getId() : null),
                "CITA",
                valor(cita.getTipoCita()),
                valor(cita.getFecha()),
                valor(cita.getHora()),
                valor(cita.getEspecialidad()),
                valor(cita.getMotivoConsulta()),
                valor(cita.getEstado())
        );

        return sha256(contenidoNormalizado);
    }

    public String normalizarTipoEntidad(String entityType) {
        return valor(entityType).replace(" ", "_").toUpperCase(Locale.ROOT);
    }

    private String sha256(String contenidoNormalizado) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(contenidoNormalizado.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);

                if (hex.length() == 1) {
                    hexString.append('0');
                }

                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar hash blockchain");
        }
    }

    private String valor(Object value) {
        return value == null ? "" : value.toString().trim();
    }
}
