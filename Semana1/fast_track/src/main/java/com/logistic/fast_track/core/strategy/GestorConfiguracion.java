package com.logistic.fast_track.core.strategy;

// Singleton
public class GestorConfiguracion {

    private static final GestorConfiguracion INSTANCIA = new GestorConfiguracion();

    private final double impuestoLocal;

    private GestorConfiguracion() {
        this.impuestoLocal = 1.16;
    }

    public static GestorConfiguracion getInstance() { return INSTANCIA; }

    public double getImpuestoLocal() { return impuestoLocal; }
}