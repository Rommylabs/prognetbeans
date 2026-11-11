package cl.biblioteca.modelo;
public final class Docente extends Usuario {
    private String profesion;
    private String grados;
    public Docente(String nombre, String run, char genero, String carrera, String isbn, String profesion, String grados) {
        super(nombre, run, genero, carrera, isbn); this.profesion=profesion; this.grados=grados;
    }
    public String getProfesion(){ return profesion; }
    public void setProfesion(String v){ profesion=v; }
    public String getGrados(){ return grados; }
    public void setGrados(String v){ grados=v; }
    @Override public int maximoDiasPrestamo() { return 20; }
    @Override public String getTipo() { return "Docente"; }
}
