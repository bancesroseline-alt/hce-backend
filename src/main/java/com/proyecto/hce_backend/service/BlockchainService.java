package com.proyecto.hce_backend.service;

import com.proyecto.hce_backend.model.AtencionMedica;
import com.proyecto.hce_backend.model.TrazabilidadBlockchain;
import com.proyecto.hce_backend.repository.TrazabilidadBlockchainRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BlockchainService {

    final TrazabilidadBlockchainRepository trazabilidadRepository;

    public BlockchainService(TrazabilidadBlockchainRepository trazabilidadRepository) {
        this.trazabilidadRepository = trazabilidadRepository;
    }

    public String generarHashAtencion(AtencionMedica atencion) {
        try {
            String data = atencion.getId() + "|" +
                    atencion.getPaciente().getId() + "|" +
                    atencion.getUsuario().getId() + "|" +
                    atencion.getFechaHora() + "|" +
                    atencion.getTipoAtencion() + "|" +
                    atencion.getMotivoConsulta() + "|" +
                    atencion.getDiagnostico() + "|" +
                    atencion.getTratamientoIndicado();

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));

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

    public TrazabilidadBlockchain registrarTrazabilidad(AtencionMedica atencion, String tipoOperacion) {

        String hash = generarHashAtencion(atencion);

        TrazabilidadBlockchain trazabilidad = new TrazabilidadBlockchain();
        trazabilidad.setAtencionId(atencion.getId());
        trazabilidad.setUsuarioId(atencion.getUsuario().getId());
        trazabilidad.setTipoOperacion(tipoOperacion);
        trazabilidad.setHashRegistro(hash);
        trazabilidad.setHashAnterior(atencion.getHashBlockchain());
        trazabilidad.setFechaRegistro(LocalDateTime.now());
        trazabilidad.setIntegridadValida(true);

        return trazabilidadRepository.save(trazabilidad);
    }

    public Boolean validarIntegridad(AtencionMedica atencion) {
        String hashActual = generarHashAtencion(atencion);
        return hashActual.equals(atencion.getHashBlockchain());
    }

    public List<TrazabilidadBlockchain> historialPorAtencion(Long atencionId) {
        return trazabilidadRepository.findByAtencionId(atencionId);
    }
}