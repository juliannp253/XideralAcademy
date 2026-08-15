package com.logistic.fast_track.service;

import com.logistic.fast_track.core.exception.ReglaNegocioException;
import com.logistic.fast_track.core.model.Envio;
import com.logistic.fast_track.core.model.EnvioAereo;
import com.logistic.fast_track.core.model.EstadoEnvio;
import com.logistic.fast_track.repository.EnvioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GestorEnviosServiceTest {

    @Mock
    private EnvioRepository envioRepository;

    @InjectMocks
    private GestorEnvioService gestorEnviosService;

    private Envio envioDePrueba;

    @BeforeEach
    void setUp() {
        envioDePrueba = new EnvioAereo(
                "TRK-TEST", 10.0, LocalDate.now(), null, null, "Volaris"
        );
    }

    @Test
    void cambiarEstadoEnvio_TransicionValida_DebeActualizarYGuardar() {
        when(envioRepository.findByIdRastreo("TRK-TEST")).thenReturn(Optional.of(envioDePrueba));
        when(envioRepository.save(any(Envio.class))).thenReturn(envioDePrueba);

        Envio resultado = gestorEnviosService.cambiarEstadoEnvio("TRK-TEST", EstadoEnvio.EN_TRANSITO);

        assertNotNull(resultado);
        assertEquals(EstadoEnvio.EN_TRANSITO, resultado.getEstado());
        verify(envioRepository, times(1)).save(envioDePrueba);
    }

    @Test
    void cambiarEstadoEnvio_TransicionInvalida_DebeLanzarExcepcion() {
        when(envioRepository.findByIdRastreo("TRK-TEST")).thenReturn(Optional.of(envioDePrueba));
        ReglaNegocioException excepcion = assertThrows(ReglaNegocioException.class, () -> {
            gestorEnviosService.cambiarEstadoEnvio("TRK-TEST", EstadoEnvio.ENTREGADO);
        });

        assertTrue(excepcion.getMessage().contains("Transición inválida"));

        verify(envioRepository, never()).save(any(Envio.class));
    }
}
