package com.logistic.fast_track.core.model;

import java.time.LocalDate;

public abstract class Envio implements iRastreable, Comparable<Envio>{
    protected String idRastreo;
    protected double peso;
    protected LocalDate fechaCreacion;
    protected String estado;
    protected Cliente remitente; // HAS-A
    protected Direccion destino; // HAS-A

    public Envio(String idRastreo, double peso, LocalDate fechaCreacion, Cliente remitente, Direccion destino) {
        this.idRastreo = idRastreo;
        this.peso = peso;
        this.fechaCreacion = fechaCreacion;
        this.remitente = remitente;
        this.destino = destino;
        this.estado = "CREADO";
    }

    public abstract double calcularTiempoEstimado();
    public abstract void actualizarEstado(String estado);

    @Override
    public int compareTo(Envio otroEnvio){
        return this.fechaCreacion.compareTo(otroEnvio.fechaCreacion);
    }

    // Getters
    public String getIdRastreo() {
        return idRastreo;
    }
    public double getPeso() {
        return peso;
    }
    public LocalDate getFechaCreacion() {
        return fechaCreacion;
    }
    public Cliente getRemitente() {
        return remitente;
    }
    public String getEstado() { return estado; }
    public Direccion getDestino() {
        return destino;
    }


}
