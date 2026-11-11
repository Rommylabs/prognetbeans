package cl.biblioteca.modelo;
import java.time.LocalDate;
public final class Prestamo {
    private final String run, isbn;
    private final LocalDate fechaInicio, fechaVencimiento;
    private final int dias;
    public Prestamo(String run,String isbn,LocalDate inicio,LocalDate vencimiento,int dias){
        this.run=run;this.isbn=isbn;this.fechaInicio=inicio;this.fechaVencimiento=vencimiento;this.dias=dias;
    }
    public String getRun(){return run;}
    public String getIsbn(){return isbn;}
    public LocalDate getFechaInicio(){return fechaInicio;}
    public LocalDate getFechaVencimiento(){return fechaVencimiento;}
    public int getDias(){return dias;}
    @Override public String toString(){
        return "Prestamo[RUN="+run+", ISBN="+isbn+", inicio="+fechaInicio+", vence="+fechaVencimiento+", dias="+dias+"]";
    }
    /** Texto de la tarjeta que se imprime y se pega en el libro. */
    public String tarjeta(Usuario usuario, Libro libro){
        return "BIBLIOTECA UNAB — SEDE ANTONIO VARAS\n"+
          "TARJETA DE PRÉSTAMO\n----------------------------------------\n"+
          "Usuario: "+usuario.getNombreCompleto()+"\nRUN: "+run+"\n"+
          "Tipo: "+usuario.getTipo()+"\nLibro: "+libro.getTitulo()+"\nISBN: "+isbn+"\n"+
          "Inicio: "+fechaInicio+"\nPlazo: "+dias+" días\nDevolver antes o el: "+fechaVencimiento+"\n"+
          "Multa por atraso: $1.000 por día\n";
    }
}
