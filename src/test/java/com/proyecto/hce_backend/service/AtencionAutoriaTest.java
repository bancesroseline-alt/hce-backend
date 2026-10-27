package com.proyecto.hce_backend.service;

import com.proyecto.hce_backend.exception.AccesoDenegadoException;
import com.proyecto.hce_backend.model.AtencionMedica;
import com.proyecto.hce_backend.model.Usuario;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AtencionAutoriaTest {

    private final AtencionMedicaService servicio = new AtencionMedicaService();

    private AtencionMedica atencionDe(String username) {
        Usuario autor = new Usuario();
        autor.setId(7L);
        autor.setUsername(username);

        AtencionMedica atencion = new AtencionMedica();
        atencion.setUsuario(autor);
        return atencion;
    }

    @Test
    void elMedicoAutorPuedeEditarSuAtencion() {
        assertDoesNotThrow(() -> servicio.verificarAutoria(atencionDe("medico1"), "medico1"));
        assertDoesNotThrow(() -> servicio.verificarAutoria(atencionDe("Medico1"), "medico1"));
    }

    @Test
    void otroUsuarioNoPuedeEditarLaAtencion() {
        assertThrows(AccesoDenegadoException.class,
                () -> servicio.verificarAutoria(atencionDe("medico1"), "medico2"));
    }

    @Test
    void sinUsuarioAutenticadoOSinAutorSeDeniega() {
        assertThrows(AccesoDenegadoException.class,
                () -> servicio.verificarAutoria(atencionDe("medico1"), null));
        assertThrows(AccesoDenegadoException.class,
                () -> servicio.verificarAutoria(new AtencionMedica(), "medico1"));
    }
}
