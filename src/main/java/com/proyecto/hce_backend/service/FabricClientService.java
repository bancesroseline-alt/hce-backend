package com.proyecto.hce_backend.service;

import com.proyecto.hce_backend.dto.FabricTraceResultDTO;
import com.proyecto.hce_backend.dto.TraceabilityRegisterRequestDTO;
import com.proyecto.hce_backend.model.LedgerBloque;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Punto unico de acceso al ledger de evidencias.
 *
 * - fabric.enabled=false (por defecto): usa el ledger SIMULADO (LedgerService), con las mismas
 *   operaciones del contrato hce-traceability.
 * - fabric.enabled=true: aqui se conectara Fabric Gateway Client. Mientras no exista esa
 *   integracion, devuelve un error explicito en lugar de aparentar un registro.
 */
@Service
public class FabricClientService {

    private final LedgerService ledgerService;

    @Value("${fabric.enabled:false}")
    private boolean fabricEnabled;

    @Value("${fabric.channel:hce-channel}")
    private String channel;

    @Value("${fabric.chaincode:hce-traceability}")
    private String chaincode;

    public FabricClientService(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    public FabricTraceResultDTO registrarTrazabilidad(TraceabilityRegisterRequestDTO request) {
        if (!fabricEnabled) {
            LedgerBloque bloque = ledgerService.registerEvidence(
                    request.getEntityType(),
                    request.getEntityId(),
                    request.getHash(),
                    request.getAction(),
                    request.getUserId(),
                    request.getOperationId()
            );

            return new FabricTraceResultDTO(
                    true,
                    false,
                    bloque.getTxId(),
                    bloque.getCanal(),
                    bloque.getContrato(),
                    null,
                    bloque.getVersion(),
                    bloque.getNumeroBloque(),
                    LedgerService.MODO
            );
        }

        // Aqui se invocara registerEvidence() mediante Fabric Gateway Client.
        return new FabricTraceResultDTO(
                false,
                false,
                null,
                channel,
                chaincode,
                "Cliente Hyperledger Fabric pendiente de configurar",
                null,
                null,
                "FABRIC"
        );
    }

    public Optional<String> consultarHashRegistrado(String entityType, String entityId) {
        if (!fabricEnabled) {
            return ledgerService.getEvidence(entityType, entityId).map(LedgerBloque::getHash);
        }

        // Aqui se invocara getEvidence() mediante Fabric Gateway Client.
        return Optional.empty();
    }
}
