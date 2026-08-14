package com.logistic.fast_track.controller;

import com.logistic.fast_track.core.model.EtiquetaLogistica;
import com.logistic.fast_track.repository.dto.EnvioRequestDTO;
import com.logistic.fast_track.service.GestorEnvioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/envios")
public class EnvioController {

    private final GestorEnvioService gestorEnvioService;

    public EnvioController(GestorEnvioService gestorEnvioService){
        this.gestorEnvioService = gestorEnvioService;
    }

    @PostMapping
    public ResponseEntity<EtiquetaLogistica> crearEnvio(@RequestBody EnvioRequestDTO request){
        EtiquetaLogistica etiqueta = gestorEnvioService.procesarNuevoEnvio(request);
        return new ResponseEntity<>(etiqueta, HttpStatus.CREATED);
    }

    @GetMapping
    public String ping(){
        return "Servidor FastTrack inicializado";
    }
}
