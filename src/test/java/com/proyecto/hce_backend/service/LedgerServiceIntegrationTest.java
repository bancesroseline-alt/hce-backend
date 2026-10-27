package com.proyecto.hce_backend.service;

import com.proyecto.hce_backend.dto.CampoResumenDTO;
import com.proyecto.hce_backend.dto.LedgerBloqueDTO;
import com.proyecto.hce_backend.dto.TraceabilityVerifyRequestDTO;
import com.proyecto.hce_backend.dto.TraceabilityVerifyResponseDTO;
import com.proyecto.hce_backend.model.EstadoCivil;
import com.proyecto.hce_backend.model.LedgerBloque;
import com.proyecto.hce_backend.model.Paciente;
import com.proyecto.hce_backend.model.TipoDocumento;
import com.proyecto.hce_backend.repository.LedgerBloqueRepository;
import com.proyecto.hce_backend.repository.PacienteRepository;
import com.proyecto.hce_backend.repository.TrazabilidadBlockchainRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class LedgerServiceIntegrationTest {

    private static final String HASH_A = "a".repeat(64);
    private static final String HASH_B = "b".repeat(64);

    @Autowired
    private LedgerService ledgerService;

    @Autowired
    private LedgerBloqueRepository ledgerRepository;

    @Autowired
    private TrazabilidadBlockchainRepository trazabilidadRepository;

    @Autowired
    private BlockchainTraceabilityService traceabilityService;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Value("${spring.datasource.url}")
    private String urlBaseDeDatos;

    @BeforeEach
    void limpiar() {
        // Salvaguarda: estas pruebas borran tablas, asi que solo corren contra la base H2 en memoria.
        assertTrue(urlBaseDeDatos.startsWith("jdbc:h2:mem:"),
                "La prueba solo puede ejecutarse contra H2 en memoria, no contra: " + urlBaseDeDatos.replaceAll("//.*", "//***"));

        ledgerRepository.deleteAll();
        trazabilidadRepository.deleteAll();
        pacienteRepository.deleteAll();
    }

    @Test
    void cadaCambioGeneraUnaVersionNuevaEnlazadaALaAnterior() {
        LedgerBloque v1 = ledgerService.registerEvidence("CITA", "7", HASH_A, "CREAR", "2", "op-1");
        LedgerBloque v2 = ledgerService.registerEvidence("CITA", "7", HASH_B, "ACTUALIZAR", "2", "op-2");

        assertEquals(1, v1.getVersion());
        assertEquals(2, v2.getVersion());
        assertNull(v1.getEvidenciaPreviaTx());
        assertEquals(v1.getTxId(), v2.getEvidenciaPreviaTx());
        assertEquals(v1.getHashBloque(), v2.getHashBloqueAnterior());
        assertEquals("HealthcareMSP.peer,AuditMSP.peer", v2.getEndosos());

        List<LedgerBloque> historial = ledgerService.getHistory("CITA", "7");
        assertEquals(2, historial.size());
        assertEquals(HASH_B, ledgerService.getEvidence("CITA", "7").orElseThrow().getHash());
        assertEquals(HASH_A, ledgerService.getEvidence("CITA", "7", 1).orElseThrow().getHash());
    }

    @Test
    void unaSolicitudEquivalenteDevuelveLaEvidenciaExistenteYUnoDistintoSeRechaza() {
        LedgerBloque original = ledgerService.registerEvidence("CITA", "8", HASH_A, "CREAR", "2", "op-x");
        LedgerBloque repetida = ledgerService.registerEvidence("CITA", "8", HASH_A, "CREAR", "2", "op-x");

        assertEquals(original.getNumeroBloque(), repetida.getNumeroBloque());
        assertEquals(1, ledgerRepository.count());

        assertThrows(RuntimeException.class,
                () -> ledgerService.registerEvidence("CITA", "8", HASH_B, "CREAR", "2", "op-x"));
        assertEquals(1, ledgerRepository.count());
    }

    @Test
    void laCadenaDetectaUnBloqueAlterado() {
        LedgerBloque b1 = ledgerService.registerEvidence("PACIENTE", "1", HASH_A, "CREAR", null, "op-a");
        ledgerService.registerEvidence("PACIENTE", "2", HASH_B, "CREAR", null, "op-b");
        ledgerService.registerEvidence("PACIENTE", "3", HASH_A, "CREAR", null, "op-c");

        assertTrue(ledgerService.primerBloqueInvalido().isEmpty());
        assertTrue(ledgerService.estado().getCadenaIntegra());

        // Alteracion directa de la tabla: el hash del primer bloque ya no corresponde a su contenido.
        b1.setHash(HASH_B);
        ledgerRepository.save(b1);

        assertEquals(b1.getNumeroBloque(), ledgerService.primerBloqueInvalido().orElseThrow());
        assertFalse(ledgerService.estado().getCadenaIntegra());

        List<LedgerBloqueDTO> historial = traceabilityService.historialLedger("PACIENTE", "1");
        assertFalse(historial.get(0).getBloqueValido());
    }

    @Test
    void verificarDistingueVerificadoDiscrepanciaSinEvidenciaYLedgerAlterado() {
        Paciente paciente = new Paciente();
        paciente.setTipoDocumento(TipoDocumento.DNI);
        paciente.setNumeroDocumento("12345678");
        paciente.setNombres("Lucia");
        paciente.setApellidos("Perez");
        paciente.setFechaNacimiento(LocalDate.of(1990, 5, 20));
        paciente.setSexo("F");
        paciente.setEstadoCivil(EstadoCivil.SOLTERO);
        paciente.setEstado(true);
        paciente = pacienteRepository.save(paciente);

        String id = String.valueOf(paciente.getId());

        // Sin evidencia: no se emite conclusion de integridad.
        assertEquals("SIN_REGISTRO", verificar("PACIENTE", id).getEstado());

        // Registrada: el hash recalculado coincide.
        traceabilityService.registrarEntidad("PACIENTE", "CREAR", paciente, null);
        TraceabilityVerifyResponseDTO ok = verificar("PACIENTE", id);
        assertEquals("VERIFICADO", ok.getEstado());
        assertTrue(ok.getIntegro());
        assertEquals(1, ok.getVersion());
        assertEquals(LedgerService.MODO, ok.getModoLedger());

        // El dato cambia sin registrar evidencia: discrepancia.
        paciente.setNombres("Lucia Maria");
        paciente = pacienteRepository.save(paciente);
        TraceabilityVerifyResponseDTO alterado = verificar("PACIENTE", id);
        assertEquals("DISCREPANCIA", alterado.getEstado());
        assertFalse(alterado.getIntegro());
        assertNotEquals(alterado.getHashActual(), alterado.getHashRegistrado());

        // Un cambio legitimo registra una nueva version y vuelve a coincidir.
        traceabilityService.registrarEntidad("PACIENTE", "ACTUALIZAR", paciente, null);
        TraceabilityVerifyResponseDTO nuevo = verificar("PACIENTE", id);
        assertEquals("VERIFICADO", nuevo.getEstado());
        assertEquals(2, nuevo.getVersion());

        // Si el ledger fue manipulado, no se concluye nada sobre el dato.
        LedgerBloque primero = ledgerService.getEvidence("PACIENTE", id, 1).orElseThrow();
        primero.setAction("MANIPULADO");
        ledgerRepository.save(primero);
        assertEquals("LEDGER_ALTERADO", verificar("PACIENTE", id).getEstado());
    }

    @Test
    void elResumenClinicoSoloLoRecibeElPersonalDeSalud() {
        Paciente paciente = new Paciente();
        paciente.setTipoDocumento(TipoDocumento.DNI);
        paciente.setNumeroDocumento("87654321");
        paciente.setNombres("Marco");
        paciente.setApellidos("Rios");
        paciente.setFechaNacimiento(LocalDate.of(1985, 1, 10));
        paciente.setSexo("M");
        paciente.setEstadoCivil(EstadoCivil.CASADO);
        paciente.setEstado(true);
        paciente = pacienteRepository.save(paciente);
        String id = String.valueOf(paciente.getId());

        List<CampoResumenDTO> medico = traceabilityService.resumenRegistro("PACIENTE", id, false);
        assertTrue(medico.stream().anyMatch(c -> c.getEtiqueta().equals("Nombre") && c.getValor().equals("Marco Rios")));
        assertTrue(medico.stream().anyMatch(c -> c.getEtiqueta().equals("Estado") && c.getValor().equals("Activo")));

        assertTrue(traceabilityService.resumenRegistro("PACIENTE", id, true).isEmpty());
    }

    private TraceabilityVerifyResponseDTO verificar(String tipo, String id) {
        TraceabilityVerifyRequestDTO solicitud = new TraceabilityVerifyRequestDTO();
        solicitud.setEntityType(tipo);
        solicitud.setEntityId(id);
        return traceabilityService.verificar(solicitud);
    }
}
