package com.proyecto.hce_backend.service;

import com.proyecto.hce_backend.dto.LedgerEstadoDTO;
import com.proyecto.hce_backend.model.LedgerBloque;
import com.proyecto.hce_backend.model.TrazabilidadBlockchain;
import com.proyecto.hce_backend.repository.LedgerBloqueRepository;
import com.proyecto.hce_backend.repository.TrazabilidadBlockchainRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Ledger de evidencias en MODO SIMULADO.
 *
 * Reproduce el contrato "hce-traceability" (registerEvidence, getEvidence, getHistory) con las mismas
 * garantias logicas que se esperan de Hyperledger Fabric: bloques de solo anexado y encadenados por hash,
 * una evidencia independiente por entidad y version, idempotencia por operationId y endoso de las
 * organizaciones Healthcare y Audit. NO es una red Fabric: cuando se conecte una real, solo cambia
 * la implementacion que usa FabricClientService.
 */
@Service
public class LedgerService {

    public static final String MODO = "SIMULADO";
    public static final String ENDOSOS = "HealthcareMSP.peer,AuditMSP.peer";
    private static final String GENESIS = "0000000000000000000000000000000000000000000000000000000000000000";

    private final LedgerBloqueRepository ledgerRepository;
    private final TrazabilidadBlockchainRepository trazabilidadRepository;

    @Value("${fabric.channel:hce-channel}")
    private String canal;

    @Value("${fabric.chaincode:hce-traceability}")
    private String contrato;

    public LedgerService(LedgerBloqueRepository ledgerRepository,
                         TrazabilidadBlockchainRepository trazabilidadRepository) {
        this.ledgerRepository = ledgerRepository;
        this.trazabilidadRepository = trazabilidadRepository;
    }

