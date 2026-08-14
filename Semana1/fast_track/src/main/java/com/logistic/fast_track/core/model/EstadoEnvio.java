package com.logistic.fast_track.core.model;

public enum EstadoEnvio {
    CREADO,
    EN_TRANSITO,
    ENTREGADO,
    CANCELADO;

    public boolean puedeTransicionarA(EstadoEnvio nuevoEstado) {
        if (this == CREADO) {
            return nuevoEstado == EN_TRANSITO || nuevoEstado == CANCELADO;
        }
        if (this == EN_TRANSITO) {
            return nuevoEstado == ENTREGADO || nuevoEstado == CANCELADO;
        }
        return false;
    }
}
