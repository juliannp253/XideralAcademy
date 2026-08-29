package com.academymty.testing;

import com.academymty.testing.util.ValidadorCurp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

// Pruebas unitarias para ValidadorCurp.
@DisplayName("Pruebas Parametrizadas del Validador de CURP")
class ValidadorCurpTest {

    private ValidadorCurp validador;

    @BeforeEach
    void setup() {
        validador = new ValidadorCurp();
    }

    @ParameterizedTest(name = "[{index}] La CURP ''{0}'' debe ser válida")
    @ValueSource(strings = {
            "RUGM800101HNLZRK09",
            "ROAL920315MDFRNR02",
            "GOMA850620HJCZNN01",
            "PENA751130MOCNRS05"
    })
    @DisplayName("Debe aceptar CURPs con estructura y entidades federativas válidas")
    void aceptarCurpsValidas(String curpValida) {
        assertTrue(validador.esValida(curpValida), "La CURP " + curpValida + " debería ser aceptada");
    }

    @ParameterizedTest(name = "[{index}] Entrada nula o vacía debe ser rechazada")
    @NullAndEmptySource
    @DisplayName("Debe rechazar de inmediato valores nulos o cadenas vacías")
    void rechazarNulosYVacios(String curpInvalida) {
        assertFalse(validador.esValida(curpInvalida), "Cadenas nulas o vacías no son CURPs válidas");
    }

    @ParameterizedTest(name = "[{index}] CURP ''{0}'' -> Se espera válido: {1}")
    @CsvSource({
            "RUGM800101HNLZRK09, true",   // Válida estándar
            "ROAL920315MDFRNR02, true",   // Válida estándar
            "RUGM800101,        false",  // Longitud corta
            "RUGM800101HXXZRK09, false",  // Entidad 'XX' no existe
            "123456789012345678, false",  // Letras reemplazadas por números
            "RUGM800101HNLZRK0999, false" // Longitud excedida
    })
    @DisplayName("Validación con múltiples columnas de datos usando @CsvSource")
    void validarCasosConCsvSource(String curp, boolean esperado) {
        assertEquals(esperado, validador.esValida(curp), "El resultado de validación para " + curp + " no coincidió");
    }

    @ParameterizedTest(name = "[{index}] Archivo CSV -> CURP: {0}, Esperado: {1}")
    @CsvFileSource(resources = "/curps_prueba.csv", numLinesToSkip = 1)
    @DisplayName("Carga masiva de casos de prueba desde archivo externo curps_prueba.csv")
    void validarCasosDesdeArchivoCsv(String curp, boolean esperado) {
        assertEquals(esperado, validador.esValida(curp));
    }

    @RepeatedTest(value = 3, name = "Repetición {currentRepetition} de {totalRepetitions}")
    @DisplayName("Prueba repetida para comprobar consistencia determinista")
    void pruebaRepetidaDeDeterminismo(RepetitionInfo info) {
        String curpConstante = "RUGM800101HNLZRK09";
        assertTrue(validador.esValida(curpConstante));
        assertEquals("HOMBRE", validador.obtenerGenero(curpConstante));
    }
}
