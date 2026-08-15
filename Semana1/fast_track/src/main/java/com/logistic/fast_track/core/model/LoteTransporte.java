package com.logistic.fast_track.core.model;

import com.logistic.fast_track.core.exception.ReglaNegocioException;

import java.util.ArrayList;
import java.util.List;

public class LoteTransporte <T extends Envio>{
    private List<T> paquetes;
    private int capacidadMaxima;

    public LoteTransporte(int capacidadMaxima){
        this.capacidadMaxima = capacidadMaxima;
        this.paquetes = new ArrayList<>();
    }

    public void cargarPaquetes(T paquete){
        if (paquetes.size() >= capacidadMaxima) {
            throw new ReglaNegocioException("Lote de transporte lleno.");
        }
        paquetes.add(paquete);
    }

    public List<T> getPaquetes() { return paquetes; }
}
