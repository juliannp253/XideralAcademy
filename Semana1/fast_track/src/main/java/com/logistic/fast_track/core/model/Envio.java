package com.logistic.fast_track.core.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "envios")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Envio implements iRastreable, Comparable<Envio>{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;
    protected String idRastreo;
    protected double peso;
    protected LocalDate fechaCreacion;
    @Enumerated(EnumType.STRING)
    protected EstadoEnvio estado;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "cliente_id")
    protected Cliente remitente; // HAS-A
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "direccion_id")
    protected Direccion destino; // HAS-A

    protected Envio() {}

    public Envio(String idRastreo, double peso, LocalDate fechaCreacion, Cliente remitente, Direccion destino) {
        this.idRastreo = idRastreo;
        this.peso = peso;
        this.fechaCreacion = fechaCreacion;
        this.remitente = remitente;
        this.destino = destino;
        this.estado = EstadoEnvio.CREADO;
    }

    public abstract double calcularTiempoEstimado();
    public abstract void actualizarEstado(EstadoEnvio estado);

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
    public EstadoEnvio getEstado() { return estado; }
    public Direccion getDestino() {
        return destino;
    }

}
