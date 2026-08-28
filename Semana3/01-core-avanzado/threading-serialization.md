# Serialization

## Fundamentos del Sistema de Archivos (File vs Path)

### Java 1.0 (java.io.File)

En el inicio Java introdujo la clase **File**. Su objetivo era representar un archivo o directorio. Un objeto **File** representaba una ruta en el disco duro, podría ser un **documento.txt** o un directorio **/usuarios/home**.

#### **¿Cuál era el problema?**  
Esta clase **File** intentaba hacer dos cosas completamente distintas al mismo tiempo (no seguía el Single Responsability Principle).
- Representaba la ruta
- Ejecutaba acciones en el disco duro: **file.delete()**, **file.createNewFile()**, etc.
#### **Consecuencias**
- **Fallos silenciosos**: Si un **file.delete()** fallaba, el método solo devolvía un simple **false**. No se lanzaban excepciones, y esto hacía más difícil encontrar el error al momento de querer debugear.
- **Problemas con Enlaces simbólicos**: Si un archivo era un "acceso directo" (symlink), **File** se confundía y realizaba las operaciones sobre el acceso directo en lugar del archivo original.
- **Falta de Metadatos**: En Linux/Unix, como los archivos tienen dueños, grupos y permisos, **File** no tenía forma de ller o modificar esa información dentro de esos SO.

### Resolución: NIO.2 (Java 7+)
Se decidió que **File** no tenía solución. En Java 7 se introdujo **NIO2** (New I/O 2) y se separan las responsabilidades. Todo esto en un nuevo paquete: **java.nio.file**.

Aparecen dos integrantes:
- **Path**: Interfaz abstracta. Solo representa una ubicación. No toca el disco duro
- **Files**: Es una clase con métodos estáticos que toma un **Path** y realiza 

#### **¿Cómo resolvieron los problemas?**
- Los fallos ahora lanzan excepciones, como por ejemplo **NoSuchFileException**, **DirectoryNotEmptyException** o **AccessDeniedException**.
- Con **Files.readAttributes()**, Java ahora sí puede leer fechas exactas de creación y permisos tanto en Windows, Linux o Mac.

## Introducción a I/O Streams.

**Stream**: secuencia de datos que viaja desde un Source hacia un Destination. Flujo en una sola dirección y de manera secuencial 

### Byte Streams (Java 1.0)
Java introdujo los Byte Streams para manejar todo el flujo de datos. Aparecieron dos clases abstractas:
- **InputStream**: Para leer bytes
- **OutputStream**: Para escribir bytes
#### Problema.
- Los Byte Streams leen la infromación de 1 byte en 1 byte.
- Se enfocaba en texto en inglés, pero la web creció y la necesidad de usar nuevos carácteres como "ñ" o acentos, cuyos carácteres ocupan de 2 a 4 bytes.
- "Año" -> "Ao": palabras eran cortadas al leerlas.

### Character Streams (Java 1.1)
Java 1.1 introdujo una los **Character Streams** enfocados en texto humano. Aparecieron los clases abstractas padres:
- **Reader**: Para leer texto
- **Writer**: Para escribir texto
#### ¿Cómo solucionó el problema?
Un **Reader** no lee byte por byte, entiende codificaciones como UTF-8. Lee bytes necesarios, los traduce internamente (mediante tabla de codificación), y entrega un carácter válido (16 bits)

Ahora su uso se podía resumir en:
- Procesar imagenes, PDF, un binario: Usa **Byte Streams**
- Leer archivo .txt, .csv, o un .json: Usa **Character Streams**

### Patrón Decorator
Java I/O es un ejemplo del **Patrón de Diseño Decorator**. En lugar de crear una clase gigante que haga todo, Java te da clases "Bases" que hacen el trabajo sucio, y clases "Decoradoras" que envuelven a la base para darle agregarle un nuevo comportamiento. 

