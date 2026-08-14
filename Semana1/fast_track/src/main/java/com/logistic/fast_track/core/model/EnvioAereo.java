package com.logistic.fast_track.core.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDate;

// IS-A
@Entity
@Table(name = "envios_aereos")
public class EnvioAereo extends Envio{
    private String numeroVuelo;
    private String aerolinea;

    protected EnvioAereo() {}

    public EnvioAereo(String idRastreo, double peso, LocalDate fechaCreacion,
                      Cliente remitente, Direccion destino, String aerolinea) {
        super(idRastreo, peso, fechaCreacion, remitente, destino);
        this.aerolinea = aerolinea;
    }

    public void asignarNumeroVuelo(String vuelo){
        this.numeroVuelo = vuelo;
    }

    @Override
    public double calcularTiempoEstimado() {
        return 1.0;
    }

    @Override
    public String obtenerUbicacionActual() {
        return "En tránsito aéreo - Vuelo: " + (numeroVuelo != null ? numeroVuelo : "Pendiente");
    }

    @Override
    public void actualizarEstado(String estado) {
        this.estado = estado;
    }

    public String getNumeroVuelo() { return numeroVuelo; }
    public String getAerolinea() { return aerolinea; }
}
