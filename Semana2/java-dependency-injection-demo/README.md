# Inyección de Dependencias (Ejemplo Puro en Java)

Este proyecto explica cómo funciona la **Inyección de Dependencias (DI)** en un entorno puramente de Java, sin utilizar ningún framework de Inversión de Control (IoC) como Spring.

## 🚀 Conceptos Clave

* **Dependencia:** Un objeto que otra clase necesita para funcionar. En nuestro caso, `PokemonService` necesita un `PokemonRepository` para acceder a los datos.
* **Inyección de Dependencias:** El proceso de suministrar (inyectar) un objeto externo (dependencia) a una clase, en lugar de que la clase lo cree por sí misma usando `new`.

## 🛠️ ¿Cómo se implementa aquí?

1. **Inversión de Control (IoC):** En `PokemonService.java`, no instanciamos `PokemonRepositoryImpl` directamente. En cambio, declaramos una dependencia hacia la *interfaz* `PokemonRepository`.
2. **Inyección por Constructor:** Pasamos la implementación del repositorio al constructor de `PokemonService`.
3. **En la clase principal (`Main.java`):** Nuestra clase `Main` se encarga de crear el `PokemonRepositoryImpl`, inyectarlo dentro de `PokemonService` y luego ejecutar el programa. 

## ▶️ Cómo ejecutar

Puedes ejecutar este proyecto fácilmente desde tu IDE favorito (como IntelliJ IDEA, Eclipse, o VSCode) ubicando el archivo `Main.java` y ejecutando su método `main`.

La salida esperada por consola demostrará que el servicio es capaz de consultar y guardar Pokémons usando el repositorio que le fue inyectado:

```
Iniciando aplicacion sin Spring (Inyeccion de Dependencias Manual)...

--- Lista de Pokemons ---
Pokemon{name='Pikachu', type='Electrico'}
Pokemon{name='Charmander', type='Fuego'}
-------------------------
Pokemon guardado en la base de datos (en memoria): Bulbasaur
Pokemon guardado en la base de datos (en memoria): Squirtle

--- Lista de Pokemons ---
Pokemon{name='Pikachu', type='Electrico'}
Pokemon{name='Charmander', type='Fuego'}
Pokemon{name='Bulbasaur', type='Planta/Veneno'}
Pokemon{name='Squirtle', type='Agua'}
-------------------------
```
