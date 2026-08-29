# Threads.

## Java 1.0
Java contaba con dos formas de crear un hilo.
### **Thread** y **Runnable**
- **Forma 1:** Heredar de **Thread**.

    Se podía heredar de **java.lang.Thread** y sobreescribir el método **run()**. Sin embargo, al en Java solo poder heredar de una clase, ya no se podía heredar alguna lógica de negocios propia.
- **Forma 2:** Implementar **Runnable**.

    **Runnable** es una interfaz funcional, con el método **run()**, en donde se representa la tarea que se quiera ejecutar, así ya se podía separar la lógica de negocio del motor que la ejecuta.

```java
Runnable tareaAsincrona = () -> {
    System.out.println("Ejecutando tarea en: " + Thread.currentThread().getName());
};

Thread hilo1 = new Thread(tareaAsincrona);
Thread hilo2 = new Thread(tareaAsincrona);

hilo1.start();
hilo2.start();
```
### El Problema: Race Conditions

Los hilos de un mismo proceso comparten la memoria RAM, por lo tanto dos hilos pueden llegar a acceder a una misma variable y cada quien aplicarle su modificación sin darse cuenta del estado real de la variable.

Ejemplo:
```java
public class CuentaBancaria {
    private double saldo = 1000.0;

    public void retirar(double monto, String usuario) {

        if (saldo >= monto) {
            System.out.println(usuario + " verificó fondos. Saldo disponible.");

            // Simulamos que el Sistema Operativo pausa este hilo 
            // por unos milisegundos para darle CPU al otro hilo.
            try { Thread.sleep(50); } catch (InterruptedException e) {}
            
            saldo = saldo - monto;
            System.out.println(usuario + " retiró $" + monto + ". Nuevo saldo: $" + saldo);
        } else {
            System.out.println(usuario + ": Fondos insuficientes.");
        }
    }
}
```
¿Qué pasa realmente dentro de deste código?

1. **Hilo 1 (Cajero)**: Entra al método, lee el saldo (1000 >= 800). Entra al if.
2. **Context Switch**: El Sistema Operativo pausa al Hilo 1.
3. **Hilo 2 (App)**: Entra al método, lee el saldo en la RAM (¡que sigue siendo 1000!). Entra al if.
4. **Context Switch**: El SO reanuda al Hilo 1. Ejecuta la resta: 1000 - 800 = 200.
5. **Context Switch**: El SO reanuda al Hilo 2. Como ya había pasado el if, ejecuta ciegamente la resta usando el saldo actualizado por el hilo anterior: 200 - 800 = -600.

### Solución Primitiva: **Synchronized**
Java introdujo la palabra reservada **Synchronazied** para resolver este problema.

Tomando en cuenta que cada objeto tiene dentro de si un "monitor". Cuando le pones **Synchronized** a un método, obligas a que el hilo bloqueé la entrada al bloque del método mientras él se encuentre dentro.

Ejemplo:
```java
public class CuentaBancariaSegura {
    private double saldo = 1000.0;

    public synchronized void retirar(double monto, String usuario) {
        if (saldo >= monto) {
            try { Thread.sleep(50); } catch (InterruptedException e) {}
            saldo = saldo - monto;
            System.out.println(usuario + " retiró $" + monto + ". Saldo: $" + saldo);
        } else {
            System.out.println(usuario + ": Fondos insuficientes.");
        }
    }
}
```

Sin embargo, ahora surge un nuevo problema. Estando un hilo dentro de un método Synchronized, se bloquea la entrada de alguien más pero las peticiones siguen llegando, todas estas acumulandose esperando poder entrar.

Además, instanciar un **new Thread()** por cada petición que llegaba al servidor web provocaba que, bajo tráfico intenso, el servidor colapsara por falta de memoria (OutOfMemoryError) antes de poder atender a todos.

## Java 5 y los Thread Pools
En 2004 con Java 5.0, introdujeron el framework de **java.util.concurrent**. Esto cambiaba de crear hilos manualmente a delegar esa responsabilidad..

