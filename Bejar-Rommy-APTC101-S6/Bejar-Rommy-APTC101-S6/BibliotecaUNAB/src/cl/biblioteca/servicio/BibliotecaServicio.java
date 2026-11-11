package cl.biblioteca.servicio;
import cl.biblioteca.modelo.*;
import cl.biblioteca.persistencia.RepositorioTexto;
import cl.biblioteca.util.Validaciones;
import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
/** Reglas de negocio; independientes de la interfaz Swing. */
public final class BibliotecaServicio {
    private final Map<String,Usuario> usuarios=new LinkedHashMap<>();
    private final Map<String,Libro> libros=new LinkedHashMap<>();
    private final Map<String,Prestamo> prestamos=new LinkedHashMap<>();
    private final RepositorioTexto repo;
    public BibliotecaServicio(RepositorioTexto repo)throws IOException {
        this.repo=repo;
        for(Usuario u:repo.leerUsuarios())usuarios.put(u.getRun(),u);
        for(Libro l:repo.leerLibros())libros.put(l.getIsbn(),l);
        for(Prestamo p:repo.leerPrestamos())prestamos.put(p.getRun(),p);
    }
    public Collection<Usuario> usuarios(){return Collections.unmodifiableCollection(usuarios.values());}
    public Collection<Libro> libros(){return Collections.unmodifiableCollection(libros.values());}
    public Collection<Prestamo> prestamos(){return Collections.unmodifiableCollection(prestamos.values());}
    public Usuario usuario(String run){return usuarios.get(Validaciones.runNormalizado(run));}
    public Libro libro(String isbn){return libros.get(Validaciones.isbn(isbn));}
    public void agregarUsuario(String tipo,String nombre,String run,String genero,String carrera,String profesion,String grados)throws IOException {
        String r=Validaciones.runNormalizado(run);
        if(usuarios.containsKey(r))throw new IllegalArgumentException("RUN ya registrado.");
        Usuario u=nuevoUsuario(tipo,nombre,r,genero,carrera,"0",profesion,grados);
        usuarios.put(r,u);try{repo.guardarUsuarios(usuarios.values());}catch(IOException e){usuarios.remove(r);throw e;}
    }
    private Usuario nuevoUsuario(String tipo,String nombre,String run,String genero,String carrera,String prestamo,String profesion,String grados){
        String nom=Validaciones.requerido(nombre,"nombre completo");char gen=Validaciones.genero(genero);
        if("Docente".equalsIgnoreCase(tipo))return new Docente(nom,run,gen,carrera==null?"":carrera.trim(),prestamo,
                       Validaciones.requerido(profesion,"profesión"),grados==null?"":grados.trim());
        if("Estudiante".equalsIgnoreCase(tipo))return new Estudiante(nom,run,gen,
                       Validaciones.requerido(carrera,"carrera"),prestamo);
        throw new IllegalArgumentException("Tipo de usuario incorrecto.");
    }
    public void editarUsuario(String run,String tipo,String nombre,String genero,String carrera,String profesion,String grados)throws IOException {
        String r=Validaciones.runNormalizado(run);Usuario anterior=usuarios.get(r);
        if(anterior==null)throw new IllegalArgumentException("Usuario inexistente.");
        Usuario reemplazo=nuevoUsuario(tipo,nombre,r,genero,carrera,anterior.getIsbnPrestado(),profesion,grados);
        if(!anterior.estaHabilitado()&&reemplazo.maximoDiasPrestamo()<prestamos.get(r).getDias())
           throw new IllegalArgumentException("No se puede cambiar el tipo: excede el plazo del préstamo activo.");
        usuarios.put(r,reemplazo);try{repo.guardarUsuarios(usuarios.values());}catch(IOException e){usuarios.put(r,anterior);throw e;}
    }
    public void eliminarUsuario(String run)throws IOException {
        String r=Validaciones.runNormalizado(run);Usuario u=usuarios.get(r);
        if(u==null)throw new IllegalArgumentException("Usuario inexistente.");
        if(!u.estaHabilitado())throw new IllegalArgumentException("No se puede eliminar: tiene un préstamo activo.");
        usuarios.remove(r);try{repo.guardarUsuarios(usuarios.values());}catch(IOException e){usuarios.put(r,u);throw e;}
    }
    public void agregarLibro(String isbn,String titulo,String autor,int total,int disponible,String imagen)throws IOException {
        String id=Validaciones.isbn(isbn);
        if(libros.containsKey(id))throw new IllegalArgumentException("ISBN ya registrado.");
        if(total<=0||disponible<=0||disponible>total)throw new IllegalArgumentException("Cantidad total > 0 y disponibilidad entre 1 y total.");
        Libro b=new Libro(id,Validaciones.requerido(titulo,"título"),Validaciones.requerido(autor,"autor"),total,disponible,imagen==null?"":imagen);
        libros.put(id,b);try{repo.guardarLibros(libros.values());}catch(IOException e){libros.remove(id);throw e;}
    }
    public void eliminarLibro(String isbn)throws IOException {
        String id=Validaciones.isbn(isbn);Libro libro=libros.get(id);
        if(libro==null)throw new IllegalArgumentException("Libro inexistente.");
        for(Prestamo p:prestamos.values())if(id.equals(p.getIsbn()))throw new IllegalArgumentException("No se puede eliminar: libro actualmente prestado.");
        libros.remove(id);try{repo.guardarLibros(libros.values());}catch(IOException e){libros.put(id,libro);throw e;}
    }
    public Prestamo prestar(String isbn,String run,int dias)throws IOException {return prestarEnFecha(isbn,run,dias,LocalDate.now());}
    public Prestamo prestarEnFecha(String isbn,String run,int dias,LocalDate fecha)throws IOException {
        String id=Validaciones.isbn(isbn),r=Validaciones.runNormalizado(run);
        Libro l=libros.get(id);if(l==null)throw new IllegalArgumentException("Libro inexistente.");
        if(l.getCantidadDisponible()<1)throw new IllegalArgumentException("No hay ejemplares disponibles.");
        Usuario u=usuarios.get(r);if(u==null)throw new IllegalArgumentException("Usuario inexistente.");
        if(!u.estaHabilitado()||prestamos.containsKey(r))throw new IllegalArgumentException("Usuario no habilitado: ya tiene préstamo.");
        if(dias<1||dias>u.maximoDiasPrestamo())throw new IllegalArgumentException("Plazo: 1 a "+u.maximoDiasPrestamo()+" días para "+u.getTipo()+".");
        Prestamo p=new Prestamo(r,id,fecha,fecha.plusDays(dias),dias);
        u.setIsbnPrestado(id);l.setCantidadDisponible(l.getCantidadDisponible()-1);prestamos.put(r,p);
        try{guardarTodo();}catch(IOException e){u.setIsbnPrestado("0");l.setCantidadDisponible(l.getCantidadDisponible()+1);prestamos.remove(r);try{guardarTodo();}catch(IOException ignored){}throw e;}
        return p;
    }
    public long devolver(String isbn,String run)throws IOException {return devolverEnFecha(isbn,run,LocalDate.now());}
    public long devolverEnFecha(String isbn,String run,LocalDate fecha)throws IOException {
        String id=Validaciones.isbn(isbn),r=Validaciones.runNormalizado(run);
        Libro l=libros.get(id);if(l==null)throw new IllegalArgumentException("Libro inexistente.");
        Usuario u=usuarios.get(r);if(u==null)throw new IllegalArgumentException("Usuario inexistente.");
        Prestamo p=prestamos.get(r);
        if(p==null||!id.equals(u.getIsbnPrestado())||!id.equals(p.getIsbn()))
            throw new IllegalArgumentException("El ISBN no coincide con el préstamo activo del usuario.");
        if(l.getCantidadDisponible()>=l.getCantidadTotal())throw new IllegalStateException("Inventario inconsistente.");
        long atraso=Math.max(0,ChronoUnit.DAYS.between(p.getFechaVencimiento(),fecha));
        u.setIsbnPrestado("0");l.setCantidadDisponible(l.getCantidadDisponible()+1);prestamos.remove(r);
        try{guardarTodo();}catch(IOException e){u.setIsbnPrestado(id);l.setCantidadDisponible(l.getCantidadDisponible()-1);prestamos.put(r,p);try{guardarTodo();}catch(IOException ignored){}throw e;}
        return atraso*1000L;
    }
    private void guardarTodo()throws IOException {
        // Cada archivo se reemplaza de forma atómica; ante un error intentamos restaurar el conjunto.
        repo.guardarUsuarios(usuarios.values());repo.guardarLibros(libros.values());repo.guardarPrestamos(prestamos.values());
    }
}
