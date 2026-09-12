# Selenium Framework POM - recorrido completo

Proyecto basado en la estructura clara del framework POM de referencia:

- `base.BasePage`: operaciones reutilizables de todas las páginas.
- `utils.DriverFactory`: creación y configuración de Chrome.
- `pages.AutomationPracticePage`: localizadores y acciones de la página.
- `base.BaseTest`: apertura y cierre del navegador en TestNG.
- `tests.AutomationPracticeTest`: escenarios y validaciones.
- `listeners.ScreenshotListener`: captura automática cuando falla una prueba.

## Cobertura

Formulario, inputs, textarea, radio buttons, checkboxes, dropdowns, fechas,
carga de archivo, tabla, Alert, Confirm, Prompt, hover, doble clic,
drag-and-drop y nueva pestaña.

## Eclipse

1. Importar como `Existing Maven Project`.
2. Ejecutar `Maven > Update Project`.
3. Abrir `testng.xml`.
4. Ejecutar `Run As > TestNG Suite`.

## Maven

```bash
mvn clean test
```

Ejecución sin mostrar Chrome:

```bash
mvn clean test -Dheadless=true
```

Generar una falla intencional y comprobar la captura:

```bash
mvn clean test -DforceFailure=true
```

Resultados:

- `test-output/index.html`
- `test-output/emailable-report.html`
- `test-output/screenshots/`

Selenium Manager obtiene automáticamente el ChromeDriver compatible.
