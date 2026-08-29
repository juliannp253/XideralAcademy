# JUnit 5 y Mockito: Guía Integral de Pruebas Unitarias

## 1. Filosofía del Testing: ¿Por qué se prueba?

### ¿Qué es un Test Automatizado?
Un test automatizado es **una afirmación sobre tu código, escrita en código, que una máquina puede comprobar de forma automática y repetible**. 

No es una herramienta mágica ni un lenguaje extraño: es simplemente un método de Java (anotado con `@Test`) que ejecuta una funcionalidad y declara cuál era el resultado esperado.

```
+-------------------------------------------------------------------------+
|                  Método main vs Test Automatizado                       |
+-------------------------------------------------------------------------+
| - Imprime texto en la consola.          | - Comprueba el resultado solo.   |
| - Te obliga a leer manualmente la salida| - Pasa a VERDE si todo coincide, |
|   para saber si el cálculo está bien.   |   o ROJO si algo no cuadra.      |
| - No se puede automatizar en un build.  | - Corre miles de veces en un CI. |
+-------------------------------------------------------------------------+
```

### Analogía con acta de calificaciones
Cuando un profesor entrega calificaciones a fin de semestre, no es solo hacer los cálculos en una servilleta: firma un **acta oficial**. El acta no realiza el cálculo; simplemente **declara** que Juan tiene 85 y que eso es lo correcto. Si semanas después alguien modifica la base de datos por error, el acta firmada y el sistema ya no coincidirán: la discrepancia salta de inmediato.

Un test automatizado es esa **acta de verdad**: declara cómo debe comportarse el sistema. El día que alguien modifique el código y rompa esa regla, el test se pondrá en **rojo** de inmediato.

### Las 4 Ideas Fundamentales del Testing

1. **El valor real de un test está en el fallo, no en el verde:**
   Una suite de pruebas completamente en verde únicamente nos dice que *los casos que se nos ocurrieron funcionan*. No garantiza que no existan bugs. Un test demuestra su verdadero valor el día que **se pone en rojo por un error inesperado que nadie había visto**.
   
2. **Prueba los límites y fronteras (*Boundary Testing*), no solo ejemplos felices:**
   Casi todos los errores reales en producción viven en los bordes:
   * El `>=` escrito erróneamente como `>`.
   * La lista con 0 elementos o con 1 solo elemento.
   * El número 69.9 vs 70.0 en una regla de aprobación.
   * El año bisiesto con 29 de febrero.
   * Los valores `null` y cadenas vacías `""`.
   Probar que un alumno con nota 95 aprueba no cuesta nada, pero protege poco (95 está lejísimos de la frontera). Lo que de verdad protege es probar **69.9, 70.0 y 70.1**.

3. **Independencia y Determinismo Total:**
   Cada test debe ser completamente autónomo. Debe poder ejecutarse solo, acompañado, en cualquier orden o 100 veces seguidas y dar **exactamente el mismo resultado**. Si el Test B depende de que el Test A haya guardado algo previamente en memoria o base de datos, la suite se vuelve frágil y mentirosa.

4. **El Patrón AAA (Arrange - Act - Assert):**
   Es el estándar universal para estructurar cualquier prueba:
   * **Arrange (Preparar):** Instanciar objetos, preparar datos de entrada y configurar respuestas simuladas.
   * **Act (Actuar):** Invocar el método específico que se desea probar y capturar el resultado.
   * **Assert (Afirmar / Verificar):** Comprobar que el resultado obtenido coincide exactamente con el esperado.

### La Pirámide de Pruebas

```
                   /\
                  /  \     E2E / UI Tests (Pocos, lentos, costosos)
                 /----\
                /      \   Integration Tests (BD, APIs, WebMvc)
               /--------\
              /          \ Unit Tests (Miles, instantáneos, aislados)
             +------------+
```

* **Unit Tests (Pruebas Unitarias):** Prueban una clase o método aislado del resto del mundo. Son ultra rápidos (milisegundos) y usan dobles de prueba (Mocks) para no tocar bases de datos ni redes.
* **Integration Tests (Pruebas de Integración):** Verifican que varios componentes funcionen bien juntos (ej. Servicio + Repositorio + Base de Datos H2/Postgres).
* **End-to-End Tests (Punta a Punta):** Prueban el flujo completo desde el cliente web o móvil hasta la base de datos real.

## 2. Anatomía de un Test y Ciclo de Vida en JUnit 5

