package pupr.capstone.myapplication;

/**
 * Clase Servicio - Representa un registro de mantenimiento
 *
 * Esta clase modela un servicio de mantenimiento realizado a un vehículo,
 * incluyendo información sobre el tipo de servicio, costos, fechas y recibos.
 *
 * Basada en el diagrama UML del sistema de mantenimiento vehicular.
 */
public class Servicio {

    // ========================================
    // ATRIBUTOS
    // ========================================

    /**
     * Tipo de servicio realizado (Primary Key en BD)
     */
    private String tipoServicio;

    /**
     * Usuario que registró el servicio (Foreign Key en BD)
     */
    private String nombreUsuario;

    /**
     * Tablilla del vehículo al que se le hizo el servicio (Foreign Key en BD)
     */
    private String tablillaVehiculo;

    /**
     * Descripción detallada del servicio (opcional)
     */
    private String descripcion;

    /**
     * Costo del servicio en la moneda local
     */
    private float costo;

    /**
     * Nombre de la compañía o taller que realizó el servicio
     */
    private String compania;

    /**
     * Fecha en que se realizó el servicio (formato MM-DD-YYYY)
     */
    private String fechaServicio;

    /**
     * Ruta del archivo del recibo (puede ser local o URL)
     */
    private String reciboPath;

    // ========================================
    // CONSTANTES
    // ========================================

    /**
     * Patrón de validación para fechas: MM-DD-YYYY
     */
    private static final String PATRON_FECHA = "^(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])-(19|20)\\d{2}$";

    /**
     * Tasa de impuesto aplicable (11.5% IVU/IVA - ajustar según tu región)
     */
    private static final float TASA_IMPUESTO = 1.115f;

