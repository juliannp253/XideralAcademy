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