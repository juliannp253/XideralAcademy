package watcher;

import processor.ProcesadorCSV;
import util.SerializadorJson;
import model.Transaccion;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

public class DirectorioWatcher {

    private final Path inboxDir = Path.of("inbox");
    private final ProcesadorCSV procesador;
    private final SerializadorJson serializador;

    public DirectorioWatcher() {
        this.procesador = new ProcesadorCSV();
        this.serializador = new SerializadorJson();
    }

    public void iniciarVigilancia() {
        try {
            Files.createDirectories(inboxDir);
        } catch (IOException e) {
            System.err.println("No se pudo crear directorio inbox: " + e.getMessage());
            return;
        }

        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {

            inboxDir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
            System.out.println("[Daemon] Iniciando vigilancia en la carpeta: " + inboxDir.toAbsolutePath());
            System.out.println("   (Esperando que se suelte un archivo .csv en 'inbox/')\n");

            while (true) {
                WatchKey key;
                try {
                    key = watchService.take();
                } catch (InterruptedException e) {
                    System.err.println("Vigilancia interrumpida.");
                    break;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    if (kind == StandardWatchEventKinds.OVERFLOW) continue;

                    Path nombreArchivo = (Path) event.context();
                    Path rutaCompleta = inboxDir.resolve(nombreArchivo);

                    if (rutaCompleta.toString().endsWith(".csv")) {
                        System.out.println("[Daemon] Nuevo archivo detectado: " + nombreArchivo);

                        List<Transaccion> transacciones = procesador.procesarArchivo(rutaCompleta);

                        serializador.procesarYGuardar(transacciones, rutaCompleta);

                        System.out.println("[Daemon] Ciclo completado. Esperando nuevos archivos...\n");
                    }
                }

                if (!key.reset()) {
                    System.err.println("Directorio no disponible (quizás fue borrado). Saliendo...");
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}