# WebFlux y Flux: Streaming de Datos y Operadores Reactivos

## ¿Qué es un `Flux` y por qué no basta con una `List<T>`?

En el proyecto anterior vimos que un `Mono` maneja de **0 a 1 elemento**. Sin embargo, en el mundo real muchas fuentes de información no entregan un solo dato cerrado, sino **una secuencia continua de múltiples datos que van ocurriendo a lo largo del tiempo** (por ejemplo: lecturas continuas de sensores IoT, cotizaciones de la bolsa de valores, mensajes en un chat o eventos de un servidor).

Para esto existe **`Flux<T>`**.

Un **`Flux<T>`** es un publicador reactivo (*Publisher*) de **Project Reactor** que representa una secuencia asíncrona de **0 a N elementos** (donde $N$ puede ser un número finito, o incluso una secuencia **infinita** que nunca termina).

```
Flux finito:    ────( onNext: 1 )────( onNext: 2 )────( onNext: 3 )────( onComplete )────>
Flux infinito:  ────( onNext: 1 )────( onNext: 2 )────( onNext: 3 )────( onNext: 4 )────> ...
Flux con error: ────( onNext: 1 )────( onNext: 2 )────( onError: Exception )─────────────>
```

### La gran limitación de `List<T>` frente a un `Flux`
En el desarrollo web tradicional siempre devolvíamos listas (`List<Employee>`, `List<Producto>`). ¿Por qué no podemos simplemente seguir usando listas?

1. **Una lista exige tener TODOS los datos listos antes de responder:** Una `List` es una estructura de datos en memoria con un tamaño definido. Para entregar una lista, el servidor tiene que recolectar todos los elementos primero. Si los datos tardan en generarse (por ejemplo, 1 dato por segundo), el cliente tiene que esperar congelado a que termine todo el proceso.
2. **Una lista no puede representar flujos infinitos:** Un sensor de temperatura que emite lecturas indefinidamente jamás termina. No puedes meter un flujo infinito dentro de una `List` porque jamás habría un punto final para entregarla y la memoria RAM terminaría explotando (*OutOfMemoryError*).

Un `Flux` resuelve esto: permite emitir y procesar **cada elemento en el instante exacto en que se genera**, sin esperar al resto.

---

## El Secreto del Streaming en la Web: `application/json` vs `text/event-stream`

Uno de los mayores dilemas al empezar con `Flux` es el siguiente:
> *"Si devuelvo un `Flux<Lectura>` en mi controlador y en el cliente recibo un arreglo JSON común y corriente... ¿de qué sirvió usar programación reactiva?"*

La respuesta está en el **tipo de contenido (*Content-Type*)** con el que servimos los datos.

### 1. El modo tradicional (`application/json`)
Cuando exponemos un `Flux` como JSON estándar:
* Por especificación, un arreglo JSON debe empezar con un corchete `[` y terminar con `]`.
* Para poder cerrar el corchete `]`, Spring WebFlux está obligado a **acumular internamente todos los elementos en memoria**, esperar a que el flujo termine (`onComplete`) y recién ahí mandar todo el bloque completo al cliente.
* **Resultado:** Si el flujo tarda 5 segundos en generar 5 datos, el cliente pasa 5 segundos en silencio total y recibe todo de golpe.

### 2. El modo Streaming con Server-Sent Events (`text/event-stream`)
Cuando exponemos el mismo `Flux` con el formato **`text/event-stream`** (SSE):
* Spring **no acumula nada en memoria**.
* La conexión HTTP se mantiene abierta entre el servidor y el cliente.
* En cuanto el publicador emite una señal `onNext`, Spring la serializa y la envía por el cable de inmediato en formato de evento:
  ```text
  data:{"numero":1,"celsius":28.5,"hora":"14:30:01"}

  data:{"numero":2,"celsius":30.7,"hora":"14:30:02"}
  ```
* **Resultado:** El cliente ve cómo los datos van "goteando" en vivo, segundo a segundo, en cuanto se producen.

---

## ¿Cómo está implementado en este proyecto?

Analicemos cómo funciona el código en el proyecto `02-webflux-flux`.

---

### 1. La Fuente de Datos Infinita (`SensorService.java`)

Para este proyecto simulamos un sensor de temperatura ambiental:

```java
@Service
public class SensorService {

    public static final Duration CADENCIA = Duration.ofSeconds(1);

    public Flux<Lectura> lecturas() {
        return Flux.interval(CADENCIA)       // Emite 0, 1, 2, 3... cada 1 segundo para siempre
                   .map(this::medir);        // Transforma cada número en una Lectura con temperatura
    }

    private Lectura medir(long n) {
        // Genera una curva sinusoidal de temperatura entre ~18°C y ~34°C en ciclos de 20 segundos
        double celsius = 26 + 8 * Math.sin(2 * Math.PI * n / 20.0);
        return new Lectura(n, "sensor-A", Math.round(celsius * 10) / 10.0, LocalTime.now().format(HORA));
    }
}
```

