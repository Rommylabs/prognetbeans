package cl.biblioteca.modelo;

/** Base de docentes y estudiantes. Un RUN identifica a un usuario. */
public abstract class Usuario {
    private String nombreCompleto;
    private String run;
    private char genero;
    private String carrera;
    private String isbnPrestado; // "0" indica disponible para préstamo.

    protected Usuario(String nombreCompleto, String run, char genero, String carrera, String isbnPrestado) {
        this.nombreCompleto = nombreCompleto;
        this.run = run;
        this.genero = genero;
        this.carrera = carrera;
        this.isbnPrestado = isbnPrestado;
    }
    public abstract int maximoDiasPrestamo();
    public abstract String getTipo();
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String v) { nombreCompleto = v; }
    public String getRun() { return run; }
    public char getGenero() { return genero; }
    public void setGenero(char v) { genero = v; }
    public String getCarrera() { return carrera; }
    public void setCarrera(String v) { carrera = v; }
    public String getIsbnPrestado() { return isbnPrestado; }
    public void setIsbnPrestado(String v) { isbnPrestado = v; }
    public boolean estaHabilitado() { return "0".equals(isbnPrestado); }
    @Override public String toString() { return getTipo()+" "+nombreCompleto+" ["+run+"]"; }
}