### Thread Pool.
En lugar de crear un hilo por cada tarea y destruirlo al terminar, creamos un **Thread Pool**. Esto se puede entender como tener un número fijo de trabajadores, i creas un pool de 10 hilos y te llegan 1,000 peticiones web, esos 10 hilos toman las primeras 10 peticiones. Las 990 restantes se forman en una cola (Queue). Conforme un hilo termina su tarea, no se destruye, sino que voltea a la cola, toma la siguiente petición y la procesa.
- Reutilización infinita de los hilos
- Consumo de memoria del servidor estable y predecible.

### ExecutorService
Con la interfaz **ExecutorService**, ya no importa cómo se arranca el hilo, solo le "avientas" tareas al ejecutor.
```java
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolEjemplo {
    public static void main(String[] args) {
        // Creamos nuestro Thread Pool fijo de 3 hilos
        ExecutorService ejecutor = Executors.newFixedThreadPool(3);

        // Simulamos la llegada de 10 peticiones a nuestra API
        for (int i = 1; i <= 10; i++) {
            final int numeroPeticion = i;
            
            // Le delegamos la tarea al pool. No hacemos 'new Thread()'
            ejecutor.submit(() -> {
                System.out.println("Procesando petición " + numeroPeticion + 
                                   " en: " + Thread.currentThread().getName());
                try { Thread.sleep(500); } catch (InterruptedException e) {}
            });
        }
        ejecutor.shutdown();
    }
}
```
### **Callable** y **Future**.
Hasta ahora, las tareas saban **Runnable**, cuyo método **run()** retorna **void**. Si se manda el hilo a consumir un API externa, de la cual ocupas traer de regreso información, **Runnable** no podía hacer esto.

Java 5 introdujo **Callable**. Es idéntico a **Runnable**, pero este sí retorna un valor y puede lanzar excepciones. Pero existe otro problema temporal, al lanzar un hilo a realizar una petición ya sea a una API o Base de Datos, este debe esperar la respuesta, sin embargo, el programa principal necesita seguir avanzando. ¿Cómo recuperamos ese valor en el futuro?

#### La respuesta es la interfaz **Future**.
Cuando envías un **Callable** al Thread Pool, el ejecutor te devuelve instantáneamente un objeto **Future**.
- Es literalmente un "ticket de tintorería". Te dice: "Aún no tengo tu resultado, pero llévate este ticket. Haz otras cosas, y cuando necesites el dato, me das el ticket y te entrego el resultado".
```java
import java.util.concurrent.*;

public class FutureEjemplo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService ejecutor = Executors.newFixedThreadPool(2);

        // Callable: Tarea que SÍ devuelve algo (un String en este caso)
        Callable<String> consultaApi = () -> {
            Thread.sleep(2000); // Simulamos latencia de red de 2 segundos
            return "Respuesta exitosa del servidor externo";
        };

        System.out.println("Enviando petición a la API...");
        // submit() no espera. Te devuelve el "ticket" (Future) instantáneamente.
        Future<String> ticket = ejecutor.submit(consultaApi);

        System.out.println("Haciendo otras cosas en el programa principal mientras tanto...");

        // Aquí usamos el ticket para pedir el resultado.
        System.out.println("Cobrando el ticket...");
        String resultado = ticket.get();
        
        System.out.println("Dato recibido: " + resultado);
        ejecutor.shutdown();
    }
}
```
### Nuevo Cuello de Botella
Había un defecto, el método **.get()** era bloqueante, si tú pedías el resultado y el hilo secundario aún no había terminado, el hilo principal el programa se *bloqueaba* en esa línea del código hasta que llegara la respuesta.

Si querías encadenar tareas (ej: Traer un usuario de PostgreSQL -> Luego usar su ID para traer sus permisos de Redis -> Luego consultar una API externa), terminabas con una cadena de hilos bloqueándose mutuamente esperando los **.get()** de los demás.

## Java 8 y **CompletableFuture**
Llegamos a 2014, las arquitecturas monolíticas estaban cambiando a arquitecturas de microservicios. Dentro de esta nueva arquitectura, todo se basa en peticiones, por lo tanto, no era conveniente seguir usando el antiguo **.get()** ya que aunque se estuvieran usando hilo secundarios, se seguía desperdiciando el tiempo esperando al terminado de una petición.

Java se inspiró en las *Promesas* de lenguajes reactivos como JavaScript y creó una clase muy poderosa del lenguaje: **CompletableFuture**