Ejemplo al leer un archivo de texto:
- **Base**: Usamos **FileReader**. Sin embargo esta clase es ineficiente ya que por cada letra que lee, va al disco duro.
- **Decorador**: Lo podemos envolver en un **BufferReader**. Este, va al disco duro una sola vez, lee el bloque de datos y lo guarda en RAM. Cuando el programa pide leer la siguiente línea de texto, el BufferReader la entrega desde la RAM instantáneamente.
```java
FileReader lectorBase = new FileReader("datos.txt");
BufferReader lectorPoderoso = new BufferReader(lectorBase);
```
### Fuga de Recurso (pre-Java 7).
Al abrir un Stream, si olvidabas cerrarlo con **.close()**, o ocurría un problema a mitad de lectura, el archivo se quedaba abierto. Después de un tiempo el servidor podía crashear por el error: **Too many open files**.

Para evitar esto, se tenía que usar los bloques **try-catch-finally**, demasiado código.
```java
BufferedReader br = null;
try {
    br = new BufferedReader(new FileReader("archivo.txt"));
    // leer datos...
} catch (IOException e) {
    e.printStackTrace();
} finally {
    if (br != null) {
        try {
            br.close(); // ¡Obligatorio!
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
```
### Evolución (Java 7+).
Java 7 introdujo un cierre automático de recursos para cualquier clase que implemente la interfaz de **AutoCloseable**.
```java
try (BufferedReader br = new BufferedReader(new FileReader("archivo.txt"))) {
    // leer datos...
} catch (IOException e) {
    System.err.println("Error: " + e.getMessage());
} 
```
## Lectura y Escritura de Archivos.

### 1. Archivos pequeños
Si con certeza es un archivo pequeño, no es necesario implemtar Buffers ni try-with-resources manualmente. Java 11 introdujo métodos en la clase **Files** que hacen el trabajo de abrir, leer todo, y cerrar el recurso.

***Si se usa esto en archivos grandes se tendrá un OutOfMemoryError***
```java
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

public class TinyFileIO {
    public static void main(String[] args) {
        Path ruta = Path.of("/app/config/settings.json");
        Path rutaSalida = Path.of("/app/config/backup.json");

        try {
            String contenido = Files.readString(ruta);
            
            System.out.println("Configuración cargada: " + contenido);

            Files.writeString(rutaSalida, contenido);
            
        } catch (IOException e) {
            System.err.println("Error procesando archivo: " + e.getMessage());
        }
    }
}
```
### 2. Archivos Medianos a Grandes
Ejemplo procesar un reporte CSV o escribir transacciones linea por linea. Usamos **BufferReader** o **BufferWriter** junto con un try-with-resources. La clase **Files** de NIO.2 nos da métodos de fábrica para conectar el **Path** moderno con los Streams tradicionales.
```java
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

public class StandardBufferedIO {
    public static void main(String[] args) {
        Path rutaEntrada = Path.of("/app/data/transacciones_hoy.csv");
        Path rutaSalida = Path.of("/app/data/transacciones_auditadas.csv");

        try (
            BufferedReader lector = Files.newBufferedReader(rutaEntrada);
            BufferedWriter escritor = Files.newBufferedWriter(rutaSalida, 
                                        StandardOpenOption.CREATE, 
                                        StandardOpenOption.APPEND)
        ) {
            String linea;

            while ((linea = lector.readLine()) != null) {
                if (linea.contains("ERROR")) {
                    escritor.write(linea);
                    escritor.newLine(); 
                }
            }
            System.out.println("Auditoría finalizada.");

        } catch (IOException e) {
            System.err.println("Fallo en I/O: " + e.getMessage());
        }
    }
}
```
### 3. Archivos Gigantes
Ejemplo quieres buscar una IP específica dentro de un log de servidor que pesa 5 GB, pero solo hay 512 MB de RAM. Java 8 introdujo **Files.lines(Path)**. No usa ciclos **while**, devuelve un **stream** (de Programación Funcional API) que se evalúa de forma perezosa.

