package pupr.capstone.myapplication;

public class Auto {
    private String nombre;
    private String tablilla;
    private int imagenResId;

    public Auto(String nombre, String tablilla, int imagenResId) {
        this.nombre = nombre;
        this.tablilla = tablilla;
        this.imagenResId = imagenResId;
    }

    public String getNombre() { return nombre; }
    public String getTablilla() { return tablilla; }
    public int getImagenResId() { return imagenResId; }
}
