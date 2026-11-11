package cl.biblioteca.persistencia;
import cl.biblioteca.modelo.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
/** Repositorio de texto plano delimitado por tabulaciones con escape de campos. */
public final class RepositorioTexto {
    private final Path carpeta;
    public RepositorioTexto(Path carpeta){this.carpeta=carpeta;}
    private static String enc(String x){return x.replace("%","%25").replace("\t","%09").replace("\r","%0D").replace("\n","%0A");}
    private static String dec(String x){return x.replace("%0A","\n").replace("%0D","\r").replace("%09","\t").replace("%25","%");}
    private static String linea(String... vals){StringJoiner j=new StringJoiner("\t");for(String v:vals)j.add(enc(v));return j.toString();}
    private static String[] campos(String linea){String[] r=linea.split("\t",-1);for(int i=0;i<r.length;i++)r[i]=dec(r[i]);return r;}
    public List<Usuario> leerUsuarios() throws IOException {
        List<Usuario> r=new ArrayList<>();
        for(String linea:leer("usuarios.txt")){
            if(linea.trim().isEmpty())continue;String[] c=campos(linea);
            if(c.length!=8)throw new IOException("Registro de usuario mal formado.");
            if("D".equals(c[0]))r.add(new Docente(c[1],c[2],c[3].charAt(0),c[4],c[5],c[6],c[7]));
            else if("E".equals(c[0]))r.add(new Estudiante(c[1],c[2],c[3].charAt(0),c[4],c[5]));
            else throw new IOException("Tipo de usuario desconocido.");
        }
        return r;
    }
    public List<Libro> leerLibros() throws IOException {
        List<Libro> r=new ArrayList<>();for(String linea:leer("libros.txt")){
            if(linea.trim().isEmpty())continue;String[] c=campos(linea);
            if(c.length!=6)throw new IOException("Registro de libro mal formado.");
            r.add(new Libro(c[0],c[1],c[2],Integer.parseInt(c[3]),Integer.parseInt(c[4]),c[5]));
        }return r;
    }
    public List<Prestamo> leerPrestamos() throws IOException {
        List<Prestamo> r=new ArrayList<>();for(String linea:leer("prestamos.txt")){
            if(linea.trim().isEmpty())continue;String[] c=campos(linea);
            if(c.length!=5)throw new IOException("Registro de préstamo mal formado.");
            r.add(new Prestamo(c[0],c[1],LocalDate.parse(c[2]),LocalDate.parse(c[3]),Integer.parseInt(c[4])));
        }return r;
    }
    private List<String> leer(String archivo)throws IOException {
        Path ruta=carpeta.resolve(archivo);
        if(!Files.exists(ruta))return Collections.emptyList();
        return Files.readAllLines(ruta,StandardCharsets.UTF_8);
    }
    private void escribir(String nombre,List<String> lineas)throws IOException {
        Files.createDirectories(carpeta);Path destino=carpeta.resolve(nombre);
        Path tmp=Files.createTempFile(carpeta,"biblioteca-",".tmp");
        try {Files.write(tmp,lineas,StandardCharsets.UTF_8);
            try{Files.move(tmp,destino,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}
            catch(AtomicMoveNotSupportedException e){Files.move(tmp,destino,StandardCopyOption.REPLACE_EXISTING);}
        }finally{Files.deleteIfExists(tmp);}
    }
    public void guardarUsuarios(Collection<Usuario> usuarios)throws IOException {
        List<String> l=new ArrayList<>();for(Usuario u:usuarios){
            Docente d=u instanceof Docente?(Docente)u:null;
            l.add(linea(d==null?"E":"D",u.getNombreCompleto(),u.getRun(),String.valueOf(u.getGenero()),
                 u.getCarrera(),u.getIsbnPrestado(),d==null?"":d.getProfesion(),d==null?"":d.getGrados()));
        }escribir("usuarios.txt",l);
    }
    public void guardarLibros(Collection<Libro> libros)throws IOException {
        List<String> l=new ArrayList<>();for(Libro b:libros)l.add(linea(b.getIsbn(),b.getTitulo(),b.getAutor(),
             String.valueOf(b.getCantidadTotal()),String.valueOf(b.getCantidadDisponible()),b.getImagen()));
        escribir("libros.txt",l);
    }
    public void guardarPrestamos(Collection<Prestamo> prestamos)throws IOException {
        List<String> l=new ArrayList<>();for(Prestamo p:prestamos)l.add(linea(p.getRun(),p.getIsbn(),
             p.getFechaInicio().toString(),p.getFechaVencimiento().toString(),String.valueOf(p.getDias())));
        escribir("prestamos.txt",l);
    }
}