* **`Flux.interval(CADENCIA)`**: Este método de Reactor genera un flujo **infinito** que emite un número incremental (`0L`, `1L`, `2L`...) cada segundo. **Nunca emite `onComplete`**.
* **`.map(this::medir)`**: Por cada número emitido, calcula una temperatura que oscila de forma predecible como una onda senoidal entre 18°C y 34°C.

---

### 2. Los Dos Endpoints: La misma fuente servida de dos formas (`LecturaRestController.java`)

En nuestro controlador creamos dos endpoints que consumen exactamente el mismo servicio y devuelven el mismo `Flux<Lectura>`, pero cambiando el encabezado `produces`:

```java
@RestController
@RequestMapping("/api/lecturas")
public class LecturaRestController {

    private final SensorService sensor;

    // ── Forma A: JSON tradicional (acumula y manda de golpe) ──
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<Lectura> comoJson() {
        return sensor.lecturas().take(5);
    }

    // ── Forma B: Server-Sent Events (emisión en vivo) ─────────
    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Lectura> comoStream() {
        return sensor.lecturas().take(20);
    }
```

* **`.take(N)`**: Como el sensor es infinito, usamos el operador `.take(N)` para indicar que solo queremos los primeros $N$ elementos y luego finalizar el flujo con un `onComplete`.

#### Comparando el comportamiento desde la terminal:

1. **Probando el endpoint A (JSON):**
   ```bash
   time curl -s http://localhost:8075/api/lecturas
   ```
   * **Comportamiento:** La terminal se queda congelada 5 segundos. A los 5.03 segundos aparece todo el arreglo JSON junto:
     `[{"numero":0,"hora":"14:00:01"}, ... ,{"numero":4,"hora":"14:00:05"}]`
   * Las horas dentro del JSON demuestran que las lecturas ocurrieron segundo a segundo, pero Spring las retuvo para empaquetarlas en un solo array.

2. **Probando el endpoint B (Stream):**
   ```bash
   curl -N -s http://localhost:8075/api/lecturas/stream
   ```
   > [!IMPORTANT]
   > El parámetro `-N` en `curl` es obligatorio porque desactiva el buffer local de la terminal. Sin `-N`, `curl` retendría los datos en la consola y no verías el goteo en tiempo real.
   * **Comportamiento:** Inmediatamente al dar Enter, aparece la primera línea. Un segundo después aparece la segunda, y así sucesivamente hasta completar las 20 lecturas.

---

### 3. Operadores Reactivos sobre Flujos en Vivo

Uno de los mayores poderes de WebFlux es aplicar operadores funcionales sobre flujos que están ocurriendo en tiempo real:

#### A. Filtrar alertas en vivo (`filter`)
```java
@GetMapping(path = "/alertas", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<Lectura> alertas(@RequestParam(defaultValue = "30") double umbral) {
    return sensor.lecturas()
                 .filter(l -> l.celsius() > umbral)
                 .doOnNext(l -> log.warn("ALERTA {} C", l.celsius()));
}
```
* **`.filter(...)`**: Solo deja pasar las lecturas cuya temperatura supere el umbral indicado (por ejemplo, $> 30^\circ\text{C}$).
* **Comportamiento:** Verás llegar ráfagas de eventos cuando la temperatura esté en la cresta de la onda ($> 30^\circ\text{C}$), y luego habrá **silencio** cuando la temperatura baje.
* **Nota importante:** Durante el silencio, el sensor **no se ha detenido**. Sigue emitiendo cada segundo, pero el operador `filter` simplemente descarta los datos que no cumplen la condición.

---

#### B. Cerrar el flujo automáticamente por condición de negocio (`takeUntil`)
```java
@GetMapping(path = "/hasta/{umbral}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<Lectura> hasta(@PathVariable double umbral) {
    return sensor.lecturas()
                 .takeUntil(l -> l.celsius() < umbral)
                 .doOnComplete(() -> log.info("Bajó del umbral: emitiendo onComplete"));
}
```
* **`.takeUntil(predicado)`**: Deja pasar todas las lecturas **hasta que** una lectura cumpla la condición (por ejemplo, que la temperatura baje de 20°C).
* Cuando se cumple la condición, emite esa última lectura, envía inmediatamente la señal **`onComplete`** y Spring **cierra la conexión HTTP de forma automática**.
* En la terminal con `curl -N`, verás que el comando termina y recuperas el control del cursor solo, sin presionar `Ctrl+C`.