    /** registerEvidence(): agrega una evidencia. Una solicitud equivalente devuelve la ya registrada. */
    public synchronized LedgerBloque registerEvidence(String entityType,
                                                      String entityId,
                                                      String hash,
                                                      String action,
                                                      String actorId,
                                                      String operationId) {
        return agregar(entityType, entityId, hash, action, actorId, operationId,
                LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS));
    }

    public Optional<LedgerBloque> getEvidence(String entityType, String entityId) {
        return ledgerRepository.findTopByEntityTypeAndEntityIdOrderByVersionDesc(entityType, entityId);
    }

    public Optional<LedgerBloque> getEvidence(String entityType, String entityId, Integer version) {
        return ledgerRepository.findByEntityTypeAndEntityIdAndVersion(entityType, entityId, version);
    }

    public List<LedgerBloque> getHistory(String entityType, String entityId) {
        return ledgerRepository.findByEntityTypeAndEntityIdOrderByVersionAsc(entityType, entityId);
    }

    public long contarEvidencias(String entityType, String entityId) {
        return ledgerRepository.countByEntityTypeAndEntityId(entityType, entityId);
    }

    /** Evidencias por registro de un tipo, en una sola consulta. */
    public Map<String, Long> contarEvidenciasPorEntidad(String entityType) {
        Map<String, Long> conteos = new java.util.HashMap<>();

        for (Object[] fila : ledgerRepository.contarPorEntidad(entityType)) {
            conteos.put(String.valueOf(fila[0]), ((Number) fila[1]).longValue());
        }

        return conteos;
    }

    /**
     * Validez de unos bloques concretos (su propio hash y su enlace con el bloque anterior),
     * sin recorrer toda la cadena: barato para mostrar el historial de un registro.
     */
    public Map<Long, Boolean> validezDeBloques(List<LedgerBloque> bloques) {
        Map<Long, Boolean> resultado = new LinkedHashMap<>();

        for (LedgerBloque bloque : bloques) {
            String hashAnteriorEsperado = ledgerRepository
                    .findTopByNumeroBloqueLessThanOrderByNumeroBloqueDesc(bloque.getNumeroBloque())
                    .map(LedgerBloque::getHashBloque)
                    .orElse(GENESIS);

            boolean enlaceValido = hashAnteriorEsperado.equals(bloque.getHashBloqueAnterior());
            boolean hashValido = calcularHashBloque(bloque).equals(bloque.getHashBloque());
            resultado.put(bloque.getNumeroBloque(), enlaceValido && hashValido);
        }

        return resultado;
    }

    /** Recalcula toda la cadena. Clave: numero de bloque; valor: si el bloque y su enlace son consistentes. */
    public Map<Long, Boolean> validezPorBloque() {
        Map<Long, Boolean> resultado = new LinkedHashMap<>();
        String hashAnterior = GENESIS;

        for (LedgerBloque bloque : ledgerRepository.findAllByOrderByNumeroBloqueAsc()) {
            boolean enlaceValido = hashAnterior.equals(bloque.getHashBloqueAnterior());
            boolean hashValido = calcularHashBloque(bloque).equals(bloque.getHashBloque());
            resultado.put(bloque.getNumeroBloque(), enlaceValido && hashValido);
            hashAnterior = bloque.getHashBloque();
        }

        return resultado;
    }

    public Optional<Long> primerBloqueInvalido() {
        return validezPorBloque().entrySet().stream()
                .filter(entrada -> !entrada.getValue())
                .map(Map.Entry::getKey)
                .findFirst();
    }

    public LedgerEstadoDTO estado() {
        Optional<Long> invalido = primerBloqueInvalido();
        Optional<LedgerBloque> ultimo = ledgerRepository.findTopByOrderByNumeroBloqueDesc();

        LedgerEstadoDTO estado = new LedgerEstadoDTO();
        estado.setModo(MODO);
        estado.setCanal(canal);
        estado.setContrato(contrato);
        estado.setOrganizaciones(List.of("Healthcare", "Audit"));
        estado.setOrderers("3 nodos Raft");
        estado.setPoliticaEndoso("AND('HealthcareMSP.peer','AuditMSP.peer')");
        estado.setTotalBloques(ledgerRepository.count());
        estado.setCadenaIntegra(invalido.isEmpty());
        estado.setPrimerBloqueInvalido(invalido.orElse(null));
        estado.setUltimoBloque(ultimo.map(LedgerBloque::getNumeroBloque).orElse(null));
        estado.setUltimoHash(ultimo.map(LedgerBloque::getHashBloque).orElse(null));
        return estado;
    }

    /**
     * Al iniciar, las evidencias que ya existian en la tabla de trazabilidad (anteriores al ledger)
     * se incorporan en orden cronologico para que su historial tambien sea verificable.
     */
    @EventListener(ApplicationReadyEvent.class)
    public synchronized void incorporarEvidenciasPrevias() {
        try {
            if (ledgerRepository.count() > 0) {
                return;
            }

            List<TrazabilidadBlockchain> previas = new ArrayList<>(trazabilidadRepository.findAllByOrderByFechaRegistroDesc());
            java.util.Collections.reverse(previas);

            for (TrazabilidadBlockchain previa : previas) {
                if (previa.getHashRegistro() == null || previa.getHashRegistro().length() != 64
                        || previa.getEntityType() == null || previa.getEntityId() == null) {
                    continue;
                }

                LedgerBloque bloque = agregar(
                        previa.getEntityType(),
                        previa.getEntityId(),
                        previa.getHashRegistro(),
                        previa.getAction() != null ? previa.getAction() : previa.getTipoOperacion(),
                        previa.getUserId(),
                        "LEGACY-" + previa.getId(),
                        previa.getFechaRegistro().truncatedTo(ChronoUnit.MILLIS)
                );

                previa.setFabricTransactionId(bloque.getTxId());
                previa.setFabricChannel(bloque.getCanal());
                previa.setFabricChaincode(bloque.getContrato());
                previa.setRegistradoEnFabric(false);
                previa.setErrorFabric(null);
                previa.setIntegridadValida(true);
                if ("ERROR".equals(previa.getStatus())) {
                    previa.setStatus("REGISTRADO");
                }
                trazabilidadRepository.save(previa);
            }
        } catch (Exception e) {
            // La incorporacion es una ayuda: un fallo aqui no debe impedir que el backend arranque.
            System.err.println("No se pudieron incorporar las evidencias previas al ledger: " + e.getMessage());
        }
    }

    private LedgerBloque agregar(String entityType,
                                 String entityId,
                                 String hash,
                                 String action,
                                 String actorId,
                                 String operationId,
                                 LocalDateTime fecha) {
        if (entityType == null || entityId == null || hash == null || action == null) {
            throw new RuntimeException("La evidencia requiere entidad, id, hash y accion");
        }

        String idOperacion = operationId == null || operationId.isBlank() ? UUID.randomUUID().toString() : operationId;

        Optional<LedgerBloque> existente = ledgerRepository.findByOperationId(idOperacion);
        if (existente.isPresent()) {
            LedgerBloque previo = existente.get();
            boolean equivalente = previo.getEntityType().equals(entityType)
                    && previo.getEntityId().equals(entityId)
                    && previo.getHash().equals(hash);

            if (equivalente) {
                return previo;
            }

            throw new RuntimeException("El identificador de operacion ya se uso con un contenido diferente");
        }

        Optional<LedgerBloque> ultimoDeEntidad = ledgerRepository
                .findTopByEntityTypeAndEntityIdOrderByVersionDesc(entityType, entityId);
        Optional<LedgerBloque> ultimoDelLedger = ledgerRepository.findTopByOrderByNumeroBloqueDesc();

        LedgerBloque bloque = new LedgerBloque();
        bloque.setTxId(sha256(UUID.randomUUID() + "|" + fecha));
        bloque.setOperationId(idOperacion);
        bloque.setCanal(canal);
        bloque.setContrato(contrato);
        bloque.setEntityType(entityType);
        bloque.setEntityId(entityId);
        bloque.setVersion(ultimoDeEntidad.map(b -> b.getVersion() + 1).orElse(1));
        bloque.setHash(hash);
        bloque.setAction(action);
        bloque.setActorId(actorId);
        bloque.setEvidenciaPreviaTx(ultimoDeEntidad.map(LedgerBloque::getTxId).orElse(null));
        bloque.setFecha(fecha);
        bloque.setEndosos(ENDOSOS);
        bloque.setHashBloqueAnterior(ultimoDelLedger.map(LedgerBloque::getHashBloque).orElse(GENESIS));
        bloque.setHashBloque(calcularHashBloque(bloque));

        return ledgerRepository.save(bloque);
    }

    private String calcularHashBloque(LedgerBloque b) {
        return sha256(String.join("|",
                texto(b.getHashBloqueAnterior()),
                texto(b.getTxId()),
                texto(b.getOperationId()),
                texto(b.getEntityType()),
                texto(b.getEntityId()),
                texto(b.getVersion()),
                texto(b.getHash()),
                texto(b.getAction()),
                texto(b.getActorId()),
                texto(b.getEvidenciaPreviaTx()),
                texto(b.getFecha()),
                texto(b.getEndosos())
        ));
    }

    private String texto(Object valor) {
        return valor == null ? "" : valor.toString();
    }

    private String sha256(String contenido) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(contenido.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();

            for (byte b : bytes) {
                hex.append(String.format("%02x", b));
            }

            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error al calcular el hash del ledger");
        }
    }
}
