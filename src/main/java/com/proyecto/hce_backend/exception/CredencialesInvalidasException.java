package com.proyecto.hce_backend.exception;

public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException() {
        super("Usuario o contraseña incorrectos");
    }
}
