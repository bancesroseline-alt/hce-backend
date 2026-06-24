package com.proyecto.hce_backend.service;

import com.proyecto.hce_backend.dto.FabricTraceResultDTO;
import com.proyecto.hce_backend.dto.TraceabilityRegisterRequestDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class FabricClientService {

    @Value("${fabric.enabled:false}")
    private boolean fabricEnabled;

    @Value("${fabric.channel:hce-channel}")
    private String channel;

    @Value("${fabric.chaincode:hce-traceability}")
    private String chaincode;

    public FabricTraceResultDTO registrarTrazabilidad(TraceabilityRegisterRequestDTO request) {
        if (!fabricEnabled) {
            return new FabricTraceResultDTO(
                    true,
                    false,
                    "LOCAL-" + UUID.randomUUID(),
                    channel,
                    chaincode,
                    "Fabric deshabilitado. Registro guardado localmente para reemplazar por SDK real."
            );
        }

        // Punto unico para integrar el SDK de Hyperledger Fabric.
        // Aqui se invocaria el chaincode con id, tipo, hash, fecha, usuario, accion y estado.
        return new FabricTraceResultDTO(
                false,
                false,
                null,
                channel,
                chaincode,
                "Cliente Hyperledger Fabric pendiente de configurar"
        );
    }

    public Optional<String> consultarHashRegistrado(String entityType, String entityId) {
        if (!fabricEnabled) {
            return Optional.empty();
        }

        // Punto unico para consultar el hash registrado en Fabric por tipo de entidad e id.
        return Optional.empty();
    }
}
