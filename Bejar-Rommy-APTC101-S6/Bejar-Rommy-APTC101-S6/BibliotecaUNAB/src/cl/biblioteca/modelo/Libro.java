package cl.biblioteca.modelo;

/** Libro del catálogo. El ISBN lo identifica y no cambia una vez creado. */
public final class Libro {
    private final String isbn;
    private String titulo;
    private String autor;
    private String imagen; // Ruta del archivo de imagen (puede ser vacía).
    private int cantidadTotal;
    private int cantidadDisponible;

    public Libro(String isbn, String titulo, String autor, int total, int disponible, String imagen) {
        this.isbn = isbn;
        this.titulo = titulo;
        this.autor = autor;
        this.cantidadTotal = total;
        this.cantidadDisponible = disponible;
        this.imagen = imagen;
    }

    public String getIsbn() { return isbn; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String v) { titulo = v; }
    public String getAutor() { return autor; }
    public void setAutor(String v) { autor = v; }
    public String getImagen() { return imagen; }
    public void setImagen(String v) { imagen = v; }
    public int getCantidadTotal() { return cantidadTotal; }
    public void setCantidadTotal(int v) { cantidadTotal = v; }
    public int getCantidadDisponible() { return cantidadDisponible; }
    public void setCantidadDisponible(int v) { cantidadDisponible = v; }

    @Override public String toString() { return titulo + " (" + isbn + ") " + cantidadDisponible + "/" + cantidadTotal; }
}
