package com.logistic.fast_track.core.model;

import java.time.LocalDateTime;

public record EtiquetaLogistica(
        String idRastreo,
        String codigoBarras,
        LocalDateTime fechaEmision
) {}