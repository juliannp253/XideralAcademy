package com.logistic.fast_track.core.model;

import java.time.LocalDate;

// IS-A
public class EnvioTerrestre extends Envio{
    private String matriculaCamion;

    public EnvioTerrestre(String idRastreo, double peso, LocalDate fechaCreacion,
                      Cliente remitente, Direccion destino, String matriculaCamion) {
        super(idRastreo, peso, fechaCreacion, remitente, destino);
        this.matriculaCamion = matriculaCamion;
    }

    @Override
    public double calcularTiempoEstimado(){
        return 5.0;
    }

    @Override
    public String obtenerUbicacionActual() {
        return "En transito terrestre - Camión: " + matriculaCamion;
    }

    @Override
    public void actualizarEstado(String estado) {
        this.estado = estado;
    }

    public String getMatriculaCamion() { return matriculaCamion; }
}
