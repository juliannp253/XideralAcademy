# Proyecto de Pruebas Automatizadas con Selenium y TestNG 🧪

En este proyecto se pone en práctica la automatización de pruebas web utilizando **Java**, **Selenium WebDriver** y **TestNG**. 

El objetivo principal fue automatizar la navegación y validación de los diferentes menús y enlaces del portal de **Banco Santander México**, asegurando que cada sección lleve a la página correcta sin tener que hacer todas esas comprobaciones a mano.

---

## 📁 ¿Cómo está organizado el proyecto?

El proyecto sigue el patrón **Page Object Model (POM)** y la estructura estándar de Maven, separando lo que es lógica de la página, herramientas de apoyo y las pruebas en sí:

```text
selenium-testing-santander/
├── pom.xml                               # Configuración de dependencias (Selenium, TestNG)
├── testng.xml                            # Suite de pruebas que define el orden de ejecución y listeners
├── src/
│   ├── main/java/
│   │   ├── base/
│   │   │   └── BasePage.java             # Métodos comunes para interactuar con el navegador
│   │   ├── pages/
│   │   │   ├── SantanderHomePage.java    # Localizadores y acciones de la página de Santander
│   │   │   └── AutomationPracticePage.java 
│   │   └── utils/
│   │       └── DriverFactory.java        # Configuración y arranque de Google Chrome
│   └── test/java/
│       ├── base/
│       │   └── BaseTest.java             # Ciclo de vida: abre la web antes del test y cierra el navegador al final
│       ├── listeners/
│       │   └── ScreenshotListener.java   # Toma captura de pantalla automática si un test falla
│       └── tests/
│           ├── PersonasMenuTest.java     # Pruebas del menú Personas (cuentas, tarjetas, créditos, etc.)
│           ├── EmpresasMenuTest.java     # Pruebas del menú Empresas
│           ├── PyMesMenuTest.java        # Pruebas del menú PyMEs
│           ├── BancaPrivadaTest.java     # Pruebas del acceso a Banca Privada
│           └── AcercaBancoTest.java      # Pruebas del menú Acerca del Banco (blog, bolsa de trabajo, etc.)
```

---

## 🔍 ¿Qué hace cada archivo y carpeta?

### 1. `src/main/java/` (El corazón de la interacción)
* **`base/BasePage.java`**: Es la clase madre de las páginas. Aquí viven las funciones reutilizables como hacer clic (haciendo scroll automático para que el elemento quede centrado en pantalla), escribir texto, seleccionar listas desplegables y esperar que los elementos estén visibles antes de tocarlos.
* **`pages/SantanderHomePage.java`**: Contiene los selectores de los diferentes menús y botones del portal de Santander (organizados en clases estáticas para que sea fácil ubicarlos: `Personas`, `Empresas`, `PyMes`, `AcercaBanco`), además de métodos sencillos para hacer clic en el menú y su subopción.
* **`pages/AutomationPracticePage.java`**: Una página de práctica con la que se aprendieron interacciones más variadas: llenar formularios, marcar checkboxes y radios, manejar alertas del navegador (Alert, Confirm, Prompt), hacer drag and drop, doble clic y cambio entre pestañas.
* **`utils/DriverFactory.java`**: Se encarga de levantar Chrome con las opciones ideales (pantalla maximizada, sin popups molestos de contraseñas y con soporte para modo silencioso/headless). Gracias a Selenium Manager, no hace falta descargar ningún binario de ChromeDriver manualmente; Selenium lo gestiona solo.

### 2. `src/test/java/` (Las pruebas y validaciones)
* **`base/BaseTest.java`**: Define las reglas para todas las pruebas. Antes de cada test (`@BeforeMethod`), abre una ventana limpia de Chrome y navega a `https://www.santander.com.mx/`. Al terminar (`@AfterMethod`), cierra el navegador para no dejar procesos colgados en la computadora.
* **`listeners/ScreenshotListener.java`**: Escucha los eventos de TestNG. Si una prueba llega a fallar, automáticamente toma una captura de pantalla, le pone nombre con la fecha y hora, la guarda en una carpeta y agrega el enlace directo en el reporte de resultados para ver qué pasó en ese instante exacto.
* **`tests/`**: Aquí están los casos de prueba por cada sección de la web (`PersonasMenuTest`, `EmpresasMenuTest`, `PyMesMenuTest`, `BancaPrivadaTest`, `AcercaBancoTest`).

---

## ⚙️ ¿Cómo funcionan y cómo se lograron los tests?

Para no escribir 30 veces el mismo test cambiando solo el botón y la URL, se implementó el siguiente método:

1. **Page Object Model (POM)**: Las pruebas no buscan elementos HTML directamente ni saben qué XPath o ID tienen; simplemente le piden a `SantanderHomePage` que seleccione una opción. Si mañana cambia un botón en la página, solo se actualiza en un lugar.
2. **DataProviders de TestNG (para pruebas repetitivas)**: En lugar de copiar y pegar el mismo método una y otra vez, se usa un `@DataProvider` combinado con `record`s de Java. Se define una lista de datos (qué botón tocar y qué texto se espera ver en la URL) y un único método de prueba se ejecuta automáticamente para cada combinación.
3. **Esperas Inteligentes (`WebDriverWait`)**: En vez de usar pausas fijas ciegas (`Thread.sleep`), el código espera de forma dinámica a que los elementos estén listos para recibir clics, lo que hace que las pruebas sean rápidas y no fallen por milisegundos de retraso en la carga.

---

## 📊 Reportes y capturas de error

Cada vez que corres las pruebas, TestNG genera reportes automáticos en la carpeta `test-output/`:

* **`test-output/index.html`**: Reporte completo con el detalle de cada prueba, tiempos de ejecución y estado (aprobada/fallida).
* **`test-output/emailable-report.html`**: Un resumen ligero y limpio, ideal para compartir o revisar rápido el resultado.
* **`test-output/screenshots/`**: Si alguna prueba falla, aquí se almacenan las capturas `.png` que tomó el listener.

---

## 🚀 ¿Cómo ejecutar las pruebas?

### Desde la terminal con Maven
Para correr toda la suite de pruebas:
```bash
mvn clean test
```

Si quieres correr las pruebas sin que se abra la ventana de Chrome (modo headless, más rápido):
```bash
mvn clean test -Dheadless=true
```

Si quieres forzar una prueba para comprobar que las capturas de error se generen correctamente:
```bash
mvn clean test -DforceFailure=true
```


