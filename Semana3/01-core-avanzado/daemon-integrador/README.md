# Daemon Integrador de Transacciones (NIO.2 + Serialización Jackson)

Este proyecto es la puesta en práctica de los conceptos aprendidos sobre **Java I/O, NIO.2, Programación Funcional con Streams y Serialización Enterprise con Jackson**, explicados detalladamente en [serialization.md](../serialization.md).

Se trata de un **Daemon / Servicio en segundo plano** que vigila continuamente un directorio de entrada (`inbox/`), procesa archivos de transacciones en formato `.csv` de forma eficiente en memoria, los serializa a formato `.json` legible en un directorio de salida (`outbox/`), y finalmente traslada el archivo original a una carpeta de respaldo (`backup/`).

---

## 🏗️ Flujo de Arquitectura del Daemon

```
                ┌────────────────────────────────────────────────────────┐
                │        DirectorioWatcher (I/O basado en Eventos)       │
                │        Delegado al Kernel del SO vía WatchService      │
                └──────────────────────────┬─────────────────────────────┘
                                           │ Detecta nuevo .csv
                                           ▼
┌──────────────────┐            ┌──────────────────────┐            ┌──────────────────┐
│     inbox/       │ ─────────> │    ProcesadorCSV     │ ─────────> │   List<Record>   │
│ (Archivo origen) │            │ (Files.lines Lazy)   │            │  (Transacciones) │
└──────────────────┘            └──────────────────────┘            └─────────┬────────┘
                                                                              │
                                ┌─────────────────────────────────────────────┘
                                ▼
                   ┌──────────────────────────┐
                   │     SerializadorJson     │
                   │    (Jackson + NIO.2)     │
                   └──────┬──────────────────┬┘
                          │                  │
               Genera JSON│                  │ Mueve archivo original
                          ▼                  ▼
                 ┌─────────────────┐   ┌─────────────────┐
                 │     outbox/     │   │     backup/     │
                 │ (.json formato) │   │ (.csv auditado) │
                 └─────────────────┘   └─────────────────┘
```

---

## 🎯 ¿Cómo y dónde se pone a prueba lo visto?

### 1. WatchService: I/O Orientado a Eventos
* **Concepto**: En lugar de hacer *polling* activo (consumiendo CPU y recursos preguntando constantemente si hay archivos), se delega la vigilancia al kernel del Sistema Operativo mediante eventos del filesystem.
* **Dónde se prueba**: En [`src/watcher/DirectorioWatcher.java`](src/watcher/DirectorioWatcher.java).
* **En el código**:
  - **Creación y registro**: Se obtiene el servicio con `FileSystems.getDefault().newWatchService()` y se registra el directorio `inboxDir` para escuchar únicamente eventos de creación con `StandardWatchEventKinds.ENTRY_CREATE` (Línea 32).
  - **Bloqueo eficiente**: `watchService.take()` suspende el hilo hasta que el sistema operativo emite una señal de evento, sin gastar ciclos de procesador innecesarios.
  - **Ciclo de eventos y reinicio de llave**: Se iteran los eventos con `key.pollEvents()` descartando desbordamientos (`OVERFLOW`), y se reestablece la llave con `key.reset()` (Línea 63) para continuar en escucha activa.

---

### 2. NIO.2: Separación de Responsabilidades (`Path` vs `Files`)
* **Concepto**: Java 7 solucionó las deficiencias de `java.io.File` (violación de SRP, fallos silenciosos devolviendo `false`, falta de soporte de metadatos) separando la representación de rutas (`Path`) de las operaciones sobre el disco duro (`Files` con excepciones explícitas).
* **Dónde se prueba**:
  - [`src/watcher/DirectorioWatcher.java`](src/watcher/DirectorioWatcher.java)
  - [`src/util/SerializadorJson.java`](src/util/SerializadorJson.java)
  - [`src/processor/ProcesadorCSV.java`](src/processor/ProcesadorCSV.java)
* **En el código**:
  - **Ubicaciones inmutables (`Path`)**: Se usan `Path.of("inbox")`, `Path.of("outbox")`, `Path.of("backup")` y métodos no mutantes como `.resolve()` y `.getFileName()` para componer rutas seguras sin tocar el disco.
  - **Operaciones atómicas y seguras (`Files`)**:
    - Creación segura de directorios: `Files.createDirectories(...)`.
    - Movimiento atómico de archivos con sobrescritura: `Files.move(archivoOriginal, rutaBackup, StandardCopyOption.REPLACE_EXISTING)` (Línea 40 en `SerializadorJson.java`).
    - Manejo estricto de excepciones `IOException` en lugar de valores booleanos engañosos.

---

