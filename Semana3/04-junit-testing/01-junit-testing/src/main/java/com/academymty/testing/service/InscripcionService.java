package com.academymty.testing.service;

import com.academymty.testing.model.Alumno;
import com.academymty.testing.model.Curso;
import com.academymty.testing.model.InscripcionResultado;
import com.academymty.testing.repository.AlumnoRepository;
import com.academymty.testing.util.ValidadorCurp;

import java.util.UUID;

public class InscripcionService {

    private final AlumnoRepository alumnoRepository;
    private final NotificacionService notificacionService;
    private final ValidadorCurp validadorCurp;

    public InscripcionService(
            AlumnoRepository alumnoRepository,
            NotificacionService notificacionService,
            ValidadorCurp validadorCurp
    ) {
        this.alumnoRepository = alumnoRepository;
        this.notificacionService = notificacionService;
        this.validadorCurp = validadorCurp;
    }

    public InscripcionResultado inscribirAlumno(Curso curso, Alumno alumno) {
        if (curso == null) {
            throw new IllegalArgumentException("El curso no puede ser nulo");
        }
        if (alumno == null) {
            throw new IllegalArgumentException("El alumno no puede ser nulo");
        }

        if (!validadorCurp.esValida(alumno.curp())) {
            throw new IllegalArgumentException("La CURP del alumno no es válida: " + alumno.curp());
        }

        Alumno alumnoPersistido = alumno;
        if (!alumnoRepository.existsByCurp(alumno.curp())) {
            alumnoPersistido = alumnoRepository.save(alumno);
        }

        curso.inscribir(alumnoPersistido);

        String mensaje = "Hola " + alumnoPersistido.nombre() + ", tu inscripción al curso " +
                curso.getNombre() + " fue exitosa.";
        notificacionService.enviarCorreoBienvenida(alumnoPersistido.email(), mensaje);

        if (curso.getCupoDisponible() <= 2) {
            notificacionService.enviarAlertaAdministrador(
                    "Alerta Cupo Bajo",
                    "El curso " + curso.getNombre() + " tiene solo " + curso.getCupoDisponible() + " lugares disponibles."
            );
        }

        String folio = "INS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new InscripcionResultado(true, folio, "Inscripción completada exitosamente", alumnoPersistido);
    }
}
