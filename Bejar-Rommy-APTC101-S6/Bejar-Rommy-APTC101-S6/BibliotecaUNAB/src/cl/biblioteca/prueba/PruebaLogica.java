package cl.biblioteca.prueba;

import cl.biblioteca.persistencia.RepositorioTexto;
import cl.biblioteca.servicio.BibliotecaServicio;
import cl.biblioteca.util.Validaciones;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

/**
 * Pruebas automatizadas de la lógica de negocio (sin JUnit, solo Java SE).
 * Ejecutar: java -cp build/classes cl.biblioteca.prueba.PruebaLogica
 * Termina con código 1 si alguna prueba falla.
 */
public final class PruebaLogica {
    private static int ok = 0;
    private static int fallos = 0;

    private static final String RUN_ESTUDIANTE = "11.111.111-1";
    private static final String RUN_DOCENTE = "12.345.678-5";
    private static final String ISBN10 = "0306406152";
    private static final String ISBN13 = "9780306406157";

    private interface Accion { void ejecutar() throws Exception; }

    private PruebaLogica() { }

    private static void debeFuncionar(String nombre, Accion a) {
        try { a.ejecutar(); registrar(nombre, true, ""); }
        catch (Exception e) { registrar(nombre, false, "falló: " + e); }
    }

    private static void debeRechazar(String nombre, Accion a) {
        try { a.ejecutar(); registrar(nombre, false, "se aceptó y debía rechazarse"); }
        catch (IllegalArgumentException | IllegalStateException e) { registrar(nombre, true, ""); }
        catch (Exception e) { registrar(nombre, false, "error inesperado: " + e); }
    }

    private static void igual(String nombre, Object esperado, Object obtenido) {
        boolean b = esperado.equals(obtenido);
        registrar(nombre, b, b ? "" : "esperado " + esperado + " y se obtuvo " + obtenido);
    }

    private static void registrar(String nombre, boolean paso, String detalle) {
        if (paso) { ok++; System.out.println("OK:    " + nombre); }
        else { fallos++; System.out.println("FALLA: " + nombre + " (" + detalle + ")"); }
    }

    private static BibliotecaServicio nuevoServicio(Path carpeta) throws Exception {
        return new BibliotecaServicio(new RepositorioTexto(carpeta));
    }