### 3. Fuga de Recursos y Lectura Eficiente (`try-with-resources` + `Files.lines`)
* **Concepto**: Evitar la fuga de descriptores de archivo (*"Too many open files"*) mediante `AutoCloseable` y evitar saturar la memoria RAM (*"OutOfMemoryError"*) en archivos medianos/grandes mediante lectura perezosa (*lazy evaluation*).
* **Dónde se prueba**: En [`src/processor/ProcesadorCSV.java`](src/processor/ProcesadorCSV.java).
* **En el código**:
  - **Cierre garantizado con `AutoCloseable`**: Se implementa `try-with-resources` sobre el flujo del archivo (Línea 20):
    ```java
    try (Stream<String> lineas = Files.lines(rutaCSV)) {
        return lineas
                .map(this::convertirLineaATransaccion)
                .toList();
    }
    ```
  - **Lectura por streaming**: `Files.lines(rutaCSV)` procesa línea a línea bajo demanda conectando directamente el pipeline funcional (`.map(...)`) sin cargar el archivo completo de golpe en memoria como lo haría un simple `Files.readString()`.

---

### 4. Serialización Moderna a JSON con Jackson
* **Concepto**: En la industria enterprise (ej. microservicios y Spring Boot), la serialización nativa en bytes de Java fue desplazada por serialización basada en texto JSON legible, seguro e interoperable utilizando la librería **Jackson**.
* **Dónde se prueba**:
  - [`src/model/Transaccion.java`](src/model/Transaccion.java)
  - [`src/util/SerializadorJson.java`](src/util/SerializadorJson.java)
* **En el código**:
  - **Modelo Inmutable**: `Transaccion` es un `record` de Java, ideal para transferir datos de forma inmutable.
  - **Jackson ObjectMapper**: Se inicializa `ObjectMapper` y se habilita la indentación legible (*pretty-printing*) con:
    ```java
    this.mapper = new ObjectMapper();
    this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
    ```
  - **Escritura del payload**: `mapper.writeValue(rutaSalida.toFile(), transacciones)` convierte la lista de objetos vivos en memoria a un archivo estructurado `.json` dentro de `outbox/`.

---

## 📁 Estructura del Proyecto

```text
daemon-integrador/
├── pom.xml                               # Configuración de Maven y dependencia de Jackson Databind
├── inbox/                                # Directorio vigilado por el Daemon (donde caen los CSVs)
├── outbox/                               # Directorio de salida con los JSONs serializados
├── backup/                               # Histórico donde se mueven los CSVs procesados
└── src/
    ├── Main.java                         # Punto de entrada de la aplicación
    ├── model/
    │   └── Transaccion.java              # Java Record (DTO inmutable para serialización)
    ├── processor/
    │   └── ProcesadorCSV.java            # Lectura con Files.lines() y transformación funcional
    ├── util/
    │   └── SerializadorJson.java         # Serialización con Jackson ObjectMapper y gestión con Files.move()
    └── watcher/
        └── DirectorioWatcher.java        # Motor del Daemon basado en NIO.2 WatchService
```

---

## 🚀 Guía de Ejecución y Prueba

### 1. Compilar el proyecto
Desde la raíz de `daemon-integrador`:
```bash
mvn clean compile
```

### 2. Iniciar el Daemon
Ejecutar la clase principal [`Main`](src/Main.java):
```bash
mvn exec:java -Dexec.mainClass="Main"
```
Verás un mensaje en consola indicando que el Daemon está en espera de archivos:
```text
=== DAEMON INTEGRADOR EMPRESARIAL INICIADO ===
[Daemon] Iniciando vigilancia en la carpeta: /.../daemon-integrador/inbox
   (Esperando que se suelte un archivo .csv en 'inbox/')
```

### 3. Probar el procesamiento en caliente
Copia o crea un archivo `.csv` (por ejemplo `transacciones_dia1.csv`) dentro del directorio `inbox/` con el siguiente contenido:

```csv
TX1001,CUENTA-5544,1500.50,APROBADA
TX1002,CUENTA-8899,320.00,RECHAZADA
TX1003,CUENTA-1122,8950.00,APROBADA
TX1004,CUENTA-3344,15.99,PENDIENTE
```

### 4. Resultado esperado
1. **Consola**: El `WatchService` detectará el evento `ENTRY_CREATE`, `ProcesadorCSV` leerá las líneas con Streams, `SerializadorJson` generará el JSON y moverá el archivo.
2. **`outbox/`**: Contendrá `transacciones_dia1.json` formateado con Jackson.
3. **`backup/`**: Contendrá el archivo `transacciones_dia1.csv` trasladado y preservado.
4. **`inbox/`**: Quedará limpio, listo para el siguiente lote.
