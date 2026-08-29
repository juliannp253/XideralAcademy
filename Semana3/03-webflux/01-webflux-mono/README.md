# WebFlux y Mono: Fundamentos de la Programación Reactiva

## ¿Qué problema teníamos antes? (El modelo tradicional de Spring MVC y Tomcat)

En proyectos anteriores trabajamos con **Spring Boot MVC** tradicional sobre un servidor **Tomcat**. En ese modelo, la arquitectura funciona bajo el esquema llamado **Thread-per-Request** (un hilo por cada petición entrante):

1. Un cliente realiza una petición HTTP al servidor.
2. Tomcat le asigna un hilo exclusivo de su piscina (*thread pool*, típicamente de 200 hilos).
3. Ese hilo atiende la petición: pasa por los filtros, llega al `@RestController`, llama al servicio, consulta la base de datos o consume otra API externa.
4. Una vez generada la respuesta, el hilo la entrega al cliente y finalmente queda libre para atender a otra persona.

```
Petición 1  ───> [ Hilo 1 (Tomcat) ] ───> Esperando BD (300ms) ───> Respuesta
Petición 2  ───> [ Hilo 2 (Tomcat) ] ───> Esperando BD (300ms) ───> Respuesta
...
Petición 201 ──> [ COLA DE ESPERA / BLOQUEO ] (No hay hilos libres)
```

### ¿Cuál era el problema con esto?
El gran problema de este modelo es el **bloqueo de hilos durante operaciones de I/O (Entrada/Salida)**.

En una aplicación web real, casi todo el tiempo de respuesta no se gasta en cálculos pesados del procesador (CPU), sino en **esperar**:
* Esperar a que la base de datos conteste una consulta SQL.
* Esperar a que un microservicio externo nos devuelva un JSON.
* Esperar a leer o escribir un archivo en disco o en la red.

En el modelo tradicional, mientras ocurre esa espera, el hilo de Java se queda **dormido / bloqueado** (como si hiciera un `Thread.sleep()`). Aunque no esté haciendo nada útil, ese hilo sigue ocupando memoria RAM (aproximadamente 1 MB por hilo en la JVM) y recursos del sistema operativo.

Si de pronto llegan 200 peticiones simultáneas que tardan 2 o 3 segundos por lentitud en la base de datos, los 200 hilos de Tomcat quedan retenidos. Cuando llega la petición número 201, **ya no hay hilos disponibles**: la petición se encola, los tiempos de respuesta se disparan y el servidor puede colapsar por inanición de hilos (*Thread Starvation*), a pesar de que el procesador esté prácticamente al 0% de uso.

---

## ¿Qué es la Programación Reactiva y qué solución trae?

La **Programación Reactiva** propone un cambio total de mentalidad: en lugar de que el hilo se quede sentado esperando la respuesta, el sistema pasa a ser **asíncrono y no bloqueante**, basado en eventos (*Event-Driven*).

### La analogía del restaurante
* **Modelo Tradicional (Tomcat / MVC):** Llega un mesero a tu mesa, toma tu orden, va a la cocina y **se queda parado frente al chef esperando** 15 minutos a que preparen tu comida. Durante esos 15 minutos, ese mesero no puede atender a nadie más. Para atender a 50 clientes necesitas 50 meseros parados en la cocina.
* **Modelo Reactivo (Netty / WebFlux):** Llega un solo mesero, toma tu orden, deja la orden en la cocina y **se va de inmediato a tomar la orden de otras mesas**. Cuando tu plato está listo, la cocina timbra una campana (un evento) y el mesero disponible toma el plato y te lo entrega. Con 2 o 3 meseros rápidos puedes atender a cientos de comensales.

### Netty y el Event Loop
Spring WebFlux no utiliza Tomcat por defecto, utiliza **Netty**. 

Netty no crea 200 hilos. Crea únicamente una pequeña cantidad de hilos de ejecución (llamados hilos de **Event Loop**), típicamente igual a la cantidad de núcleos lógicos de tu procesador (por ejemplo, 8 o 12 hilos).

```
Peticiones ───> [ Event Loop (4-8 hilos) ] ───> Registra callback (No bloquea)
                                                           │
                                              [ Notificación cuando el dato está listo ]
                                                           │
Cliente <────── [ Event Loop ] <───────────────────────────┘
```

## ¿Qué es un `Mono`?

En la librería **Project Reactor** (el motor reactivo detrás de Spring WebFlux), existen dos tipos fundamentales: `Mono` y `Flux`.