    /** Servicio con un estudiante, un docente y un libro de 2 ejemplares. */
    private static BibliotecaServicio escenario(Path carpeta) throws Exception {
        BibliotecaServicio s = nuevoServicio(carpeta);
        s.agregarUsuario("Estudiante", "Ana Pérez", RUN_ESTUDIANTE, "F", "Ingeniería", "", "");
        s.agregarUsuario("Docente", "Luis Soto", RUN_DOCENTE, "M", "", "Informática", "Magíster");
        s.agregarLibro(ISBN10, "Libro A", "Autor A", 2, 2, "");
        return s;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("PRUEBAS DE LÓGICA DE NEGOCIO — APTC101 S6");
        System.out.println("------------------------------------------");

        System.out.println("# Validaciones");
        debeFuncionar("RUN válido con puntos y guion", () -> Validaciones.runNormalizado(RUN_ESTUDIANTE));
        debeFuncionar("RUN válido sin puntos", () -> Validaciones.runNormalizado("12345678-5"));
        debeRechazar("RUN con dígito verificador incorrecto", () -> Validaciones.runNormalizado("12.345.678-9"));
        debeRechazar("RUN con formato inválido", () -> Validaciones.runNormalizado("abc"));
        debeFuncionar("Género M", () -> Validaciones.genero("M"));
        debeFuncionar("Género f (minúscula)", () -> Validaciones.genero("f"));
        debeRechazar("Género distinto de M/F", () -> Validaciones.genero("X"));
        debeFuncionar("ISBN de 10 caracteres", () -> Validaciones.isbn(ISBN10));
        debeFuncionar("ISBN-10 terminado en X", () -> Validaciones.isbn("080442957X"));
        debeFuncionar("ISBN de 13 dígitos con guiones", () -> Validaciones.isbn("978-0-306-40615-7"));
        debeRechazar("ISBN de 11 caracteres", () -> Validaciones.isbn("12345678901"));
        debeRechazar("ISBN de 12 caracteres", () -> Validaciones.isbn("123456789012"));
        debeRechazar("ISBN de 9 caracteres", () -> Validaciones.isbn("123456789"));
        debeRechazar("ISBN-13 con letra", () -> Validaciones.isbn("978030640615X"));

        System.out.println("# Usuarios");
        debeRechazar("RUN duplicado", () -> {
            BibliotecaServicio s = escenario(Files.createTempDirectory("t"));
            s.agregarUsuario("Estudiante", "Otra", "11111111-1", "F", "Derecho", "", "");
        });
        debeRechazar("Estudiante sin carrera", () -> nuevoServicio(Files.createTempDirectory("t"))
            .agregarUsuario("Estudiante", "Sin Carrera", RUN_ESTUDIANTE, "F", "", "", ""));
        debeRechazar("Docente sin profesión", () -> nuevoServicio(Files.createTempDirectory("t"))
            .agregarUsuario("Docente", "Sin Profesión", RUN_DOCENTE, "M", "", "", "Doctor"));
        debeRechazar("Eliminar usuario inexistente", () -> nuevoServicio(Files.createTempDirectory("t")).eliminarUsuario(RUN_ESTUDIANTE));

        System.out.println("# Libros");
        debeRechazar("ISBN de libro duplicado", () -> escenario(Files.createTempDirectory("t")).agregarLibro(ISBN10, "Otro", "X", 1, 1, ""));
        debeRechazar("Cantidad total cero", () -> escenario(Files.createTempDirectory("t")).agregarLibro(ISBN13, "X", "Y", 0, 0, ""));
        debeRechazar("Disponible cero", () -> escenario(Files.createTempDirectory("t")).agregarLibro(ISBN13, "X", "Y", 2, 0, ""));
        debeRechazar("Disponible mayor que el total", () -> escenario(Files.createTempDirectory("t")).agregarLibro(ISBN13, "X", "Y", 2, 3, ""));
        debeRechazar("Eliminar libro inexistente", () -> escenario(Files.createTempDirectory("t")).eliminarLibro(ISBN13));

        System.out.println("# Préstamo");
        debeRechazar("Plazo de estudiante de 11 días", () -> escenario(Files.createTempDirectory("t")).prestar(ISBN10, RUN_ESTUDIANTE, 11));
        debeRechazar("Plazo de docente de 21 días", () -> escenario(Files.createTempDirectory("t")).prestar(ISBN10, RUN_DOCENTE, 21));
        debeRechazar("Plazo de 0 días", () -> escenario(Files.createTempDirectory("t")).prestar(ISBN10, RUN_ESTUDIANTE, 0));
        debeRechazar("Libro inexistente", () -> escenario(Files.createTempDirectory("t")).prestar(ISBN13, RUN_ESTUDIANTE, 5));
        debeRechazar("Usuario inexistente", () -> escenario(Files.createTempDirectory("t")).prestar(ISBN10, "10.000.000-8", 5));
        debeFuncionar("Préstamo de estudiante por 10 días", () -> escenario(Files.createTempDirectory("t")).prestar(ISBN10, RUN_ESTUDIANTE, 10));
        debeFuncionar("Préstamo de docente por 20 días (polimorfismo)", () -> escenario(Files.createTempDirectory("t")).prestar(ISBN10, RUN_DOCENTE, 20));
        debeFuncionar("Vencimiento y stock tras el préstamo", () -> {
            BibliotecaServicio s = escenario(Files.createTempDirectory("t"));
            LocalDate hoy = LocalDate.of(2026, 11, 16);
            s.prestarEnFecha(ISBN10, RUN_ESTUDIANTE, 10, hoy);
            igual("  vencimiento = inicio + 10 días", hoy.plusDays(10), s.prestamos().iterator().next().getFechaVencimiento());
            igual("  disponibles baja de 2 a 1", 1, s.libro(ISBN10).getCantidadDisponible());
            igual("  usuario queda con el ISBN en 'préstamo'", ISBN10, s.usuario(RUN_ESTUDIANTE).getIsbnPrestado());
        });
        debeRechazar("Segundo préstamo del mismo usuario", () -> {
            BibliotecaServicio s = escenario(Files.createTempDirectory("t"));
            s.prestar(ISBN10, RUN_ESTUDIANTE, 5);
            s.prestar(ISBN10, RUN_ESTUDIANTE, 5);
        });
        debeRechazar("Libro sin ejemplares disponibles", () -> {
            BibliotecaServicio s = nuevoServicio(Files.createTempDirectory("t"));
            s.agregarUsuario("Estudiante", "Ana", RUN_ESTUDIANTE, "F", "Ing", "", "");
            s.agregarLibro(ISBN10, "Libro A", "Autor A", 1, 1, "");
            s.agregarUsuario("Docente", "Luis", RUN_DOCENTE, "M", "", "Informática", "Doctor");
            s.prestar(ISBN10, RUN_ESTUDIANTE, 5);
            s.prestar(ISBN10, RUN_DOCENTE, 5);
        });
        debeRechazar("Eliminar usuario con préstamo activo", () -> {
            BibliotecaServicio s = escenario(Files.createTempDirectory("t"));
            s.prestar(ISBN10, RUN_ESTUDIANTE, 5);
            s.eliminarUsuario(RUN_ESTUDIANTE);
        });
        debeRechazar("Eliminar libro prestado", () -> {
            BibliotecaServicio s = escenario(Files.createTempDirectory("t"));
            s.prestar(ISBN10, RUN_ESTUDIANTE, 5);
            s.eliminarLibro(ISBN10);
        });

        System.out.println("# Devolución");
        debeRechazar("Devolver con ISBN distinto al prestado", () -> {
            BibliotecaServicio s = escenario(Files.createTempDirectory("t"));
            s.agregarLibro(ISBN13, "Libro B", "Autor B", 1, 1, "");
            s.prestar(ISBN10, RUN_ESTUDIANTE, 5);
            s.devolver(ISBN13, RUN_ESTUDIANTE);
        });
        debeRechazar("Devolver sin préstamo activo", () -> escenario(Files.createTempDirectory("t")).devolver(ISBN10, RUN_ESTUDIANTE));
        debeFuncionar("Devolución a tiempo: sin multa, habilita y repone stock", () -> {
            BibliotecaServicio s = escenario(Files.createTempDirectory("t"));
            LocalDate hoy = LocalDate.of(2026, 11, 16);
            s.prestarEnFecha(ISBN10, RUN_ESTUDIANTE, 10, hoy);
            igual("  multa", 0L, s.devolverEnFecha(ISBN10, RUN_ESTUDIANTE, hoy.plusDays(10)));
            igual("  usuario habilitado", true, s.usuario(RUN_ESTUDIANTE).estaHabilitado());
            igual("  disponibles vuelve a 2", 2, s.libro(ISBN10).getCantidadDisponible());
        });
        debeFuncionar("Devolución con 3 días de atraso: multa $3.000", () -> {
            BibliotecaServicio s = escenario(Files.createTempDirectory("t"));
            LocalDate hoy = LocalDate.of(2026, 11, 16);
            s.prestarEnFecha(ISBN10, RUN_ESTUDIANTE, 10, hoy);
            igual("  multa", 3000L, s.devolverEnFecha(ISBN10, RUN_ESTUDIANTE, hoy.plusDays(13)));
        });
        debeFuncionar("Usuario puede pedir otro libro tras devolver", () -> {
            BibliotecaServicio s = escenario(Files.createTempDirectory("t"));
            s.prestar(ISBN10, RUN_ESTUDIANTE, 5);
            s.devolver(ISBN10, RUN_ESTUDIANTE);
            s.prestar(ISBN10, RUN_ESTUDIANTE, 5);
        });

        System.out.println("# Persistencia en archivos de texto");
        debeFuncionar("Reinicio del servicio recupera usuarios, libros y préstamos", () -> {
            Path carpeta = Files.createTempDirectory("t");
            BibliotecaServicio s = escenario(carpeta);
            s.prestar(ISBN10, RUN_ESTUDIANTE, 10);
            BibliotecaServicio reiniciado = nuevoServicio(carpeta);
            igual("  usuarios", 2, reiniciado.usuarios().size());
            igual("  libros", 1, reiniciado.libros().size());
            igual("  préstamos", 1, reiniciado.prestamos().size());
            igual("  disponibles del libro", 1, reiniciado.libro(ISBN10).getCantidadDisponible());
        });
        debeFuncionar("Las eliminaciones persisten", () -> {
            Path carpeta = Files.createTempDirectory("t");
            BibliotecaServicio s = escenario(carpeta);
            s.eliminarUsuario(RUN_DOCENTE);
            s.eliminarLibro(ISBN10);
            BibliotecaServicio reiniciado = nuevoServicio(carpeta);
            igual("  usuarios", 1, reiniciado.usuarios().size());
            igual("  libros", 0, reiniciado.libros().size());
        });

        System.out.println("------------------------------------------");
        System.out.println("Resultado: " + ok + " correctas, " + fallos + " con falla.");
        if (fallos > 0) System.exit(1);
    }
}