### Nuevo Paradigma: Estilo Declarativo
Con **CompletableFuture**, ya no le pides el resultado al "ticket" usando la fuerza bruta. En su lugar, le adjuntas instrucciones.
- Le dices a Java: *"Inicia esta tarea en segundo plano. No me voy a quedar a esperar. Cuando termines, toma el resultado y pásaselo a esta otra función. Y cuando esa termine, guarda el dato."*

El hilo principal pasa por esas líneas de código en 1 milisegundo, delega todo el árbol de tareas, y queda libre inmediatamente para atender a otros usuarios.

### CompletableFuture.
Existen tres operaciones fundamentales:
- **supplyAsyn()**: Inica esta tarea en un hilo aparte y devuelveme algo
- **thenApply()**: Cuando la tarea anterior termine, toma su resultado, modifícalo/procesalo, y devuelve algo nuevo
- **thenAccept()**: Cuando lo anterior termine, toma el resultado y consúmelo, pero no devuelvas nada -> Imprimelo en consola, envía un mensaje, etc.

Ejemplo:
```java
import java.util.concurrent.CompletableFuture;

public class AsyncBankingFlow {
    public static void main(String[] args) {
        System.out.println("1. [Hilo Principal] Recibiendo petición de transferencia...");

        // Iniciamos la cadena asíncrona
        CompletableFuture.supplyAsync(() -> {
            System.out.println("2. [" + Thread.currentThread().getName() + "] Consultando PostgreSQL...");
            simularRetardo(1000);
            return "Usuario_Validado_BD"; // Resultado de la Fase 1
            
        }).thenApply(usuarioBD -> {
            System.out.println("3. [" + Thread.currentThread().getName() + "] Buscando perfil de fraude en Redis para: " + usuarioBD);
            simularRetardo(500);
            return usuarioBD + "_SinRiesgo"; // Resultado de la Fase 2
            
        }).thenAccept(resultadoFinal -> {
            System.out.println("4. [" + Thread.currentThread().getName() + "] Publicando evento en RabbitMQ: " + resultadoFinal);
            // Fin de la cadena, no retorna nada
        });

        System.out.println("5. [Hilo Principal] Flujo delegado. El hilo principal está libre y responde HTTP 202 Accepted.");

        // (Solo para evitar que el programa termine antes que los hilos)
        simularRetardo(3000); 
    }

    private static void simularRetardo(int millis) {
        try { Thread.sleep(millis); } catch (InterruptedException e) {}
    }
}
```
Resultado: 
```bash
1. [Hilo Principal] Recibiendo petición de transferencia...
5. [Hilo Principal] Flujo delegado. El hilo principal está libre y responde HTTP 202 Accepted.
2. [ForkJoinPool.commonPool-worker-1] Consultando PostgreSQL...
3. [ForkJoinPool.commonPool-worker-1] Buscando perfil de fraude en Redis para: Usuario_Validado_BD
4. [ForkJoinPool.commonPool-worker-1] Publicando evento en RabbitMQ: Usuario_Validado_BD_SinRiesgo
```
### El Problema de la Era Asíncrona
**CompletableFuture** es increíblemente eficiente a nivel de CPU. Pero, tiene un costo alto: la curva de aprendizaje y la legibilidad.
El código asíncrono es difícil de depurar (hacer debug saltando de un hilo a otro es muy difícil). Cuando tienes flujos complejos con 10 ramificaciones condicionales, el código lleno de .thenApply(), .thenCompose() y .handle() se vuelve un desastre que a los desarrolladores les cuesta mucho mantener.

La industria buscaba por una forma de escribir código como en la Era Manual (línea por línea, fácil de leer), pero con el rendimiento altísimo de la Era Asíncrona.

Y eso es lo que acaba de revolucionar al mundo de Java con el proyecto Loom.

## Java 21+ y lo Hilos Virtuales
Es probablemente el cambio arquitectónico más grande que ha tenido la Máquina Virtual de Java (JVM) en sus más de 25 años de historia: **El Proyecto Loom y los Hilos Virtuales**.

### Virtual Threads
En Java 21, la JVM dejó de delegarle la creación de hilos al Sistema Operativo. En su lugar, creó una capa intermedia: los **Hilos Virtuales**.

