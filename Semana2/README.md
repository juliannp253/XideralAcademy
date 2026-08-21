# Pokémon API REST

Bienvenido a mi proyecto backend de **Pokémon API**.

Este proyecto llevo a cado refactorización y transformación un proyecto incial: pasando de configuraciones manuales y código repetitivo a una arquitectura moderna, automatizada 
y limpia utilizando el ecosistema de **Spring Data** y **Docker**.

---

## 📑 Tabla de Contenidos
1. [Acerca del Proyecto](#-acerca-del-proyecto)
2. [Evolución y Refactorización (Los Cambios)](#-evolución-y-refactorización)
3. [Arquitectura de los Proyectos (JPA vs MongoDB)](#-arquitectura-de-los-proyectos)
4. [Ejecución y Despliegue con Docker](#-ejecución-y-despliegue)

---

## 🚀 Acerca del Proyecto
El objetivo inicial era gestionar una API de empleados, pero decidí adaptar el dominio de negocio para gestionar **Pokémon**. Este cambio requirió reestructurar los Modelos (Entidades), Controladores y la base de datos para manejar atributos como `name`, `type` y `level`.

El repositorio contiene dos versiones del mismo proyecto, las cuales demuestran el uso de bases de datos relacionales (SQL) y no relacionales (NoSQL).

---

## 🛠️ Evolución y Refactorización
Dentro de este proyecto apliqué las siguientes mejoras sobre el código base original:

* **1. Modelado de Dominio Personalizado:**
  Se reemplazó la entidad genérica `Employee` por `Pokemon`. Se ajustaron los `@RestController` y los DTOs para que todas las rutas y respuestas JSON coincidieran con la nueva lógica de negocio.
* **2. Automatización de Infraestructura (Docker Compose):**
  En lugar de instalar bases de datos locales o ejecutar scripts manualmente, implementé un `compose.yaml` en cada proyecto. Esto levanta los contenedores y ejecuta los scripts de inicialización (`init.sql` e `init.js`) automáticamente, poblando la base de datos desde el segundo cero.
* **3. Implementación de Spring Data:**
  Se eliminó por completo la declaración manual de métodos repetitivos (como `save`, `findById`, `findAll`, `delete`) en la capa de acceso a datos (DAO) y en los servicios. Al heredar de las interfaces de Spring Data, el framework ahora genera estas consultas dinámicamente, resultando en un código mucho más limpio y mantenible.

---

## 📂 Arquitectura de los Proyectos
El repositorio se divide en dos implementaciones independientes que exponen los mismos endpoints pero usan distinta tecnología de persistencia:

### 1. Spring Data JPA (MySQL)
* **Ubicación:** Carpeta del proyecto JPA.
* **Tecnología:** Utiliza Spring Data JPA y Hibernate.
* **Showcase Técnico:** Demuestra el uso de anotaciones relacionales (`@Entity`, `@Table`, `@Id`, `@Column`) y cómo `JpaRepository` mapea los objetos Pokémon a tablas SQL de manera automática.

### 2. Spring Data MongoDB (NoSQL)
* **Ubicación:** Carpeta del proyecto Mongo.
* **Tecnología:** Utiliza Spring Data MongoDB.
* **Showcase Técnico:** Demuestra la adaptabilidad a bases de datos orientadas a documentos. Se reemplazan las tablas por colecciones usando `@Document(collection = "pokemon")` y el repositorio hereda de `MongoRepository`, manejando JSONs nativos.

---

## 🐳 Ejecución y Despliegue
Ambos proyectos están contenerizados para facilitar el inicio de los proyectos al clonar el repositorio.

1. Navega a la raíz del proyecto que desees probar (JPA o Mongo).
2. Levanta la base de datos con sus datos iniciales ejecutando:
   ```bash
   docker-compose up -d
   ```
3. Ejecuta la aplicación de Spring Boot desde tu IDE.
4. Prueba los endpoints (ej. GET http://localhost:8070/api/pokemons) a través de Postman o tu navegador.