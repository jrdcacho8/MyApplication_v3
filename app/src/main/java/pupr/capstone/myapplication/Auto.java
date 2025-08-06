package pupr.capstone.myapplication;

import android.graphics.Bitmap;

public class Auto {
    private String nombre;
    private String tablilla;
    private Bitmap imagenBitmap;

    public Auto(String nombre, String tablilla, Bitmap imagenBitmap) {
        this.nombre = nombre;
        this.tablilla = tablilla;
        this.imagenBitmap = imagenBitmap;
    }

    public String getNombre() { return nombre; }
    public String getTablilla() { return tablilla; }
    public Bitmap getImagenBitmap() { return imagenBitmap; }
}
