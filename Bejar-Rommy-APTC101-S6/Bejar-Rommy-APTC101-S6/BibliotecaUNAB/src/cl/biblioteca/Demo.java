package cl.biblioteca;

import cl.biblioteca.modelo.*;
import cl.biblioteca.persistencia.RepositorioTexto;
import cl.biblioteca.servicio.BibliotecaServicio;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

/**
 * Programa de demostración por consola (sin interfaz gráfica).
 * Crea los objetos según las relaciones del diagrama y ejecuta los requerimientos:
 * usuarios, libros, préstamo, devolución con multa y eliminaciones.
 * Los archivos se escriben en una carpeta temporal, no en los datos reales.
 */
public final class Demo {
    private Demo() { }

    /** Operación que puede fallar; permite mostrar el resultado de cada caso. */
    private interface Operacion { Object ejecutar() throws Exception; }

    private static void caso(String descripcion, Operacion op) {
        try {
            Object r = op.ejecutar();
            System.out.println("  [OK]       " + descripcion + (r == null ? "" : " -> " + r));
        } catch (Exception e) {
            System.out.println("  [RECHAZADO] " + descripcion + " -> " + e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        Path carpeta = Files.createTempDirectory("biblioteca-demo");
        // Asociación: el repositorio se crea afuera y se entrega al servicio.
        RepositorioTexto repo = new RepositorioTexto(carpeta);
        // El servicio agrupa usuarios y libros (agregación) y administra los préstamos (composición).
        BibliotecaServicio servicio = new BibliotecaServicio(repo);

        System.out.println("1. Usuarios (herencia: Estudiante y Docente extienden Usuario)");
        caso("Crear estudiante", () -> { servicio.agregarUsuario("Estudiante", "Ana Pérez", "11.111.111-1", "F", "Ingeniería", "", ""); return null; });
        caso("Crear docente", () -> { servicio.agregarUsuario("Docente", "Luis Soto", "12.345.678-5", "M", "", "Informática", "Magíster"); return null; });
        caso("RUN duplicado", () -> { servicio.agregarUsuario("Estudiante", "Otra Persona", "11111111-1", "F", "Derecho", "", ""); return null; });
        caso("Dígito verificador incorrecto", () -> { servicio.agregarUsuario("Estudiante", "Pedro Gil", "12.345.678-9", "M", "Derecho", "", ""); return null; });
        caso("Género distinto de M/F", () -> { servicio.agregarUsuario("Estudiante", "Pedro Gil", "10.000.000-8", "X", "Derecho", "", ""); return null; });
        for (Usuario u : servicio.usuarios()) System.out.println("  " + u + " | máximo " + u.maximoDiasPrestamo() + " días");

        System.out.println("2. Libros");
        caso("Crear libro (2 ejemplares)", () -> { servicio.agregarLibro("978-0306406157", "Programación en Java", "A. Autor", 2, 2, ""); return null; });
        caso("Crear segundo libro (1 ejemplar)", () -> { servicio.agregarLibro("0306406152", "Estructuras de Datos", "B. Autor", 1, 1, ""); return null; });
        caso("ISBN duplicado", () -> { servicio.agregarLibro("9780306406157", "Otro título", "B. Autor", 1, 1, ""); return null; });
        caso("ISBN de 11 caracteres", () -> { servicio.agregarLibro("12345678901", "Inválido", "C. Autor", 1, 1, ""); return null; });
        caso("Cantidad disponible mayor a la total", () -> { servicio.agregarLibro("9780132350884", "Inválido", "C. Autor", 1, 2, ""); return null; });

        System.out.println("3. Préstamos (polimorfismo: maximoDiasPrestamo() según el tipo de usuario)");
        caso("Estudiante pide 11 días", () -> servicio.prestar("9780306406157", "11.111.111-1", 11));
        caso("Estudiante pide 10 días", () -> servicio.prestar("9780306406157", "11.111.111-1", 10));
        caso("Estudiante pide un segundo libro", () -> servicio.prestar("9780306406157", "11.111.111-1", 5));
        caso("Docente pide 20 días", () -> servicio.prestar("9780306406157", "12.345.678-5", 20));
        caso("Libro sin ejemplares disponibles", () -> servicio.prestar("9780306406157", "12.345.678-5", 5));
        Prestamo p = servicio.prestamos().iterator().next();
        System.out.println(p.tarjeta(servicio.usuario(p.getRun()), servicio.libro(p.getIsbn())));

        System.out.println("4. Devoluciones");
        caso("Devolver con ISBN que no corresponde", () -> servicio.devolver("0306406152", "11.111.111-1"));
        caso("Devolver 3 días tarde (multa en $)", () -> servicio.devolverEnFecha("9780306406157", "11.111.111-1", LocalDate.now().plusDays(13)));
        caso("Devolver a tiempo (multa en $)", () -> servicio.devolver("9780306406157", "12.345.678-5"));

        System.out.println("5. Eliminaciones");
        caso("Eliminar usuario", () -> { servicio.eliminarUsuario("11.111.111-1"); return null; });
        caso("Eliminar usuario inexistente", () -> { servicio.eliminarUsuario("11.111.111-1"); return null; });
        caso("Eliminar libro", () -> { servicio.eliminarLibro("9780306406157"); return null; });
        caso("Eliminar libro inexistente", () -> { servicio.eliminarLibro("9780306406157"); return null; });

        System.out.println("6. Contenido de los archivos de texto");
        for (String f : new String[]{"usuarios.txt", "libros.txt", "prestamos.txt"})
            System.out.println("  " + f + ": " + Files.readAllLines(carpeta.resolve(f)));
    }
}