Carga solo una línea en memoria, la procesa, la descarta y trae la siguiente.
```java
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.util.stream.Stream;

public class GiganticFileIO {
    public static void main(String[] args) {
        Path serverLog = Path.of("/var/log/aplicacion.log");

        try (Stream<String> lineas = Files.lines(serverLog)) {
            
            boolean hackeoDetectado = lineas
                .filter(linea -> linea.contains("192.168.1.100")) 
                .anyMatch(linea -> linea.contains("SQL INJECTION"));
                
            if (hackeoDetectado) {
                System.out.println("¡Alerta de seguridad disparada!");
            }
            
        } catch (IOException e) {
            System.err.println("No se pudo leer el log: " + e.getMessage());
        }
    }
}
```

## Serialización.
Transformar un objeto a bytes se llama **Serialización**, esto para poder ser transportado a través de la red.

### El estándar de la Industria: JSON y Jackson.

Para resolver los problemas de seguridad e interoperabilidad, el mundo enterprise migró a la Serialización basada en texto, siendo **JSON** el rey absoluto.

Cuando trabajas con un framework robusto como Spring Boot y devuelves un objeto Java en un Controller, Spring usa por debajo una librería llamada **Jackson**.

Jackson toma tu objeto vivo en RAM y lo serializa, genera un texto JSON plano y legible que viaja por la red (como Character Streams), y puede ser deserializado de forma segura por cualquier lenguaje.

## I/O y NIO.2
Los Streams tradicionales de java.io son bloqueantes (Blocking I/O). Para resolver esto en aplicaciones de alto rendimiento, aparecieron dos conceptos clave.

### Channels y Buffers (NIO).
- **Channel**: Es una conexión abierta a un archivo o a un socket de red bidireccional. Lo que significa que puedes leer y escribir en el mismo canal.
- **Buffer**: Los canales no leen letra por letra. Leen bloques enteros de memoria a la vez, manipulando bytes directamente a nivel de hardware. 

### Árboles de Archivos.
Antes de NIO.2, tenías que escribir complejos métodos recursivos manuales. Ahora, usamos el poder de los Streams funcionales de Java 8 combinados con **Files.walk()**.
```java
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.util.stream.Stream;

public class FileTreeExplorer {
    public static void main(String[] args) {
        Path directorioRaiz = Path.of("/app/usuarios");

        try (Stream<Path> rutas = Files.walk(directorioRaiz)) {
            
            rutas.filter(Files::isRegularFile)
                 .filter(path -> path.toString().endsWith(".png")) 
                 .filter(path -> {
                     try {
                         return Files.size(path) > 2000000;
                     } catch (IOException e) {
                         return false;
                     }
                 })
                 .forEach(path -> System.out.println("Encontrado para optimizar: " + path));
                 
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```
### WatchService (I/O Orientado a Eventos).
WatchService es una API de Java NIO.2 para observar cambios en el sistema de archivos de forma basada en eventos, evitando tener que consultar constantemente el estado de una carpeta mediante polling.

En lugar de forzar a Java a preguntar constantemente, WatchService delega el trabajo pesado al núcleo (kernel) del Sistema Operativo. Esto es arquitectura orientada a eventos.
```java
import java.nio.file.*;
import java.io.IOException;

public class DirectorioWatcher {
    public static void main(String[] args) {
        Path rutaAVigilar = Path.of("/data/pagos");

        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            
            rutaAVigilar.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
            
            System.out.println("Microservicio en espera. Vigilando la carpeta...");

            while (true) {
                WatchKey key = watchService.take();
                
                for (WatchEvent<?> event : key.pollEvents()) {
                    System.out.println("¡Nuevo archivo detectado para procesar!: " + event.context());
                }
                
                boolean valid = key.reset();
                if (!valid) {
                    break;
                }
            }
            
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```

Puedes ver puesto a prueba este tema en la siguiente ruta con un proyecto: **/daemon-integrador/..**