### ¿Cómo ejecuta JUnit los tests por dentro?
Por defecto, para garantizar el principio de independencia, **JUnit crea una nueva instancia de la clase de prueba por cada método `@Test` que va a ejecutar** (ciclo de vida `PER_METHOD`).

```
Instancia 1 (BoletaTest) ───> @BeforeEach ───> testCalcularPromedio() ───> @AfterEach
Instancia 2 (BoletaTest) ───> @BeforeEach ───> testEstaAprobado()     ───> @AfterEach
```

### Anotaciones del Ciclo de Vida

| Anotación | Momento de Ejecución | Requisito / Uso común |
| :--- | :--- | :--- |
| **`@BeforeAll`** | Una sola vez antes de TODOS los tests de la clase | Debe ser método `static`. Conexiones globales o configs pesadas. |
| **`@BeforeEach`** | Antes de CADA método `@Test` | Inicializar datos limpios y preparar instancias frescas. |
| **`@Test`** | Marca el método como un caso de prueba ejecutable | No retorna nada (`void`). |
| **`@AfterEach`** | Después de CADA método `@Test` | Limpieza de archivos temporales o reseteo de estado. |
| **`@AfterAll`** | Una sola vez al final de TODOS los tests | Debe ser método `static`. Cierre de recursos globales. |
| **`@DisplayName`**| Asigna un nombre descriptivo y legible al test | Permite documentar en español qué prueba cada método. |

---

## 3. Aserciones que encontramos en JUnit 5

Las aserciones son métodos estáticos de `org.junit.jupiter.api.Assertions.*` que comprueban si una condición se cumple. Si falla, lanzan un `AssertionFailedError`.

### Aserciones de Comparación de Valores e Identidad

```java
// 1. Igualdad por valor (usa el método .equals() por debajo)
assertEquals(2, boleta.getCantidadMaterias(), "Debe tener 2 materias");
assertNotEquals(0, boleta.getCantidadMaterias());

// 2. Booleanos
assertTrue(boleta.estaAprobado(), "El alumno debe estar aprobado");
assertFalse(boleta.estaAprobado(), "El alumno debe estar reprobado");

// 3. Nulos
assertNotNull(boleta.getAlumno(), "El alumno no debe ser nulo");
assertNull(alumnoInexistente);

// 4. Identidad en memoria (compara con '==' si es el mismo objeto exacto en RAM)
assertSame(alumnoOriginal, boleta.getAlumno(), "Deben ser la misma referencia en memoria");
assertNotSame(alumnoClonado, boleta.getAlumno());
```

### Decimales en `float` y `double` (Delta)

En computación, los números decimales (`double` y `float`) siguen el estándar binario IEEE 754, lo que provoca pequeñas imprecisiones aritméticas (por ejemplo: `0.1 + 0.2 = 0.30000000000000004`).

Si comparas decimales con `assertEquals(esperado, actual)` sin margen de error, tu test fallará aleatoriamente. **Siempre debes incluir un margen de tolerancia (`delta`)**:

```java
// Permite una tolerancia de 0.001 de diferencia
assertEquals(80.333, boleta.calcularPromedio(), 0.001, "Promedio con margen de 0.001");
```

### Aserciones Agrupadas: `assertAll`

En un test tradicional, si tienes 4 `assertEquals` seguidos y el primero falla, **la ejecución se detiene de inmediato** y nunca te enteras si los otros 3 estaban bien o mal.

Con **`assertAll`**, JUnit ejecuta **todas** las aserciones internas y entrega un reporte completo de todos los fallos juntos:

```java
assertAll("Verificación completa de la boleta",
    () -> assertEquals(2, boleta.getCantidadMaterias(), "Cantidad de materias"),
    () -> assertTrue(boleta.estaAprobado(), "Estado de aprobación"),
    () -> assertEquals("APROBADO", boleta.obtenerEstadoMateria("Programación")),
    () -> assertEquals("APROBADO", boleta.obtenerEstadoMateria("Bases de Datos"))
);
```

### Probar Excepciones: `assertThrows`

Mucha de la calidad de una aplicación está en **cómo reacciona ante datos incorrectos**. Para verificar que un método rechaza entradas inválidas y lanza la excepción esperada:

```java
@Test
void lanzarExcepcionPorCalificacionFueraDeRango() {
    // Verifica que se lance IllegalArgumentException y captura el objeto para inspeccionarlo
    IllegalArgumentException error = assertThrows(
        IllegalArgumentException.class,
        () -> boleta.agregarCalificacion("Física", -5.0),
        "Calificación negativa debe lanzar excepción"
    );

    // Además podemos comprobar el mensaje de error dentro de la excepción
    assertTrue(error.getMessage().contains("entre 0.0 y 100.0"));
}

@Test
void verificarQueNoLanzaExcepciones() {
    // Verifica explícitamente que una llamada válida termine de forma limpia
    assertDoesNotThrow(() -> curso.inscribir(alumnoValido));
}
```

### Probar Tiempos y Rendimiento: `assertTimeout`

Para garantizar que un proceso crítico o cálculo no tarde más del tiempo acordado:

```java
@Test
void generacionDeActaDentroDelTiempoLimite() {
    // Si la ejecución tarda más de 300 ms, el test se pone en ROJO
    String resultado = assertTimeout(
        Duration.ofMillis(300),
        () -> curso.generarActaCalificaciones(),
        "El acta tardó más de los 300 ms permitidos"
    );
    assertNotNull(resultado);
}
```

## 4. Los Tres Estados de un Test: Verde, Rojo y Omitido (Skipped)

En JUnit, un test no solo puede estar aprobado o reprobado. Existen **3 estados**:

1. PASSED (Verde)-> Todas las aserciones se cumplieron con éxito.
2. FAILED (Rojo)-> Una aserción falló o saltó una excepción no esperada.
3. SKIPPED (Amarillo)-> El test fue omitido/abortado deliberadamente. ***El build sigue saliendo VERDE***

### ¿Cómo se omite un test?

1. **De forma estática con `@Disabled`:**
   ```java
   @Test
   @Disabled("Deshabilitado hasta que se arregle el bug #402 en la API externa")
   void testEnMantenimiento() { ... }
   ```

2. **De forma condicional con `Assumptions` (Suposiciones):**
   A diferencia de un `assert` (que si falla pone el test en rojo), un `assumeTrue` o `assumeFalse` comprueba una precondición ambiental. Si la condición no se cumple, **el test se aborta silenciosamente sin romper el build**:
   ```java
   @Test
   void testSoloParaServidorDeIntegracionContinua() {
       // Si no estamos en Linux o no existe la variable de entorno, el test se omite (Skipped)
       assumeTrue("true".equalsIgnoreCase(System.getenv("CI_SERVER")));
       
       // El código siguiente solo corre si la condición fue verdadera
       ejecutarPruebaPesadaDeIntegracion();
   }
   ```

## 5. Organización de Suites: `@Nested` y `@Tag`

### Organización Jerárquica con `@Nested`
En lugar de tener una clase gigantesca con 30 métodos planos, `@Nested` permite agrupar las pruebas por **contextos o escenarios del mundo real**, esto hace que el reporte de pruebas se lea como una especificación funcional en lenguaje natural:

```java
@DisplayName("Gestión de Cursos")
class CursoTest {

    @Nested
    @DisplayName("Cuando el curso tiene cupo disponible")
    class CuandoTieneCupoDisponible {
        @Test
        @DisplayName("Debe permitir inscribir alumnos con éxito")
        void inscribirConExito() { ... }
    }

    @Nested
    @DisplayName("Cuando el curso está lleno")
    class CuandoElCursoEstaLleno {
        @Test
        @DisplayName("Debe rechazar la inscripción y lanzar CursoLlenoException")
        void rechazarInscripcion() { ... }
    }
}
```

### Partir la Suite en Dos Velocidades con `@Tag`
En proyectos grandes hay tests que tardan 1 milisegundo y tests que tardan varios segundos. Con `@Tag` podemos etiquetarlos:

```java
@Tag("rapido")
@Test
void calculoMatematicoEnMemoria() { ... }

@Tag("lento")
@Test
void generacionDeReportesPDF() { ... }
```

Desde Maven podemos ejecutar selectivamente solo los tests rápidos para un feedback instantáneo:
```bash
./mvnw test -Dgroups="rapido"
```

## 6. Tests Parametrizados: `@ParameterizedTest` (Un Test, Cientos de Datos)

El problema de las pruebas unitarias clásicas es la duplicación: si quieres probar un validador de CURP con 20 cadenas distintas, tendrías que escribir 20 métodos `@Test` casi idénticos.

Con **`@ParameterizedTest`** escribes **un solo método** y le inyectas decenas de entradas diferentes:

