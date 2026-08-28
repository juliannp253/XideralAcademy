package util;

import model.Transaccion;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class SerializadorJson {

    private final ObjectMapper mapper;

    private final Path directorioOutbox = Path.of("outbox");
    private final Path directorioBackup = Path.of("backup");

    public SerializadorJson() {
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public void procesarYGuardar(List<Transaccion> transacciones, Path archivoOriginal) {
        if (transacciones.isEmpty()) return;

        String nombreOriginal = archivoOriginal.getFileName().toString();
        String nombreJson = nombreOriginal.replace(".csv", ".json");
        Path rutaSalida = directorioOutbox.resolve(nombreJson);

        try {
            Files.createDirectories(directorioOutbox);
            Files.createDirectories(directorioBackup);

            System.out.println("[Serializador] Escribiendo JSON en: " + rutaSalida);
            mapper.writeValue(rutaSalida.toFile(), transacciones);

            Path rutaBackup = directorioBackup.resolve(nombreOriginal);
            Files.move(archivoOriginal, rutaBackup, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("[Serializador] Limpieza: CSV movido a " + rutaBackup);

        } catch (IOException e) {
            System.err.println("[Error I/O] Falló la escritura o el movimiento: " + e.getMessage());
        }
    }
}