package com.logistic.fast_track.controller.dto;

import java.time.LocalDateTime;

public record ErrorResponseDTO (
    LocalDateTime fecha,
    int status,
    String error,
    String mensaje,
    String ruta
){}