### 1. `@ValueSource` (Para listas simples de valores)
```java
@ParameterizedTest(name = "[{index}] La CURP {0} debe ser válida")
@ValueSource(strings = {
    "RUGM800101HNLZRK09",
    "ROAL920315MDFRNR02",
    "GOMA850620HJCZNN01"
})
void validarCurpsValidas(String curp) {
    assertTrue(validador.esValida(curp));
}
```

### 2. `@NullAndEmptySource` (Manejo de nulos y vacíos)
```java
@ParameterizedTest
@NullAndEmptySource
void rechazarNulosYVacios(String entradaInvalida) {
    assertFalse(validador.esValida(entradaInvalida));
}
```

### 3. `@CsvSource` (Tablas de datos en línea: Entrada y Resultado Esperado)
```java
@ParameterizedTest(name = "[{index}] CURP: {0} -> Esperado: {1}")
@CsvSource({
    "RUGM800101HNLZRK09, true",   // Válida
    "RUGM800101,        false",  // Longitud corta
    "RUGM800101HXXZRK09, false"   // Entidad 'XX' inexistente
})
void validarCasosTabulares(String curp, boolean esperado) {
    assertEquals(esperado, validador.esValida(curp));
}
```

### 4. `@CsvFileSource` (Carga masiva desde archivo `.csv` externo)
```java
@ParameterizedTest
@CsvFileSource(resources = "/curps_prueba.csv", numLinesToSkip = 1)
void validarDesdeArchivo(String curp, boolean esperado) {
    assertEquals(esperado, validador.esValida(curp));
}
```

### 5. `@MethodSource` (Generador dinámico de objetos complejos)
Ideal cuando los datos de prueba requieren lógica Java compleja, como validar años bisiestos (29 de febrero):

```java
@ParameterizedTest
@MethodSource("proveerFechasBisiestas")
void probarFechasBisiestas(String curp, boolean esperado, String motivo) {
    assertEquals(esperado, validador.esValida(curp), motivo);
}

static Stream<Arguments> proveerFechasBisiestas() {
    return Stream.of(
        Arguments.of("RUGM000229HNLZRK09", true, "Año 2000 es bisiesto (29 feb existe)"),
        Arguments.of("RUGM010229HNLZRK09", false, "Año 2001 NO es bisiesto (29 feb no existe)"),
        Arguments.of("RUGM801301HNLZRK09", false, "Mes 13 no existe en el calendario")
    );
}
```

## 7. Dobles de Prueba y Mockito: Aislamiento Total

### ¿Qué es un Doble de Prueba (*Test Double*)?
En una película de acción, cuando el actor principal tiene que saltar de un helicóptero en llamas, no salta el actor real: se usa un **doble de riesgo**.

En programación ocurre lo mismo: cuando queremos probar nuestra lógica de negocio (`InscripcionService`), no queremos que se conecte a una base de datos Oracle real, ni que mande un correo electrónico verdadero a un cliente real, ni que cobre una tarjeta de crédito. Usamos **Dobles de Prueba**.

```
                   TAXONOMÍA DE DOBLES DE PRUEBA                      

1. Dummy -> Objeto de relleno que se pasa como parámetro pero nunca se  
            usa (ej. 'new Alumno()').                                   
2. Stub  -> Objeto simulado que devuelve respuestas fijas programadas   
            previamente ('when(...).thenReturn(...)').                  
3. Spy   -> Envoltorio sobre un objeto REAL que registra las llamadas   
            que recibió permitiendo verificar cómo interactuaron con él.
4. Mock  -> Objeto simulado completo que verifica comportamiento y      
            llamadas ('verify(...)').                                   

```

### Configuración con `@ExtendWith(MockitoExtension.class)`

```java
@ExtendWith(MockitoExtension.class)
class InscripcionServiceTest {

    @Mock
    private AlumnoRepository alumnoRepository;     // Mock simulado vacío

    @Mock
    private NotificacionService notificacionService; // Mock simulado vacío

    @Spy
    private ValidadorCurp validadorCurp;           // Instancia REAL interceptada

    @InjectMocks
    private InscripcionService inscripcionService; // Inyecta los mocks en el servicio

    @Captor
    private ArgumentCaptor<String> captorEmail;   // Capturador de argumentos
    
    @Captor
    private ArgumentCaptor<String> captorMensaje;
}
```

---

### 1. Programar Respuestas: *Stubbing* (`when / thenReturn / thenThrow`)

Por defecto, todos los métodos de un `@Mock` devuelven valores por defecto (`null`, `0`, `false` o colecciones vacías). Con `when` programamos qué queremos que respondan:

```java
// Devuelve false cuando busquen si existe esa CURP
when(alumnoRepository.existsByCurp(alumno.curp())).thenReturn(false);

// Devuelve el objeto alumno cuando llamen a save()
when(alumnoRepository.save(any(Alumno.class))).thenReturn(alumno);

// Simula que el servicio de correo lanza una excepción por caída de red
when(notificacionService.enviarCorreoBienvenida(anyString(), anyString()))
    .thenThrow(new RuntimeException("Servidor SMTP caído"));
```

* **Argument Matchers:** Podemos usar comparadores flexibles como `anyString()`, `anyInt()`, `any(Clase.class)` o valores exactos con `eq("valorExacto")`.

### 2. Verificación de Comportamiento: *Verifying* (`verify`)

En pruebas unitarias no solo importa lo que el método retorna, sino **las acciones secundarias que realizó**:

```java
// Verifica que save() se llamó EXACTAMENTE 1 vez con el alumno correcto
verify(alumnoRepository, times(1)).save(alumno);

// Verifica que NUNCA se envió alerta de administrador
verify(notificacionService, never()).enviarAlertaAdministrador(anyString(), anyString());

// Verifica que si los datos eran inválidos, los mocks NUNCA fueron tocados
verifyNoInteractions(notificacionService);
```

### 3. ArgumentCaptor

A veces no solo queremos saber si un método fue invocado, sino **inspeccionar con lujo de detalle el contenido exacto del parámetro que se le pasó**:

```java
@Test
void validarContenidoDelCorreoEnviado() {
    inscripcionService.inscribirAlumno(curso, alumno);

    // Captura los argumentos reales pasados al mock
    verify(notificacionService).enviarCorreoBienvenida(captorEmail.capture(), captorMensaje.capture());

    // Validamos el contenido capturado
    assertEquals("paco@academymty.mx", captorEmail.getValue());
    assertTrue(captorMensaje.getValue().contains("Francisco Perez"));
    assertTrue(captorMensaje.getValue().contains("Patrones de Diseño"));
}
```

---

### 4. `@Mock` vs `@Spy`

* **`@Mock`:** Es una cáscara vacía. Si no programas una respuesta con `when()`, el método no hace absolutamente nada y devuelve `null` o `false`.
* **`@Spy`:** Es una instancia **real** de la clase. Ejecuta el código verdadero de Java de cada método, a menos que uses `doReturn().when()` para sobreescribir un método específico.

```java
// Con un Spy, alteramos temporalmente un método real sin afectar al resto
doReturn(false).when(validadorCurpSpy).esValida(anyString());
```

### ¿Cuándo NO mockear?

**No siempre hay que mockearlo todo:**
1. **NUNCA mockees objetos de dominio simples (Records, Entidades, POJOs, DTOs o Listas):** Instancia un `new Alumno(...)` o `new ArrayList<>()` real. Mockear un POJO o un getter es una mala práctica que genera tests frágiles y sin sentido.
2. **No mockees utilerías deterministas puras:** Clases utilitarias o de validación matemática/formato que no tocan disco ni red (como `ValidadorCurp`) deben usarse de forma real o con `@Spy`.
3. **Solo mockea los límites del sistema:** Bases de datos (`Repositories`), APIs externas (`WebClients`), servicios de mensajería (colas Kafka, SMTP) y componentes con efectos secundarios reales.

## 8. Estructura del proyecto

```
01-junit-testing/
├── pom.xml
├── scripts/
│   ├── probar.sh              <- Ejecuta la suite completa
│   └── filtrar.sh             <- Ejecuta solo @Tag("rapido") o @Tag("lento")
└── src/
    ├── main/java/com/academymty/testing/
    │   ├── exception/         <- CursoLlenoException, CursoCerradoException
    │   ├── model/             <- Alumno, Boleta, Curso, InscripcionResultado
    │   ├── repository/        <- AlumnoRepository
    │   ├── service/           <- InscripcionService, NotificacionService
    │   └── util/              <- ValidadorCurp
    └── test/
        ├── java/com/academymty/testing/
        │   ├── BoletaTest.java             <- Ciclo de vida, aserciones y boundary testing
        │   ├── CursoTest.java              <- Excepciones, timeouts, @Nested y @Tag
        │   ├── ValidadorCurpTest.java      <- @ParameterizedTest (Value, Csv, MethodSource)
        │   └── InscripcionServiceTest.java <- Mockito (@Mock, @Spy, @Captor, verify)
        └── resources/
            └── curps_prueba.csv
```