package processor;

import model.Transaccion;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class ProcesadorCSV {

     /*
      Recibe la ruta del archivo, lo lee línea por línea sin saturar la RAM,
      y devuelve una lista de objetos listos para serializar.
     */
    public List<Transaccion> procesarArchivo(Path rutaCSV) {
        System.out.println("\n[Procesador] Leyendo archivo: " + rutaCSV.getFileName());

        try (Stream<String> lineas = Files.lines(rutaCSV)) {

            return lineas
                    .map(this::convertirLineaATransaccion) // Convierte texto a Objeto
                    .toList();

        } catch (IOException e) {
            System.err.println("[Error] Falló la lectura del archivo: " + e.getMessage());
            return List.of();
        }
    }


    // Método auxiliar: Toma una línea de texto "TX1,CTA,150,APROBADA" y construye un record Transaccion.

    private Transaccion convertirLineaATransaccion(String linea) {
        String[] columnas = linea.split(",");

        return new Transaccion(
                columnas[0],                       // idTransaccion
                columnas[1],                       // cuentaOrigen
                Double.parseDouble(columnas[2]),   // monto
                columnas[3]                        // estado
        );
    }
}