    /**
     * Nombres de meses en español para formateo
     */
    private static final String[] NOMBRES_MESES = {
            "", "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    };

    // ========================================
    // CONSTRUCTORES
    // ========================================

    /**
     * Constructor vacío - Requerido para frameworks de serialización
     * (Firebase, Room, Gson, etc.)
     */
    public Servicio() {
    }

    /**
     * Constructor completo - Crea un servicio con todos los datos
     *
     * @param tipoServicio     Tipo de mantenimiento (ej: "Cambio de Aceite")
     * @param nombreUsuario    Usuario que registra el servicio
     * @param tablillaVehiculo Tablilla del vehículo (ej: "ABC-123")
     * @param descripcion      Descripción detallada del trabajo realizado
     * @param costo            Costo del servicio
     * @param compania         Nombre del taller o empresa
     * @param fechaServicio    Fecha del servicio (MM-DD-YYYY)
     */
    public Servicio(String tipoServicio, String nombreUsuario, String tablillaVehiculo,
                    String descripcion, float costo, String compania, String fechaServicio) {
        this.tipoServicio = tipoServicio;
        this.nombreUsuario = nombreUsuario;
        this.tablillaVehiculo = tablillaVehiculo;
        this.descripcion = descripcion;
        this.costo = costo;
        this.compania = compania;
        this.fechaServicio = fechaServicio;
    }

    /**
     * Constructor simplificado - Para formularios básicos de mantenimiento
     * Útil cuando solo se tienen los datos esenciales del servicio
     *
     * @param tipoServicio  Tipo de mantenimiento
     * @param fechaServicio Fecha del servicio (MM-DD-YYYY)
     * @param compania      Nombre del taller
     * @param costo         Costo del servicio
     */
    public Servicio(String tipoServicio, String fechaServicio, String compania, float costo) {
        this.tipoServicio = tipoServicio;
        this.fechaServicio = fechaServicio;
        this.compania = compania;
        this.costo = costo;
    }

    // ========================================
    // GETTERS
    // ========================================

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

    public float getCosto() {
        return costo;
    }

    public String getCompania() {
        return compania;
    }

    public String getFechaServicio() {
        return fechaServicio;
    }

    public String getReciboPath() {
        return reciboPath;
    }

    // ========================================
    // SETTERS
    // ========================================

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

    /**
     * Establece el costo del servicio
     *
     * @param costo Costo (debe ser >= 0)
     */
    public void setCosto(float costo) {
        this.costo = Math.max(0, costo); // Prevenir costos negativos
    }

    public void setCompania(String compania) {
        this.compania = compania;
    }

    public void setFechaServicio(String fechaServicio) {
        this.fechaServicio = fechaServicio;
    }

    public void setReciboPath(String reciboPath) {
        this.reciboPath = reciboPath;
    }

    // ========================================
    // MÉTODOS DE NEGOCIO
    // ========================================

    /**
     * Registra la ruta de un recibo para este servicio
     * NOTA: Esta es una implementación placeholder. En producción:
     * - Usar Firebase Storage, AWS S3, o API backend para archivos reales
     * - Implementar validación de tipos de archivo
     * - Manejar errores de red/almacenamiento
     *
     * @return Mensaje indicando el resultado de la operación
     */
    public String subirReciboServicio() {
        if (this.reciboPath != null && !this.reciboPath.trim().isEmpty()) {
            return "Ya existe un recibo para este servicio: " + this.reciboPath;
        }

        // Validar que tenemos datos mínimos para generar el nombre
        if (this.tipoServicio == null || this.fechaServicio == null) {
            return "Error: Debe especificar tipo de servicio y fecha antes de subir recibo";
        }

        // Generar nombre de archivo basado en servicio y fecha
        String nombreArchivo = this.tipoServicio.replace(" ", "_").replace("/", "-") +
                "_" + this.fechaServicio.replace("-", "") + ".pdf";

        // TODO: Implementar subida real de archivo
        this.reciboPath = "/storage/recibos/" + nombreArchivo;

        return "Recibo registrado: " + nombreArchivo;
    }

    // ========================================
    // VALIDACIÓN
    // ========================================

    /**
     * Valida si la fecha del servicio tiene formato correcto
     *
     * @return true si la fecha cumple el patrón MM-DD-YYYY
     */
    public boolean esFechaValida() {
        if (fechaServicio == null) return false;
        return fechaServicio.matches(PATRON_FECHA);
    }

    /**
     * Valida si el servicio tiene todos los datos mínimos requeridos
     *
     * @return true si el servicio es válido para ser guardado
     */
    public boolean esValido() {
        return tipoServicio != null && !tipoServicio.trim().isEmpty() &&
                fechaServicio != null && esFechaValida() &&
                compania != null && !compania.trim().isEmpty() &&
                costo >= 0;
    }

    // ========================================
    // FORMATEO Y PRESENTACIÓN
    // ========================================

    /**
     * Formatea la fecha para mostrar en la UI
     * Convierte MM-DD-YYYY a "DD de Mes YYYY"
     *
     * @return Fecha formateada (ej: "15 de Enero 2025") o mensaje de error
     */
    public String getFechaFormateada() {
        if (fechaServicio == null) return "Fecha no especificada";

        try {
            String[] partes = fechaServicio.split("-");

            if (partes.length == 3) {
                int mes = Integer.parseInt(partes[0]);

                // Validar que el mes esté en rango
                if (mes < 1 || mes > 12) {
                    return fechaServicio; // Retornar original si es inválido
                }

                String dia = partes[1];
                String año = partes[2];

                return dia + " de " + NOMBRES_MESES[mes] + " " + año;
            }
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            // Si hay error al parsear, retornar fecha original
            return fechaServicio;
        }

        return fechaServicio;
    }

    /**
     * Calcula el costo del servicio incluyendo impuestos
     *
     * @return Costo con impuestos aplicados
     */
    public float getCostoConImpuestos() {
        return costo * TASA_IMPUESTO;
    }

    /**
     * Formatea el costo como moneda
     *
     * @return String con formato "$XX.XX"
     */
    public String getCostoFormateado() {
        return String.format("$%.2f", costo);
    }

    /**
     * Formatea el costo con impuestos como moneda
     *
     * @return String con formato "$XX.XX" incluyendo impuestos
     */
    public String getCostoConImpuestosFormateado() {
        return String.format("$%.2f", getCostoConImpuestos());
    }

// ========================================
// MÉTODOS ESTÁNDAR
//

    /**
     * Representación en texto del servicio para debugging
     *
     * @return String con todos los campos del servicio
     */
    @Override
    public String toString() {
        return "Servicio{" +
                "tipoServicio='" + tipoServicio + '\'' +
                ", nombreUsuario='" + nombreUsuario + '\'' +
                ", tablillaVehiculo='" + tablillaVehiculo + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", fechaServicio='" + fechaServicio + '\'' +
                ", costo=" + getCostoFormateado() +
                ", compania='" + compania + '\'' +
                ", reciboPath='" + reciboPath + '\'' +
                '}';
    }

    /**
     * Compara dos servicios por igualdad
     * Dos servicios son iguales si tienen el mismo tipo, fecha y tablilla
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Servicio servicio = (Servicio) obj;

        if (tipoServicio != null ? !tipoServicio.equals(servicio.tipoServicio) : servicio.tipoServicio != null)
            return false;
        if (fechaServicio != null ? !fechaServicio.equals(servicio.fechaServicio) : servicio.fechaServicio != null)
            return false;
        return tablillaVehiculo != null ? tablillaVehiculo.equals(servicio.tablillaVehiculo) : servicio.tablillaVehiculo == null;
    }


}
