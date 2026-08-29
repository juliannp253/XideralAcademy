package com.academymty.testing;

import com.academymty.testing.exception.CursoCerradoException;
import com.academymty.testing.exception.CursoLlenoException;
import com.academymty.testing.model.Alumno;
import com.academymty.testing.model.Curso;
import org.junit.jupiter.api.*;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

// Pruebas unitarias para la clase Curso.
@DisplayName("Pruebas Unitarias de Gestión de Cursos")
class CursoTest {

    private Alumno alumno1;
    private Alumno alumno2;
    private Alumno alumno3;

    @BeforeEach
    void setup() {
        alumno1 = new Alumno(1, "A001", "Carlos Mendoza", "MENZ900101HDFRNR01", "carlos@academymty.mx");
        alumno2 = new Alumno(2, "A002", "Diana Soto", "SOTD920315MDFRNR02", "diana@academymty.mx");
        alumno3 = new Alumno(3, "A003", "Elena Rios", "RIOE950520MDFRNR03", "elena@academymty.mx");
    }

    @Nested
    @Tag("rapido")
    @DisplayName("Cuando el curso tiene cupo disponible")
    class CuandoTieneCupoDisponible {

        private Curso curso;

        @BeforeEach
        void crearCursoConCupo() {
            curso = new Curso(101, "Java Reactivo", 2);
        }

        @Test
        @DisplayName("Debe permitir inscribir alumnos sin lanzar ninguna excepción")
        void inscribirAlumnoConExito() {
            assertDoesNotThrow(() -> curso.inscribir(alumno1));
            assertEquals(1, curso.getAlumnos().size());
            assertEquals(1, curso.getCupoDisponible());
        }

        @Test
        @DisplayName("Debe inscribir hasta el límite máximo de capacidad")
        void inscribirHastaElLimite() {
            curso.inscribir(alumno1);
            curso.inscribir(alumno2);

            assertAll(
                    () -> assertEquals(2, curso.getAlumnos().size()),
                    () -> assertEquals(0, curso.getCupoDisponible())
            );
        }
    }

    @Nested
    @Tag("rapido")
    @DisplayName("Cuando el curso está lleno")
    class CuandoElCursoEstaLleno {

        private Curso cursoLleno;

        @BeforeEach
        void llenarCurso() {
            cursoLleno = new Curso(102, "Spring Boot Microservicios", 2);
            cursoLleno.inscribir(alumno1);
            cursoLleno.inscribir(alumno2);
        }

        @Test
        @DisplayName("Debe lanzar CursoLlenoException al intentar inscribir sobre el cupo máximo")
        void lanzarExcepcionPorCursoLleno() {
            CursoLlenoException excepcion = assertThrows(
                    CursoLlenoException.class,
                    () -> cursoLleno.inscribir(alumno3),
                    "Debe rechazar la inscripción cuando el cupo está agotado"
            );

            assertTrue(excepcion.getMessage().contains("No hay cupo disponible"));
            assertEquals(2, cursoLleno.getAlumnos().size(), "La lista de inscritos no debe modificarse");
        }
    }

    @Nested
    @Tag("rapido")
    @DisplayName("Cuando el curso está cerrado")
    class CuandoElCursoEstaCerrado {

        private Curso cursoCerrado;

        @BeforeEach
        void cerrarCurso() {
            cursoCerrado = new Curso(103, "DevOps y Cloud", 5);
            cursoCerrado.cerrarInscripciones();
        }

        @Test
        @DisplayName("Debe lanzar CursoCerradoException al intentar inscribir en curso cerrado")
        void lanzarExcepcionPorCursoCerrado() {
            CursoCerradoException excepcion = assertThrows(
                    CursoCerradoException.class,
                    () -> cursoCerrado.inscribir(alumno1)
            );
            assertTrue(excepcion.getMessage().contains("ya está cerrado"));
        }
    }

    @Nested
    @Tag("lento")
    @DisplayName("Pruebas de Tiempo y Rendimiento (Timeouts)")
    class PruebasDeTiempo {

        @Test
        @DisplayName("La generación de acta debe completarse en menos del tiempo límite establecido")
        void generacionDeActaDentroDelTiempoLimite() {
            Curso curso = new Curso(104, "Arquitectura de Software", 30);
            curso.inscribir(alumno1);

            String resultado = assertTimeout(
                    Duration.ofMillis(300),
                    () -> curso.generarActaCalificaciones(),
                    "La generación del acta tardó más del tiempo máximo permitido (300 ms)"
            );

            assertNotNull(resultado);
            assertTrue(resultado.contains("ACTA-CURSO-104"));
        }
    }

    @Nested
    @DisplayName("Los Tres Estados de un Test (Verde, Rojo y Abortado/Skipped)")
    class EstadosDeUnTest {

        @Test
        @Disabled("Demostración de test desactivado deliberadamente con @Disabled (Estado SKIPPED)")
        void testDeshabilitadoTemporalmente() {
            fail("Este test no se debe ejecutar porque tiene @Disabled");
        }
    }
}
