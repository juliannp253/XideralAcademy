package com.logistic.fast_track.repository.dto;

import com.logistic.fast_track.core.model.Cliente;
import com.logistic.fast_track.core.model.Direccion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record EnvioRequestDTO(
        @NotBlank(message = "El tipo de envío es obligatorio (AEREO o TERRESTRE)")
        String tipo,

        @Positive(message = "El peso debe ser mayor a 0")
        double peso,

        @NotNull(message = "Los datos del remitente son obligatorios")
        Cliente remitente,

        @NotNull(message = "Los datos del destino son obligatorios")
        Direccion destino,

        String aerolinea,
        String matriculaCamion
) {}
