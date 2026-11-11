package cl.biblioteca.util;
public final class Validaciones {
    private Validaciones(){}
    public static String runNormalizado(String run){
        if(run==null) throw new IllegalArgumentException("Ingrese RUN.");
        String v=run.replace(".","").replace("-","").replaceAll("\\s+","").toUpperCase();
        if(!v.matches("[0-9]{1,8}[0-9K]"))throw new IllegalArgumentException("RUN con formato inválido.");
        String cuerpo=v.substring(0,v.length()-1);char dv=v.charAt(v.length()-1);
        int suma=0, factor=2;
        for(int i=cuerpo.length()-1;i>=0;i--){suma+=(cuerpo.charAt(i)-'0')*factor;factor=factor==7?2:factor+1;}
        int resto=11-(suma%11);char esperado=resto==11?'0':resto==10?'K':(char)('0'+resto);
        if(dv!=esperado)throw new IllegalArgumentException("Dígito verificador del RUN incorrecto.");
        return Long.parseLong(cuerpo)+"-"+dv;
    }
    public static String requerido(String v,String etiqueta){
        if(v==null||v.trim().isEmpty())throw new IllegalArgumentException("Ingrese "+etiqueta+".");
        return v.trim();
    }
    public static char genero(String v){
        if(v==null||!(v.trim().equalsIgnoreCase("M")||v.trim().equalsIgnoreCase("F")))
            throw new IllegalArgumentException("El género debe ser M o F.");
        return Character.toUpperCase(v.trim().charAt(0));
    }
    /** Normaliza y valida un ISBN: 10 caracteres (el último puede ser X) o 13 dígitos. */
    public static String isbn(String v){
        String s=requerido(v,"ISBN").replace("-","").replace(" ","").toUpperCase();
        if(!s.matches("[0-9]{13}|[0-9]{9}[0-9X]"))
            throw new IllegalArgumentException("ISBN inválido: use 10 caracteres (el último puede ser X) o 13 dígitos.");
        return s;
    }
}