Un **`Mono<T>`** es un publicador reactivo (*Publisher*) que representa un valor asíncrono que puede emitir:
* **0 elementos** (un flujo vacío que termina exitosamente).
* **1 elemento** de tipo `T`.
* O una **señal de error**.

### Los 3 canales de comunicación
Un flujo reactivo se comunica a través de tres canales estándar:
1. **`onNext(valor)`**: Emite un dato hacia el suscriptor.
2. **`onComplete()`**: Señal que avisa que el flujo terminó con éxito (no habrá más datos).
3. **`onError(excepcion)`**: Señal que avisa que ocurrió un error y el flujo se detuvo.

```
Mono con dato:     ────( onNext: Employee )────( onComplete )────>
Mono vacío:        ────────────────────────────( onComplete )────>  (¡Éxito sin datos!)
Mono con error:    ────────────────────────────( onError: Exception )─>
```

### La idea clave: El Mono es la "Receta", no el plato
Cuando escribes:
```java
Mono<Employee> resultado = repo.findById(1);
```
En esa línea **no se ha buscado nada, no se ha consultado nada y no se ha esperado ningún segundo**. 

Un `Mono` es una **declaración de intenciones** (una receta de cocina). Define qué pasos se deben hacer cuando los datos se requieran.

**"Nada pasa hasta que te suscribes" (*Nothing happens until you subscribe*):**
 El código dentro de un Mono solo se pone en marcha cuando alguien llama al método `.subscribe()`. En una aplicación web con Spring WebFlux, nosotros nunca llamamos a `.subscribe()` manualmente en los controladores: **Spring se encarga de suscribirse por debajo** cuando llega la petición HTTP y de liberar el hilo del servidor de inmediato.

---

## ¿Cómo está implementado en este proyecto?


### 1. Dependencias (`pom.xml`)
Para usar WebFlux, en lugar del starter web clásico de MVC, incluimos el starter reactivo:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```
Este starter trae consigo **Project Reactor** (`Mono` / `Flux`), el servidor **Netty** y la serialización JSON.

---

### 2. El Repositorio y la Latencia Simulada (`EmployeeRepository.java`)

En una aplicación real consultaríamos una base de datos reactiva (como MongoDB reactivo o PostgreSQL con R2DBC). Para entender el concepto sin complicarnos con bases de datos externas, el repositorio guarda los datos en memoria en un `Map`, pero simula lo único importante: **que una consulta real toma tiempo**.

```java
@Repository
public class EmployeeRepository {

    public static final Duration LATENCIA = Duration.ofMillis(300);

    private final Map<Integer, Employee> tabla = new ConcurrentHashMap<>(...);

    // ── Versión Reactiva (No Bloqueante) ─────────────────────
    public Mono<Employee> findById(int id) {
        return Mono.justOrEmpty(tabla.get(id))   // Si no existe, genera un Mono vacío
                   .delayElement(LATENCIA);      // Simula que tarda 300ms SIN dormir el hilo
    }

    // ── Versión Bloqueante (Como en Spring MVC tradicional) ──
    public Employee findByIdBloqueante(int id) {
        try {
            Thread.sleep(LATENCIA.toMillis());   // DUERME el hilo actual
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return tabla.get(id);
    }
}
```

* **`Mono.justOrEmpty(...)`**: Toma el valor del `Map`. Si la llave existe, crea un `Mono` con ese empleado; si es `null`, crea un `Mono.empty()`.
* **`.delayElement(LATENCIA)`**: Este operador es vital. Programa la emisión del valor para 300 ms después mediante un temporizador asíncrono. **El hilo no se queda esperando los 300 ms**: se desentiende de inmediato y queda libre.
* **`findByIdBloqueante()`**: Usa `Thread.sleep()`. Aquí el hilo se queda congelado durante 300 ms sin poder hacer nada. Esta versión existe en el proyecto únicamente para comparar ambos comportamientos en la comparación.

---

### 3. El Controlador Reactivo (`EmployeeRestController.java`)

```java
@RestController
@RequestMapping("/api")
public class EmployeeRestController {

    private final EmployeeRepository repo;

