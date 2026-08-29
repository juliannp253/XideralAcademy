package com.academymty.testing;

import com.academymty.testing.model.Alumno;
import com.academymty.testing.model.Curso;
import com.academymty.testing.model.InscripcionResultado;
import com.academymty.testing.repository.AlumnoRepository;
import com.academymty.testing.service.InscripcionService;
import com.academymty.testing.service.NotificacionService;
import com.academymty.testing.util.ValidadorCurp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// Pruebas unitarias para InscripcionService utilizando Mockito.
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias de InscripcionService con Mockito")
class InscripcionServiceTest {

    @Mock
    private AlumnoRepository alumnoRepository;

    @Mock
    private NotificacionService notificacionService;

    @Spy
    private ValidadorCurp validadorCurp = new ValidadorCurp();

    @InjectMocks
    private InscripcionService inscripcionService;

    @Captor
    private ArgumentCaptor<String> captorEmail;

    @Captor
    private ArgumentCaptor<String> captorMensaje;

    private Alumno alumnoValido;
    private Curso curso;

    @BeforeEach
    void setup() {
        alumnoValido = new Alumno(10, "A010", "Francisco Perez", "PAGF900101HNLZRK09", "paco@academymty.mx");
        curso = new Curso(501, "Patrones de Diseño", 5);
    }

    @Nested
    @DisplayName("Casos de Éxito de Inscripción")
    class CasosDeExito {

        @Test
        @DisplayName("Debe inscribir a un alumno nuevo, persistirlo y enviar correo de bienvenida")
        void inscribirAlumnoNuevoExitosamente() {
            // 1. Arrange: Programar respuestas de los mocks
            when(alumnoRepository.existsByCurp(alumnoValido.curp())).thenReturn(false);
            when(alumnoRepository.save(alumnoValido)).thenReturn(alumnoValido);
            when(notificacionService.enviarCorreoBienvenida(anyString(), anyString())).thenReturn(true);

            // 2. Act
            InscripcionResultado resultado = inscripcionService.inscribirAlumno(curso, alumnoValido);

            // 3. Assert: Validación de resultado
            assertAll("Validar resultado de inscripción",
                    () -> assertTrue(resultado.exitosa()),
                    () -> assertNotNull(resultado.folio()),
                    () -> assertEquals(alumnoValido, resultado.alumno()),
                    () -> assertEquals(1, curso.getAlumnos().size())
            );

            // 4. Verificación de interacciones de los mocks
            verify(alumnoRepository, times(1)).existsByCurp(alumnoValido.curp());
            verify(alumnoRepository, times(1)).save(alumnoValido);
            verify(notificacionService, times(1)).enviarCorreoBienvenida(eq("paco@academymty.mx"), anyString());

            verify(notificacionService, never()).enviarAlertaAdministrador(anyString(), anyString());
        }

        @Test
        @DisplayName("No debe volver a guardar en el repositorio si el alumno ya existía")
        void noGuardarSiAlumnoYaExiste() {
            when(alumnoRepository.existsByCurp(alumnoValido.curp())).thenReturn(true);

            InscripcionResultado resultado = inscripcionService.inscribirAlumno(curso, alumnoValido);

            assertTrue(resultado.exitosa());

            verify(alumnoRepository, never()).save(any(Alumno.class));
            verify(notificacionService, times(1)).enviarCorreoBienvenida(anyString(), anyString());
        }

        @Test
        @DisplayName("Debe disparar alerta al administrador cuando el cupo disponible queda en 2 o menos")
        void alertarAlAdministradorPorCupoBajo() {
            Curso cursoPequeno = new Curso(502, "Taller Intensivo", 2);

            when(alumnoRepository.existsByCurp(anyString())).thenReturn(true);

            inscripcionService.inscribirAlumno(cursoPequeno, alumnoValido);

            verify(notificacionService, times(1)).enviarAlertaAdministrador(
                    eq("Alerta Cupo Bajo"),
                    contains("tiene solo 1 lugares disponibles")
            );
        }
    }

    @Nested
    @DisplayName("Captura de Argumentos con ArgumentCaptor")
    class PruebasConArgumentCaptor {

        @Test
        @DisplayName("Debe capturar y verificar que el mensaje de bienvenida incluya el nombre del alumno y del curso")
        void validarContenidoDelMensajeCapturado() {
            when(alumnoRepository.existsByCurp(anyString())).thenReturn(true);

            inscripcionService.inscribirAlumno(curso, alumnoValido);

            verify(notificacionService).enviarCorreoBienvenida(captorEmail.capture(), captorMensaje.capture());

            String emailCapturado = captorEmail.getValue();
            String mensajeCapturado = captorMensaje.getValue();

            assertEquals("paco@academymty.mx", emailCapturado);
            assertTrue(mensajeCapturado.contains("Francisco Perez"), "El mensaje debe contener el nombre");
            assertTrue(mensajeCapturado.contains("Patrones de Diseño"), "El mensaje debe contener el curso");
        }
    }

    @Nested
    @DisplayName("Casos de Error y Validación de No-Interacción")
    class CasosDeError {

        @Test
        @DisplayName("Debe rechazar la inscripción si la CURP es inválida y NO tocar dependencias externas")
        void rechazarInscripcionPorCurpInvalida() {
            Alumno alumnoCurpMala = new Alumno(11, "A011", "Luis Vega", "CURP_INVALIDA_999", "luis@academymty.mx");

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> inscripcionService.inscribirAlumno(curso, alumnoCurpMala)
            );

            assertTrue(exception.getMessage().contains("CURP del alumno no es válida"));

            verify(alumnoRepository, never()).existsByCurp(anyString());
            verify(alumnoRepository, never()).save(any());
            verifyNoInteractions(notificacionService);
            assertEquals(0, curso.getAlumnos().size(), "El curso debe permanecer intacto");
        }

        @Test
        @DisplayName("Demostración de @Spy: alterar temporalmente la lógica real con doReturn()")
        void alterarComportamientoDeSpy() {
            doReturn(false).when(validadorCurp).esValida(anyString());

            assertThrows(
                    IllegalArgumentException.class,
                    () -> inscripcionService.inscribirAlumno(curso, alumnoValido)
            );
        }
    }
}
