package pupr.capstone.myapplication;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Servicio - Representa un registro de mantenimiento
 * VERSIÓN CORREGIDA - Todos los métodos implementados
 */
public class Servicio {

    private long id;
    private String tipoServicio;
    private String nombreUsuario;
    private String tablillaVehiculo;
    private String descripcion;
    private float costo;
    private String compania;
    private long fechaServicioMs;
    private String reciboPath;

    private static final float TASA_IMPUESTO = 1.115f;

    // ========================================
    // CONSTRUCTORES
    // ========================================

    public Servicio() {
    }

    public Servicio(String tipoServicio, long fechaServicioMs, String compania, float costo) {
        this.tipoServicio = tipoServicio;
        this.fechaServicioMs = fechaServicioMs;
        this.compania = compania;
        this.costo = costo;
    }

    // ========================================
    // GETTERS - CORREGIDOS
    // ========================================

    public long getId() {
        return id;
    }

    public String getTipoServicio() {
        return tipoServicio;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getTablillaVehiculo() {
        return tablillaVehiculo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public float getCosto() {  // ✅ CORREGIDO: Devuelve float, no String
        return costo;
    }

    public String getCompania() {
        return compania;
    }

    public long getFechaServicioMs() {
        return fechaServicioMs;
    }

    public String getReciboPath() {
        return reciboPath;
    }

    // ========================================
    // SETTERS - CORREGIDOS
    // ========================================

    public void setId(long id) {
        this.id = id;
    }

    public void setTipoServicio(String tipoServicio) {
        this.tipoServicio = tipoServicio;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public void setTablillaVehiculo(String tablillaVehiculo) {
        this.tablillaVehiculo = tablillaVehiculo;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setCosto(float costo) {
        this.costo = Math.max(0, costo);
    }

    public void setCompania(String compania) {
        this.compania = compania;
    }

    public void setFechaServicioMs(long fechaServicioMs) {
        this.fechaServicioMs = fechaServicioMs;
    }

    public void setReciboPath(String reciboPath) {
        this.reciboPath = reciboPath;
    }

    // ========================================
    // VALIDACIÓN
    // ========================================

    public boolean esValido() {
        return tipoServicio != null && !tipoServicio.trim().isEmpty() &&
                fechaServicioMs > 0 &&
                compania != null && !compania.trim().isEmpty() &&
                costo >= 0;
    }

    // ========================================
    // FORMATEO
    // ========================================

    public String getFechaFormateada() {
        if (fechaServicioMs <= 0) return "Fecha no especificada";
        SimpleDateFormat sdf = new SimpleDateFormat("dd 'de' MMMM yyyy", new Locale("es", "ES"));
        return sdf.format(new Date(fechaServicioMs));
    }

    public float getCostoConImpuestos() {
        return costo * TASA_IMPUESTO;
    }

    public String getCostoFormateado() {
        return String.format(Locale.US, "$%.2f", costo);
    }
}