    public EmployeeRestController(EmployeeRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/employees/{id}")
    public Mono<Employee> findById(@PathVariable int id) {
        return repo.findById(id)
                   .switchIfEmpty(Mono.error(new ResponseStatusException(
                           HttpStatus.NOT_FOUND, "No existe el empleado " + id)));
    }
```

#### ¿Qué cambia con respecto a Spring MVC?
En MVC la firma del método devolvía directamente `public Employee findById(...)` y el hilo se bloqueaba hasta que el método retornara.
En WebFlux devolvemos **`public Mono<Employee> findById(...)`**:
1. Entra la petición HTTP.
2. El método `findById()` ejecuta su cuerpo, devuelve el objeto `Mono` en microsegundos y **suelta el hilo de Netty**.
3. Cuando transcurren los 300 ms de latencia, Netty retoma la respuesta, serializa el JSON y lo envía al cliente.

---

### 4. El Vacío no es un Error (`switchIfEmpty`)

Una de las sorpresas más comunes al empezar con WebFlux es qué pasa si un ID no existe. Analicemos estos dos endpoints del proyecto:

```java
// Endpoint A: Sin switchIfEmpty
@GetMapping("/employees-suave/{id}")
public Mono<Employee> findByIdSuave(@PathVariable int id) {
    return repo.findById(id); // Si no existe devuelve Mono.empty()
}

// Endpoint B: Con switchIfEmpty
@GetMapping("/employees/{id}")
public Mono<Employee> findById(@PathVariable int id) {
    return repo.findById(id)
               .switchIfEmpty(Mono.error(new ResponseStatusException(
                       HttpStatus.NOT_FOUND, "No existe el empleado " + id)));
}
```

Si probamos ambos con un ID inexistente (`999`):
* `GET /api/employees-suave/999` ➔ Responde **`HTTP 200 OK` con cuerpo vacío** (`content-length: 0`).
* `GET /api/employees/999` ➔ Responde **`HTTP 404 NOT FOUND`**.

#### ¿Por qué `Mono.empty()` responde `200 OK` y no `404`?
Porque en el modelo reactivo, un flujo vacío significa que el publicador emitió la señal **`onComplete`** sin haber emitido ningún `onNext`. Para el framework, completar un flujo sin errores es un **final feliz**. 

"No haber encontrado datos" no es un error técnico; es simplemente un flujo que terminó en cero elementos. Por eso, si como regla de negocio queremos devolver un `404 Not Found`, debemos indicarlo explícitamente usando **`.switchIfEmpty(...)`**, que sustituye el flujo vacío por un `Mono.error(...)`.

---

### 5. Manejo de Errores Reactivo sin `try/catch` (`onErrorResume`)

En el paradigma reactivo no podemos envolver el código en bloques `try/catch` tradicionales, porque la ejecución ocurre de manera asíncrona en otro momento y contexto. Los errores viajan como eventos por el canal **`onError`**.

En el endpoint de prueba:

```java
@GetMapping("/employees/{id}/boom")
public Mono<Employee> boom(@PathVariable int id) {
    return repo.findById(id)
               .flatMap(e -> Mono.<Employee>error(new IllegalStateException("truena a proposito")))
               .onErrorResume(ex -> {
                   log.warn("Error capturado en el flujo: {}", ex.getMessage());
                   return Mono.just(new Employee(-1, "Plan", "B", "fallback@academymty.mx"));
               });
}
```

* **`flatMap(...)`**: Permite transformar el elemento emitido en otro publicador (`Mono.error(...)`).
* **`onErrorResume(...)`**: Es el equivalente reactivo a un bloque `catch`. Si viaja una señal de error por el flujo, este operador la atrapa y nos permite retornar un flujo alternativo (un empleado de respaldo o *fallback*). El cliente recibe un `200 OK` con los datos de contingencia en lugar de un error 500.

---

### 6. Métodos Reactivos que no devuelven contenido (`Mono<Void>`)

Para operaciones como eliminar recursos donde no se necesita retornar un cuerpo:

```java
@DeleteMapping("/employees/{id}")
public Mono<Void> delete(@PathVariable int id) {
    return repo.findById(id).then();
}
```
* **`Mono<Void>`**: Es el equivalente reactivo a un método `void`.
* **`.then()`**: Descarta cualquier valor emitido por el flujo original y únicamente conserva la señal de finalización (`onComplete` o `onError`). De esta manera, el cliente sabe exactamente cuándo concluyó la operación sin esperar un payload.

---

### 7. Comprobando los hilos del Event Loop (`HiloRestController.java`)

Para comprobar que Netty realmente trabaja con una cantidad fija y pequeña de hilos:

```java
@RestController
public class HiloRestController {

    @GetMapping("/api/hilo")
    public Mono<Map<String, Object>> hilo() {
        return Mono.just(Map.of(
                "hilo", Thread.currentThread().getName(),
                "hilosDisponibles", Runtime.getRuntime().availableProcessors()
        ));
    }
}
```

Si llamamos a este endpoint repetidas veces:
```bash
for i in {1..5}; do curl -s http://localhost:8074/api/hilo; echo ""; done
```
Veremos que los nombres de los hilos se repiten una y otra vez:
* `reactor-http-nio-1`
* `reactor-http-nio-2`
* `reactor-http-nio-3`
* `reactor-http-nio-4`

En Tomcat veríamos `http-nio-8080-exec-1`, `exec-2`, `exec-3`... creando hilos nuevos hasta el límite configurado. En WebFlux, son siempre los mismos pocos hilos del Event Loop.

---

## El Gran Experimento: Reactivo vs Bloqueante (`scripts/comparar.sh`)

El proyecto incluye dos endpoints con exactamente la misma latencia simulada de **300 ms**:
1. **Ruta Reactiva:** `/api/employees/1` (Usa `delayElement`, no bloquea el hilo).
2. **Ruta Bloqueante:** `/api/mvc/employees/1` (Usa `Thread.sleep()` dentro de `BloqueanteRestController.java`).

Si hacemos **1 sola petición** a cada uno, ambos tardan exactamente lo mismo: ~300 ms.

Pero, ¿qué pasa si lanzamos **100 peticiones concurrentes** al mismo tiempo con el script `./scripts/comparar.sh 100`?

```bash
$ ./scripts/comparar.sh 100
  Tu máquina tiene 8 núcleos, así que el event loop de Netty tiene ~8 hilos.
  Lanzamos 100 peticiones CONCURRENTES a cada ruta.

  Resultados:
  reactivo     100 peticiones en   0.38 s
  bloqueante   100 peticiones en   3.85 s
```

### ¿Por qué ocurrió esto?
* **En el endpoint Reactivo:** Las 100 peticiones entraron casi al mismo tiempo. Los 8 hilos del Event Loop tomaron las 100 peticiones, registraron los temporizadores de 300 ms y se desocuparon de inmediato. Al cumplirse los 300 ms, se entregaron las 100 respuestas en paralelo. Tiempo total: **~0.38 segundos**.
* **En el endpoint Bloqueante:** Las 100 peticiones intentaron entrar, pero los 8 hilos se durmieron con `Thread.sleep()`. Solo pudieron procesarse de 8 en 8. Para procesar 100 peticiones con 8 hilos se necesitaron unas 13 tandas consecutivas de 300 ms ($13 \times 300\text{ ms} \approx 3.9\text{ s}$).


**Conclusión del Experimento:**
La programación reactiva no hace que una consulta lenta a base de datos sea mágicamente más rápida. Lo que hace es **evitar que una petición lenta secuestre los hilos del servidor y bloquee a los demás usuarios**.

## ¿Cómo se usa esto en el entorno profesional real?

En la industria actual de desarrollo en Java:

### ¿Cuándo SÍ se utiliza WebFlux y Mono?
1. **APIs Gateway y Proxies (ej. Spring Cloud Gateway):** Sistemas que reciben miles de peticiones por segundo y simplemente enrutan o agregan llamadas a otros microservicios.
2. **Arquitecturas de Alta Concurrencia:** Aplicaciones donde se manejan miles de conexiones simultáneas con operaciones intensivas de I/O (chats, dashboards en tiempo real, streaming).
3. **Pilas Reactivas de Extremo a Extremo (*End-to-End Reactive*):** Cuando toda la cadena de componentes es no bloqueante:
   * Cliente WebClient ➔ WebFlux ➔ Driver R2DBC / Reactive MongoDB / Reactive Redis.

### ¿Cuándo NO conviene usarlo?
1. **Sistemas CRUD tradicionales con bases de datos relacionales vía JPA / Hibernate (JDBC):** JDBC es por naturaleza bloqueante a nivel de socket. Si usas JPA clásico dentro de WebFlux, tus hilos de Netty se van a bloquear igual que en el experimento, perdiendo todas las ventajas.
2. **Operaciones pesadas de CPU:** Si tu servicio realiza procesamiento de imágenes, cifrado pesado o cálculos matemáticos largos, el Event Loop se saturará. Para esos casos es mejor el modelo tradicional de hilos o delegar el cálculo a piscinas de hilos dedicadas (*Schedulers.boundedElastic()*).
3. **Curva de aprendizaje del equipo:** El código funcional reactivo y la depuración de errores es más compleja que el flujo imperativo secuencial tradicional.