#### ¿Cómo funcionan?
La JVM mantiene un Thread Pool muy pequeño de hilos reales del Sistema Operativo (*Carrier Threads*). Suele tener tantos hilos portadores como núcleos físicos tenga tu CPU.

Tú, en tu código, puedes crear 1 millón de Hilos Virtuales. Estos hilos son tan ligeros que solo ocupan unos pocos bytes en la memoria RAM.

Cuando un Hilo Virtual tiene que ejecutar código, se "monta" sobre un Carrier Thread físico.
Aquí viene la magia: Si el Hilo Virtual necesita consultar la base de datos y va a tener que esperar 200 milisegundos por la respuesta (una operación bloqueante), la JVM detecta esto y "desmonta" al Hilo Virtual instantáneamente. Lo saca del núcleo físico y lo guarda en la RAM. El núcleo físico queda libre en nanosegundos para montar otro Hilo Virtual y procesar la petición de otro cliente.

Cuando la base de datos responde, la JVM despierta al Hilo Virtual original, lo vuelve a montar en cualquier núcleo físico disponible, y el código continúa exactamente donde se quedó.

Ejemplo:
```java
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import java.time.Duration;
import java.time.Instant;

public class VirtualThreadsDemo {
    public static void main(String[] args) {
        System.out.println("Iniciando prueba de carga masiva...");
        Instant inicio = Instant.now();

        // Creamos un Hilo Virtual nuevo por cada tarea
        try (var ejecutor = Executors.newVirtualThreadPerTaskExecutor()) {
            
            // Simulamos 100,000 peticiones concurrentes
            IntStream.range(0, 100_000).forEach(i -> {
                
                ejecutor.submit(() -> {
                    // Escribimos código bloqueante normal (estilo Java 1.0)
                    // Con Hilos Virtuales, Thread.sleep es cuando se "desmonta" el hilo de la CPU.
                    try {
                        Thread.sleep(1000); 
                    } catch (InterruptedException e) {}
                    
                    return i;
                });
                
            });
            // El bloque try-with-resources se encarga de esperar automáticamente 
            // a que las 100,000 tareas terminen antes de continuar.
        }

        Instant fin = Instant.now();
        System.out.println("100,000 tareas procesadas en: " + Duration.between(inicio, fin).toMillis() + " ms");
    }
}
```

Hoy, con **Spring Boot 3.2+** y **Java 21+**, simplemente habilitas los hilos virtuales (**spring.threads.virtual.enabled=true** en  **application.properties**), se escribe código lineal, síncrono y fácil de entender, y Spring Boot asignará un Hilo Virtual por cada petición HTTP. Tienes el máximo rendimiento del hardware con la sintaxis más sencilla.

### Los Límites.
**1. Tareas de Espera (I/O) vs Tareas de Cálculo (CPU)**    
- **I/O Bound (Ideal para Hilos Virtuales)**: Si tu hilo va a la base de datos, llama a una API externa, o lee un archivo, el hilo pasa el 99% de su vida esperando. Aquí los Hilos Virtuales brillan. Como están esperando, se "desmontan" del procesador físico y no consumen CPU. Para este escenario, sí, puedes lanzar 100,000 hilos virtuales sin problema.

- **CPU Bound (Terrible para Hilos Virtuales)**: Si tu hilo tiene que hacer cálculos matemáticos intensivos (por ejemplo, encriptar miles de contraseñas con algoritmos pesados, procesar video, o minar criptomonedas), el hilo nunca espera. Necesita usar el procesador físico al 100%.

**2. Cuellos de Botella Externos**
1. Creas un millon de Virtual Threads, cada uno intenta hacer consulta a una base de datos relacional al mismo tiempo.
2. Las bases de datos tienen un límite de conexiones simultáneas (Connection Pool), que rondan entre 100 y 500 conexiones.
3. De nada sirve tener infinitos hilos virtuales si los vas a amontonar en un embudo de 100 conexiones de base de datos.

**3. Pinning**

Actualmente, los Hilos Virtuales tienen un defecto técnico con esa palabra. Si un Hilo Virtual entra a un bloque de código **synchronized**, se queda pegado (pinned) al núcleo físico del procesador -> Pierde su superpoder de "desmontarse"

