package com.logistic.fast_track.core.strategy;

public class EmbalajeFragil implements iEstrategiaEmbalaje{
    @Override
    public double calcularCostoCaja(double peso) {
        return (peso * 10.0) + 200.0;
    }
}
