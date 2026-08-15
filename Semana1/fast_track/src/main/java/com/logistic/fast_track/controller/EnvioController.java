package com.logistic.fast_track.controller;

import com.logistic.fast_track.controller.dto.CotizacionResponseDTO;
import com.logistic.fast_track.core.model.*;
import com.logistic.fast_track.repository.dto.EnvioRequestDTO;
import com.logistic.fast_track.service.GestorEnvioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/envios")
public class EnvioController {

    private final GestorEnvioService gestorEnvioService;

    public EnvioController(GestorEnvioService gestorEnvioService){
        this.gestorEnvioService = gestorEnvioService;
    }

    @PostMapping
    public ResponseEntity<EtiquetaLogistica> crearEnvio(@Valid @RequestBody EnvioRequestDTO request){
        EtiquetaLogistica etiqueta = gestorEnvioService.procesarNuevoEnvio(request);
        return new ResponseEntity<>(etiqueta, HttpStatus.CREATED);
    }

    @GetMapping("/listar")
    public ResponseEntity<List<Envio>> listarEnvios(
            @RequestParam(value = "orden", required = false, defaultValue = "fecha") String orden){
        List<Envio> envios = gestorEnvioService.obtenerTodosLosEnvios(orden);
        if (envios.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(envios, HttpStatus.OK);
    }

    @GetMapping("/cotizar")
    public ResponseEntity<CotizacionResponseDTO> cotizarEmbalaje(
            @RequestParam double peso,
            @RequestParam(defaultValue = "ESTANDAR") String tipoEmbalaje){
        double costo = gestorEnvioService.cotizarEmbalaje(peso, tipoEmbalaje);
        CotizacionResponseDTO respuesta = new CotizacionResponseDTO(peso, tipoEmbalaje.toUpperCase(), costo, "MXN");

         return new ResponseEntity<>(respuesta, HttpStatus.OK);
    }

    @PatchMapping("/{idRastreo}/estado")
    public ResponseEntity<Envio> actualizarEstado(
            @PathVariable String idRastreo,
            @RequestParam EstadoEnvio nuevoEstado) {

        Envio envioActualizado = gestorEnvioService.cambiarEstadoEnvio(idRastreo, nuevoEstado);
        return ResponseEntity.ok(envioActualizado);
    }

    @GetMapping("/lotes/aereos")
    public ResponseEntity<LoteTransporte<EnvioAereo>> obtenerLoteVuelo(
            @RequestParam(defaultValue = "10") int capacidad) {

        LoteTransporte<EnvioAereo> lote = gestorEnvioService.armarLoteAereo(capacidad);

        return ResponseEntity.ok(lote);
    }
}
