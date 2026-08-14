package com.logistic.fast_track.controller.dto;

public record CotizacionResponseDTO (
        double peso,
        String tipoEmbalaje,
        double costoFinal,
        String moneda
){}