---

#### C. De vuelta a `Mono`: Colapsar un flujo (`collectList`)
¿Qué pasa si tenemos un flujo de eventos pero al final queremos calcular un único resultado consolidado (como un reporte o promedio)?

```java
@GetMapping(path = "/resumen", produces = MediaType.APPLICATION_JSON_VALUE)
public Mono<Map<String, Object>> resumen() {
    return sensor.lecturas()
                 .take(10)
                 .collectList()      // Transforma Flux<Lectura> en Mono<List<Lectura>>
                 .map(this::estadisticas);
}
```
* **`.collectList()`**: Espera a que el `Flux` emita sus 10 elementos y reciba el `onComplete`. En ese instante, los junta todos en una sola lista `List<Lectura>` y cambia el tipo de retorno a un **`Mono<List<Lectura>>`**.
* **`.map(this::estadisticas)`**: Toma esa lista completa y calcula los valores máximos, mínimos y el promedio en un único mapa JSON de respuesta.

> Esto demuestra por qué el endpoint (A) se comportaba como una lista: internamente, responder un `Flux` con `application/json` ejecuta exactamente esta misma operación de `collectList()` por debajo.

---

## Visualización en el Frontend con el Navegador (`static/index.html`)

Para ver la diferencia real lado a lado, el proyecto incluye una interfaz web accesible en `http://localhost:8075`.

```
┌───────────────────────────────────────┬───────────────────────────────────────┐
│       Panel A (application/json)      │     Panel B (text/event-stream)       │
├───────────────────────────────────────┼───────────────────────────────────────┤
│ [ Pedir las 5 lecturas ]              │ [ Abrir el stream ]                   │
│                                       │                                       │
│ (Pantalla en espera durante 5 seg...) │ #0 · 14:30:01   26.0 °C               │
│                                       │ #1 · 14:30:02   28.5 °C               │
│ (A los 5s se llena todo de golpe)     │ #2 · 14:30:03   30.7 °C (ALERTA)      │
│ #0, #1, #2, #3, #4                    │ ... (gotea en vivo cada segundo)      │
└───────────────────────────────────────┴───────────────────────────────────────┘
```

### ¿Cómo consume el navegador un `text/event-stream`?
Para conectarse a un endpoint reactivo SSE en JavaScript no se requieren librerías pesadas. Los navegadores web soportan nativamente la API **`EventSource`**:

```javascript
// Se abre la conexión reactiva con el backend
const es = new EventSource('/api/lecturas/stream');

// Se ejecuta UNA VEZ POR CADA señal onNext emitida por el servidor
es.onmessage = function (evento) {
    const lectura = JSON.parse(evento.data);
    pintarEnPantalla(lectura);
};

// Se ejecuta cuando el servidor envía onComplete o se interrumpe la conexión
es.onerror = function () {
    es.close(); // Cierra el canal del lado del cliente
};
```
* Cada `onNext` del `Flux` en Java viaja por la red y dispara inmediatamente el método `onmessage` en el navegador.
* Cuando el servidor emite `onComplete` tras las 20 lecturas, el navegador entra en `onerror` y cierra el recurso limpiamente.

---

## ¿Cómo se usa esto en el entorno profesional real?

En la arquitectura moderna de software, `Flux` y el streaming reactivo se utilizan ampliamente en:

1. **Dashboards y Monitoreo en Tiempo Real:** Paneles de control de infraestructura (monitoreo de servidores, métricas de CPU/RAM, telemetría de dispositivos IoT) que muestran datos actualizándose segundo a segundo sin recargar la página.
2. **Plataformas Financieras y Trading:** Transmisión de cotizaciones de acciones, criptomonedas o divisas en vivo a miles de clientes conectados concurrentemente.
3. **Consumo de Mensajería Reactiva:** Microservicios que consumen eventos continuos desde brokers como **Apache Kafka** o **RabbitMQ** y los transforman o redirigen en tiempo real sin saturar la memoria.
4. **Procesamiento de Archivos Grandes por Lotes:** Leer y procesar archivos masivos (por ejemplo, un CSV de varios Gigabytes) procesando fila por fila como un flujo `Flux`, evitando tener que cargar el archivo entero en la memoria RAM del servidor.
5. **Inteligencia Artificial y Modelos LLM:** Los endpoints que generan respuestas texto por palabra (como ChatGPT o Claude) utilizan exactamente este protocolo `text/event-stream` para enviar las palabras generadas en streaming al cliente.
