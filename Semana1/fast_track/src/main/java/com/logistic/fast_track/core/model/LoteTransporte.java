package com.logistic.fast_track.core.model;

import java.util.List;

public class LoteTransporte <T extends Envio>{
    private List<T> paquetes;
    private int capacidadMaxima;

    public LoteTransporte(int capacidadMaxima){
        this.capacidadMaxima = capacidadMaxima;
    }

    public void cargarPaquetes(T paquete){
        if (paquetes.size() >= capacidadMaxima) {
            throw new IllegalStateException("Lote de transporte lleno.");
        }
        paquetes.add(paquete);
    }

    public List<T> getPaquetes() { return paquetes; }
}
