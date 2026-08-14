package com.logistic.fast_track.service;

import com.logistic.fast_track.core.model.Envio;
import com.logistic.fast_track.core.model.EnvioAereo;
import com.logistic.fast_track.core.model.EnvioTerrestre;
import com.logistic.fast_track.core.model.EtiquetaLogistica;
import com.logistic.fast_track.repository.EnvioRepository;
import com.logistic.fast_track.repository.dto.EnvioRequestDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;


@Service
public class GestorEnvioService {
    private final EnvioRepository envioRepository;

    public GestorEnvioService(EnvioRepository envioRepository){
        this.envioRepository = envioRepository;
    }

    @Transactional
    public EtiquetaLogistica procesarNuevoEnvio(EnvioRequestDTO dto){

        String idRastreo = "TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Envio nuevoEnvio;

        if ("AEREO".equalsIgnoreCase(dto.tipo())) {
            nuevoEnvio = new EnvioAereo(
                    idRastreo, dto.peso(), LocalDate.now(),
                    dto.remitente(), dto.destino(), dto.aerolinea()
            );
        } else if ("TERRESTRE".equalsIgnoreCase(dto.tipo())) {
            nuevoEnvio = new EnvioTerrestre(
                    idRastreo, dto.peso(), LocalDate.now(),
                    dto.remitente(), dto.destino(), dto.matriculaCamion()
            );
        } else {
            throw new IllegalArgumentException("Tipo de envío no permitido");
        }

        envioRepository.save(nuevoEnvio);
        return new EtiquetaLogistica(idRastreo, "BARCODE-" + idRastreo, LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public List<Envio> obtenerTodosLosEnvios(String orden){
        List<Envio> envios = envioRepository.findAll();

        if ("peso_desc".equalsIgnoreCase(orden)){
            // Lambda + Comparator
            envios.sort((e1, e2) -> Double.compare(e2.getPeso(), e1.getPeso()));
        } else {
            java.util.Collections.sort(envios);
        }
        return envios;
    }
}
