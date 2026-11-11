package cl.biblioteca;
import cl.biblioteca.persistencia.RepositorioTexto;
import cl.biblioteca.servicio.BibliotecaServicio;
import cl.biblioteca.ui.MainFrame;
import java.nio.file.Paths;
import javax.swing.*;
public final class Main {
    private Main(){}
    public static void main(String[] args){
        SwingUtilities.invokeLater(()->{
            try{
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                BibliotecaServicio servicio=new BibliotecaServicio(new RepositorioTexto(Paths.get("datos")));
                new MainFrame(servicio).setVisible(true);
            }catch(Exception ex){JOptionPane.showMessageDialog(null,"Error al iniciar: "+ex.getMessage(),"Biblioteca",JOptionPane.ERROR_MESSAGE);}
        });
    }
}
