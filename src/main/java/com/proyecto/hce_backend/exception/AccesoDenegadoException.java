package com.proyecto.hce_backend.exception;

/** El usuario esta autenticado pero no puede realizar la accion sobre este recurso (HTTP 403). */
public class AccesoDenegadoException extends RuntimeException {

    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
