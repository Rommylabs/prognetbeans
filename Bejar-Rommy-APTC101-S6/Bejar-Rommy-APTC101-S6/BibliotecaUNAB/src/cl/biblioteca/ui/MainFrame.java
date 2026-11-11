package cl.biblioteca.ui;
import cl.biblioteca.modelo.*;
import cl.biblioteca.servicio.BibliotecaServicio;
import cl.biblioteca.util.Validaciones;
import java.awt.*;
import java.io.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
/** Ventana principal JFrame; formularios secundarios implementados con JDialog. */
public final class MainFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private final transient BibliotecaServicio servicio;
    private final DefaultTableModel usuariosModel=modelo("Tipo","Nombre","RUN","Género","Carrera","Préstamo","Profesión","Grados");
    private final DefaultTableModel librosModel=modelo("ISBN","Título","Autor","Total","Disponibles","Imagen");
    private final DefaultTableModel prestamosModel=modelo("RUN","ISBN","Inicio","Vencimiento","Días");
    private final JTabbedPane pestañas=new JTabbedPane();
    public MainFrame(BibliotecaServicio servicio){
        super("Biblioteca UNAB | Antonio Varas");this.servicio=servicio;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);setSize(1120,680);setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        JLabel header=new JLabel("  BIBLIOTECA UNAB — SISTEMA DE GESTIÓN",SwingConstants.LEFT);
        header.setFont(header.getFont().deriveFont(Font.BOLD,19f));header.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        add(header,BorderLayout.NORTH);add(pestañas,BorderLayout.CENTER);
        pestañas.addTab("Inicio",inicio());
        pestañas.addTab("Usuarios",usuariosPanel());
        pestañas.addTab("Libros",librosPanel());
        pestañas.addTab("Préstamos y devoluciones",prestamosPanel());
        JButton home=new JButton("Volver al inicio");home.addActionListener(e->pestañas.setSelectedIndex(0));
        JPanel footer=new JPanel(new FlowLayout(FlowLayout.RIGHT));footer.add(home);add(footer,BorderLayout.SOUTH);
        refrescar();
    }
    private static DefaultTableModel modelo(String... columnas){return new DefaultTableModel(columnas,0){@Override public boolean isCellEditable(int r,int c){return false;}};}
    private JPanel inicio(){
        JPanel p=new JPanel(new GridBagLayout());JPanel centro=new JPanel(new GridLayout(0,1,10,10));
        JLabel desc=new JLabel("Seleccione un módulo para administrar la biblioteca",SwingConstants.CENTER);
        desc.setFont(desc.getFont().deriveFont(17f));centro.add(desc);
        for(int i=1;i<=3;i++){final int tab=i;JButton b=new JButton(new String[]{"","Administrar usuarios","Administrar libros","Gestionar préstamos / devoluciones"}[i]);
            b.addActionListener(e->pestañas.setSelectedIndex(tab));centro.add(b);}
        p.add(centro);return p;
    }
    private JPanel usuariosPanel(){
        JPanel p=new JPanel(new BorderLayout());JTable t=new JTable(usuariosModel);t.setAutoCreateRowSorter(true);p.add(new JScrollPane(t),BorderLayout.CENTER);
        JPanel bar=new JPanel(new FlowLayout(FlowLayout.LEFT));
        boton(bar,"Crear",()->formularioUsuario(null));
        boton(bar,"Editar seleccionado",()->{int r=t.getSelectedRow();if(r<0)throw new IllegalArgumentException("Seleccione usuario.");
            String run=String.valueOf(t.getValueAt(r,2));formularioUsuario(servicio.usuario(run));});
        boton(bar,"Eliminar seleccionado",()->{int r=t.getSelectedRow();if(r<0)throw new IllegalArgumentException("Seleccione usuario.");
            String run=String.valueOf(t.getValueAt(r,2));confirmar("¿Eliminar usuario "+run+"?",()->servicio.eliminarUsuario(run));});
        p.add(bar,BorderLayout.NORTH);return p;
    }
    private JPanel librosPanel(){
        JPanel p=new JPanel(new BorderLayout());JTable t=new JTable(librosModel);t.setAutoCreateRowSorter(true);p.add(new JScrollPane(t),BorderLayout.CENTER);
        JPanel bar=new JPanel(new FlowLayout(FlowLayout.LEFT));boton(bar,"Crear libro",this::formularioLibro);
        boton(bar,"Eliminar seleccionado",()->{int r=t.getSelectedRow();if(r<0)throw new IllegalArgumentException("Seleccione libro.");
            String isbn=String.valueOf(t.getValueAt(r,0));confirmar("¿Eliminar libro "+isbn+"?",()->servicio.eliminarLibro(isbn));});
        p.add(bar,BorderLayout.NORTH);return p;
    }
    private JPanel prestamosPanel(){
        JPanel p=new JPanel(new BorderLayout());JTable t=new JTable(prestamosModel);p.add(new JScrollPane(t),BorderLayout.CENTER);
        JPanel bar=new JPanel(new FlowLayout(FlowLayout.LEFT));boton(bar,"Nuevo préstamo",this::formularioPrestamo);
        boton(bar,"Registrar devolución",this::formularioDevolucion);
        boton(bar,"Ver tarjeta del préstamo",()->{
            int r=t.getSelectedRow();if(r<0)throw new IllegalArgumentException("Seleccione préstamo.");
            String run=String.valueOf(t.getValueAt(r,0));for(Prestamo pr:servicio.prestamos())if(pr.getRun().equals(run)){
                mostrarTarjeta(pr);return;}throw new IllegalArgumentException("Préstamo no encontrado.");
        });p.add(bar,BorderLayout.NORTH);return p;
    }
    private interface Accion{void ejecutar()throws Exception;}
    private void boton(JPanel bar,String texto,Accion a){JButton b=new JButton(texto);b.addActionListener(e->{try{a.ejecutar();refrescar();}catch(Exception ex){error(ex);}});bar.add(b);}
    private void confirmar(String pregunta,Accion accion)throws Exception{
        if(JOptionPane.showConfirmDialog(this,pregunta,"Confirmación",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION)accion.ejecutar();
    }
    private static JTextField campo(String s){return new JTextField(s==null?"":s,22);}
    private void formularioUsuario(Usuario anterior){
        boolean editar=anterior!=null;
        JDialog dialog=new JDialog(this,editar?"Editar usuario":"Nuevo usuario",true);
        JPanel form=new JPanel(new GridLayout(0,2,8,8));form.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
        JComboBox<String> tipo=new JComboBox<>(new String[]{"Estudiante","Docente"});
        if(editar)tipo.setSelectedItem(anterior.getTipo());
        JTextField nombre=campo(editar?anterior.getNombreCompleto():""),run=campo(editar?anterior.getRun():""),
            genero=campo(editar?String.valueOf(anterior.getGenero()):""),carrera=campo(editar?anterior.getCarrera():""),
            profesion=campo(anterior instanceof Docente?((Docente)anterior).getProfesion():""),
            grados=campo(anterior instanceof Docente?((Docente)anterior).getGrados():"");
        run.setEditable(!editar);
        String[] etiquetas={"Tipo","Nombre completo","RUN (con dígito)","Género (M/F)","Carrera","Profesión (docente)","Grados (magíster/doctor)"};
        JComponent[] comps={tipo,nombre,run,genero,carrera,profesion,grados};
        for(int i=0;i<etiquetas.length;i++){form.add(new JLabel(etiquetas[i]));form.add(comps[i]);}
        JButton guardar=new JButton("Guardar");guardar.addActionListener(e->{try{
            if(editar)servicio.editarUsuario(run.getText(),String.valueOf(tipo.getSelectedItem()),nombre.getText(),genero.getText(),carrera.getText(),profesion.getText(),grados.getText());
            else servicio.agregarUsuario(String.valueOf(tipo.getSelectedItem()),nombre.getText(),run.getText(),genero.getText(),carrera.getText(),profesion.getText(),grados.getText());
            refrescar();dialog.dispose();}catch(Exception ex){error(ex);}});
        JPanel root=new JPanel(new BorderLayout());root.add(form,BorderLayout.CENTER);root.add(guardar,BorderLayout.SOUTH);
        dialog.setContentPane(root);dialog.pack();dialog.setLocationRelativeTo(this);dialog.setVisible(true);
    }
    private void formularioLibro(){
        JDialog dialog=new JDialog(this,"Registrar libro",true);
        JPanel form=new JPanel(new GridLayout(0,2,8,8));form.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        JTextField isbn=campo(""),titulo=campo(""),autor=campo(""),total=campo("1"),disponible=campo("1"),imagen=campo("");
        JButton buscar=new JButton("Seleccionar imagen");buscar.addActionListener(e->{JFileChooser f=new JFileChooser();
            if(f.showOpenDialog(dialog)==JFileChooser.APPROVE_OPTION)imagen.setText(f.getSelectedFile().getAbsolutePath());});
        String[] labels={"ISBN","Título","Autor","Cantidad total","Cantidad disponible","Ruta imagen"};
        JTextField[] fields={isbn,titulo,autor,total,disponible,imagen};
        for(int i=0;i<labels.length;i++){form.add(new JLabel(labels[i]));form.add(fields[i]);}
        form.add(new JLabel("Archivo de imagen (opcional)"));form.add(buscar);
        JButton guardar=new JButton("Guardar libro");guardar.addActionListener(e->{try{
            servicio.agregarLibro(isbn.getText(),titulo.getText(),autor.getText(),Integer.parseInt(total.getText()),
              Integer.parseInt(disponible.getText()),imagen.getText());refrescar();dialog.dispose();
        }catch(Exception ex){error(ex);}});
        dialog.add(form,BorderLayout.CENTER);dialog.add(guardar,BorderLayout.SOUTH);
        dialog.pack();dialog.setLocationRelativeTo(this);dialog.setVisible(true);
    }
    private void formularioPrestamo(){
        JPanel p=new JPanel(new GridLayout(0,2,8,8));JTextField isbn=campo(""),run=campo(""),dias=campo("10");
        p.add(new JLabel("ISBN"));p.add(isbn);p.add(new JLabel("RUN usuario"));p.add(run);p.add(new JLabel("Días de préstamo"));p.add(dias);
        if(JOptionPane.showConfirmDialog(this,p,"Nuevo préstamo",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION){
            try{Prestamo pr=servicio.prestar(isbn.getText(),run.getText(),Integer.parseInt(dias.getText()));
                refrescar();mostrarTarjeta(pr);}catch(Exception ex){error(ex);}
        }
    }
    private void formularioDevolucion(){
        JPanel p=new JPanel(new GridLayout(0,2,8,8));JTextField isbn=campo(""),run=campo("");
        p.add(new JLabel("ISBN"));p.add(isbn);p.add(new JLabel("RUN usuario"));p.add(run);
        if(JOptionPane.showConfirmDialog(this,p,"Devolución",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION){
            try{long multa=servicio.devolver(isbn.getText(),run.getText());refrescar();
                JOptionPane.showMessageDialog(this,multa>0?"Devolución registrada. Multa: $"+multa:"Devolución registrada. Sin multa.");
            }catch(Exception ex){error(ex);}
        }
    }
    private void mostrarTarjeta(Prestamo pr){
        Usuario u=servicio.usuario(pr.getRun());Libro l=servicio.libro(pr.getIsbn());
        JTextArea area=new JTextArea(pr.tarjeta(u,l),15,42);area.setEditable(false);area.setFont(new Font(Font.MONOSPACED,Font.PLAIN,13));
        JButton imprimir=new JButton("Imprimir tarjeta");JDialog d=new JDialog(this,"Tarjeta de préstamo",true);
        imprimir.addActionListener(e->{try{area.print();}catch(Exception ex){error(ex);}});
        d.add(new JScrollPane(area),BorderLayout.CENTER);d.add(imprimir,BorderLayout.SOUTH);d.pack();d.setLocationRelativeTo(this);d.setVisible(true);
    }
    private void refrescar(){
        usuariosModel.setRowCount(0);for(Usuario u:servicio.usuarios()){
            Docente d=u instanceof Docente?(Docente)u:null;
            usuariosModel.addRow(new Object[]{u.getTipo(),u.getNombreCompleto(),u.getRun(),u.getGenero(),u.getCarrera(),
               u.getIsbnPrestado(),d==null?"":d.getProfesion(),d==null?"":d.getGrados()});}
        librosModel.setRowCount(0);for(Libro l:servicio.libros())librosModel.addRow(new Object[]{l.getIsbn(),l.getTitulo(),l.getAutor(),
           l.getCantidadTotal(),l.getCantidadDisponible(),l.getImagen()});
        prestamosModel.setRowCount(0);for(Prestamo p:servicio.prestamos())prestamosModel.addRow(new Object[]{p.getRun(),p.getIsbn(),
           p.getFechaInicio(),p.getFechaVencimiento(),p.getDias()});
    }
    private void error(Exception e){JOptionPane.showMessageDialog(this,e.getMessage(),"Atención",JOptionPane.ERROR_MESSAGE);}
}
