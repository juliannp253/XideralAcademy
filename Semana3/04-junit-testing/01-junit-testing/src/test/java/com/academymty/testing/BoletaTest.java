package com.academymty.testing;

import com.academymty.testing.model.Alumno;
import com.academymty.testing.model.Boleta;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

// Pruebas unitarias para la clase Boleta.
@DisplayName("Pruebas Unitarias de Boleta Académica")
class BoletaTest {

    private Alumno alumnoPrueba;
    private Boleta boleta;

    @BeforeAll
    static void antesDeTodosLosTests() {
    }

    @AfterAll
    static void despuesDeTodosLosTests() {
    }

    @BeforeEach
    void prepararCadaTest(TestInfo testInfo) {
        // JUnit crea una NUEVA INSTANCIA de BoletaTest por cada método @Test.
        // @BeforeEach asegura que cada prueba inicie con un estado limpio e independiente.
        alumnoPrueba = new Alumno(1, "A001", "Ana Gomez", "GOMA850620HJCZNN01", "ana@academymty.mx");
        boleta = new Boleta(alumnoPrueba);
    }

    @AfterEach
    void limpiarCadaTest() {
        // Para después de cada test individual liberar recursos si es necesario
    }

    @Test
    @DisplayName("Debe registrar calificaciones correctamente y asociarlas al alumno")
    void registrarCalificacionesCorrectamente() {
        // 1. Arrange (Preparar)
        boleta.agregarCalificacion("Matemáticas", 85.0);
        boleta.agregarCalificacion("Física", 90.0);

        // 2. Act (Actuar)
        int totalMaterias = boleta.getCantidadMaterias();
        Alumno alumnoAsociado = boleta.getAlumno();

        // 3. Assert (Afirmar)
        assertEquals(2, totalMaterias, "La cantidad de materias registradas debe ser 2");
        assertNotNull(alumnoAsociado, "El alumno asociado no debe ser nulo");
        assertSame(alumnoPrueba, alumnoAsociado, "Debe ser exactamente la misma instancia de Alumno en memoria");
    }

    @Test
    @DisplayName("Debe calcular el promedio general con precisión decimal (Delta)")
    void calcularPromedioConPrecisionDecimal() {
        boleta.agregarCalificacion("Historia", 80.0);
        boleta.agregarCalificacion("Geografía", 85.0);
        boleta.agregarCalificacion("Química", 76.0);

        double promedio = boleta.calcularPromedio();

        // (80 + 85 + 76) / 3 = 241 / 3 = 80.33333333333333
        assertEquals(80.333, promedio, 0.001, "El promedio debe coincidir con una tolerancia de 0.001");
    }

    @Test
    @DisplayName("Aserciones agrupadas con assertAll para validar múltiples propiedades juntas")
    void validarMultiplesPropiedadesConAssertAll() {
        boleta.agregarCalificacion("Programación", 95.0);
        boleta.agregarCalificacion("Bases de Datos", 88.0);

        assertAll("Verificación completa de la boleta",
                () -> assertEquals(2, boleta.getCantidadMaterias(), "Debe tener 2 materias"),
                () -> assertTrue(boleta.estaAprobado(), "El alumno debe estar aprobado"),
                () -> assertEquals("APROBADO", boleta.obtenerEstadoMateria("Programación")),
                () -> assertEquals("APROBADO", boleta.obtenerEstadoMateria("Bases de Datos"))
        );
    }

    @Nested
    @DisplayName("Pruebas de Límites (Boundary Testing)")
    class PruebasDeFrontera {

        @Test
        @DisplayName("Frontera Inferior Reprobatoria: Promedio 69.99 debe estar REPROBADO")
        void promedio69Punto99DebeEstarReprobado() {
            boleta.agregarCalificacion("Matemáticas", 69.99);

            assertFalse(boleta.estaAprobado(), "Un promedio de 69.99 NO alcanza la nota mínima de 70.0");
            assertEquals("REPROBADO", boleta.obtenerEstadoMateria("Matemáticas"));
        }

        @Test
        @DisplayName("Frontera Exacta Aprobatoria: Promedio 70.00 debe estar APROBADO")
        void promedioExacto70DebeEstarAprobado() {
            boleta.agregarCalificacion("Matemáticas", 70.00);

            assertTrue(boleta.estaAprobado(), "Un promedio de 70.00 EXACTO debe ser APROBATORIO (regla >= 70.0)");
            assertEquals("APROBADO", boleta.obtenerEstadoMateria("Matemáticas"));
        }

        @Test
        @DisplayName("Frontera Superior Aprobatoria: Promedio 70.01 debe estar APROBADO")
        void promedio70Punto01DebeEstarAprobado() {
            boleta.agregarCalificacion("Matemáticas", 70.01);

            assertTrue(boleta.estaAprobado(), "Un promedio de 70.01 debe estar APROBADO");
        }
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException al agregar calificaciones fuera del rango 0-100")
    void lanzarExcepcionPorCalificacionFueraDeRango() {
        IllegalArgumentException errorNegativo = assertThrows(
                IllegalArgumentException.class,
                () -> boleta.agregarCalificacion("Física", -5.0),
                "Calificación negativa debe lanzar IllegalArgumentException"
        );
        assertTrue(errorNegativo.getMessage().contains("entre 0.0 y 100.0"));

        assertThrows(
                IllegalArgumentException.class,
                () -> boleta.agregarCalificacion("Química", 100.5),
                "Calificación mayor a 100 debe lanzar IllegalArgumentException"
        );
    }

    @Test
    @DisplayName("Debe lanzar IllegalStateException al calcular promedio de una boleta vacía")
    void lanzarExcepcionAlCalcularPromedioSinCalificaciones() {
        assertThrows(
            IllegalStateException.class,
            () -> boleta.calcularPromedio(),
            "Calcular promedio sin materias debe lanzar IllegalStateException"
        );
    }
}
