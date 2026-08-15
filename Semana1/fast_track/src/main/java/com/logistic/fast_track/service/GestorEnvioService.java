package com.logistic.fast_track.service;

import com.logistic.fast_track.core.exception.ReglaNegocioException;
import com.logistic.fast_track.core.model.*;
import com.logistic.fast_track.core.strategy.EmbalajeEstandar;
import com.logistic.fast_track.core.strategy.EmbalajeFragil;
import com.logistic.fast_track.core.strategy.GestorConfiguracion;
import com.logistic.fast_track.core.strategy.iEstrategiaEmbalaje;
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
            throw new ReglaNegocioException("Tipo de envío no permitido");
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

    public double cotizarEmbalaje(double peso, String tipoEmbalaje){
        iEstrategiaEmbalaje estrategia;
        if ("FRAGIL".equalsIgnoreCase(tipoEmbalaje)) {
            estrategia = new EmbalajeFragil();
        } else if ("ESTANDAR".equalsIgnoreCase(tipoEmbalaje)) {
            estrategia = new EmbalajeEstandar();
        } else {
            // Clase anonima
            estrategia = new iEstrategiaEmbalaje() {
                @Override
                public double calcularCostoCaja(double p) {
                    return p * 50.0;
                }
            };
        }

        double costoBase = estrategia.calcularCostoCaja(peso);
        double impuesto = GestorConfiguracion.getInstance().getImpuestoLocal();

        return costoBase * impuesto;
    }

    @Transactional
    public Envio cambiarEstadoEnvio(String idRastreo, EstadoEnvio nuevoEstado) {
        Envio envio = envioRepository.findByIdRastreo(idRastreo)
                .orElseThrow(() -> new ReglaNegocioException("No se encontró el envío con ID: " + idRastreo));

        if (!envio.getEstado().puedeTransicionarA(nuevoEstado)) {
            throw new ReglaNegocioException(
                    "Transición inválida. Un envío en estado " + envio.getEstado() + " no puede pasar a " + nuevoEstado
            );
        }

        envio.actualizarEstado(nuevoEstado);
        return envioRepository.save(envio);
    }

    @Transactional(readOnly = true)
    public LoteTransporte<EnvioAereo> armarLoteAereo(int capacidadMaxima) {

        LoteTransporte<EnvioAereo> loteVuelo = new LoteTransporte<>(capacidadMaxima);

        List<Envio> todosLosEnvios = envioRepository.findAll();

        todosLosEnvios.stream()
                .filter(envio -> envio instanceof EnvioAereo)
                .filter(envio -> envio.getEstado() == EstadoEnvio.CREADO)
                .map(envio -> (EnvioAereo) envio) // Casteo
                .limit(capacidadMaxima)
                .forEach(loteVuelo::cargarPaquetes); // Method reference

        return loteVuelo;
    }
}
