package com.logistic.fast_track.controller;

import com.logistic.fast_track.core.model.EtiquetaLogistica;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/envios")
public class EnvioController {
    @PostMapping
    public ResponseEntity<EtiquetaLogistica> crearEnvio(){
        String idGenerado = "TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String codigoBarras = "BARCODE-00200503";

        EtiquetaLogistica nuevaEtiqueta = new EtiquetaLogistica(idGenerado, codigoBarras, LocalDateTime.now());
        return new ResponseEntity<>(nuevaEtiqueta, HttpStatus.CREATED);
    }

    @GetMapping
    public String ping(){
        return "Servidor FastTrack inicializado";
    }
}
