package com.logistic.fast_track.repository.dto;

import com.logistic.fast_track.core.model.Cliente;
import com.logistic.fast_track.core.model.Direccion;

public record EnvioRequestDTO(
        String tipo,
        double peso,
        Cliente remitente,
        Direccion destino,
        String aerolinea,
        String matriculaCamion
) {}
