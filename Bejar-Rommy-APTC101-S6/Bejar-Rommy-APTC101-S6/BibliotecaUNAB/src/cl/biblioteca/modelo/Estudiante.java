package cl.biblioteca.modelo;
public final class Estudiante extends Usuario {
    public Estudiante(String nombre, String run, char genero, String carrera, String isbn) {
        super(nombre, run, genero, carrera, isbn);
    }
    @Override public int maximoDiasPrestamo() { return 10; }
    @Override public String getTipo() { return "Estudiante"; }
}
