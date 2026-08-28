import watcher.DirectorioWatcher;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== DAEMON INTEGRADOR EMPRESARIAL INICIADO ===");

        DirectorioWatcher watcher = new DirectorioWatcher();
        watcher.iniciarVigilancia();
    }
}