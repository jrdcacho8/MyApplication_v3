package pupr.capstone.myapplication;


import java.util.HashMap;
import java.util.Map;

/**
 * Clase MaintenanceForm - Maneja el formulario "Detalles de Mantenimiento"

 * Esta clase encapsula la lógica de validación del formulario de mantenimiento vehicular.
 * Proporciona validación en tiempo real y creación de objetos Servicio validados.

 */
public class MaintenanceForm {

    // ========================================
    // CAMPOS DEL FORMULARIO
    // ========================================

    private String vehiculoSeleccionado;
    private String tablillaVehiculo;
    private String tipoMantenimiento;
    private String fechaServicio;
    private String compania;
    private float costo;

    // ========================================
    // ESTADOS DEL FORMULARIO
    // ========================================

    private Map<String, String> errores;
    private boolean formularioValido;
    private boolean reciboSubido;
    private String pathRecibo;

    // ========================================
    // CONSTANTES
    // ========================================

    /** Patrón de validación para tablillas: XXX-XXX (letras y números) */
    private static final String PATRON_TABLILLA = "^[A-Za-z0-9]{3}-[A-Za-z0-9]{3}$";

    /** Patrón de validación para fechas: MM-DD-YYYY */
    private static final String PATRON_FECHA = "^(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])-(19|20)\\d{2}$";

    /** Costo mínimo aceptable (permite servicios gratuitos) */
    private static final float COSTO_MINIMO = 0.0f;

    // ========================================
    // CONSTRUCTORES
    // ========================================

    /**
     * Constructor vacío - Inicializa el formulario en estado limpio
     */
    public MaintenanceForm() {
        this.errores = new HashMap<>();
        this.formularioValido = false;
        this.reciboSubido = false;
        this.costo = 0.0f;
    }

    /**
     * Constructor con datos de vehículo - Útil cuando se pre-selecciona un vehículo
     *
     * @param vehiculoInfo Información del vehículo (ej: "Toyota Corolla 2020")
     * @param tablilla Tablilla del vehículo (ej: "ABC-123")
     */
    public MaintenanceForm(String vehiculoInfo, String tablilla) {
        this();
        this.vehiculoSeleccionado = vehiculoInfo;
        this.tablillaVehiculo = tablilla != null ? tablilla.toUpperCase() : null;
    }

    // ========================================
    // GETTERS
    // ========================================

    public String getVehiculoSeleccionado() {
        return vehiculoSeleccionado;
    }

    public String getTablillaVehiculo() {
        return tablillaVehiculo;
    }

    public String getTipoMantenimiento() {
        return tipoMantenimiento;
    }

    public String getFechaServicio() {
        return fechaServicio;
    }

    public String getCompania() {
        return compania;
    }

    public float getCosto() {
        return costo;
    }

    public boolean isReciboSubido() {
        return reciboSubido;
    }

    public String getPathRecibo() {
        return pathRecibo;
    }

    /**
     * Obtiene el mapa de errores de validación
     * @return Map con clave=campo y valor=mensaje de error
     */
    public Map<String, String> getErrores() {
        return errores;
    }

    /**
     * Verifica si el formulario es válido (sin errores)
     * @return true si todos los campos son válidos
     */
    public boolean isFormularioValido() {
        return formularioValido;
    }


    /**
     * Establece el vehículo seleccionado y valida
     * @param vehiculoSeleccionado Información del vehículo
     */
    public void setVehiculoSeleccionado(String vehiculoSeleccionado) {
        this.vehiculoSeleccionado = vehiculoSeleccionado;
        validarCampo("vehiculo");
    }

    /**
     * Establece la tablilla del vehículo (auto-convierte a mayúsculas)
     * @param tablillaVehiculo Tablilla en formato XXX-XXX
     */
    public void setTablillaVehiculo(String tablillaVehiculo) {
        // Auto-convertir a mayúsculas para facilitar entrada de usuario
        this.tablillaVehiculo = tablillaVehiculo != null ?
                tablillaVehiculo.toUpperCase().trim() : null;
        validarCampo("tablilla");
    }

    /**
     * Establece el tipo de mantenimiento
     * @param tipoMantenimiento Tipo de servicio (ej: "Cambio de Aceite")
     */
    public void setTipoMantenimiento(String tipoMantenimiento) {
        this.tipoMantenimiento = tipoMantenimiento;
        validarCampo("tipo");
    }

    /**
     * Establece la fecha del servicio
     * @param fechaServicio Fecha en formato MM-DD-YYYY
     */
    public void setFechaServicio(String fechaServicio) {
        this.fechaServicio = fechaServicio;
        validarCampo("fecha");
    }

    /**
     * Establece la compañía/taller
     * @param compania Nombre del taller o empresa
     */
    public void setCompania(String compania) {
        this.compania = compania;
        validarCampo("compania");
    }

    /**
     * Establece el costo del servicio
     * @param costo Costo en punto flotante
     */
    public void setCosto(float costo) {
        this.costo = costo;
        validarCampo("costo");
    }

    /**
     * Establece el costo desde un String (remueve símbolos de moneda)
     * @param costoString Costo como texto (ej: "$150.00" o "150")
     */
    public void setCosto(String costoString) {
        try {
            // Limpiar string: remover $, espacios, etc.
            String cleanCosto = costoString.replace("$", "")
                    .replace(",", "")
                    .trim();
            this.costo = Float.parseFloat(cleanCosto);
            validarCampo("costo");
        } catch (NumberFormatException e) {
            errores.put("costo", "El costo debe ser un número válido");
            actualizarEstadoFormulario();
        }
    }


    /**
     * Valida un campo específico y actualiza el mapa de errores
     * @param campo Nombre del campo a validar
     */
    private void validarCampo(String campo) {
        // Remover error previo si existía
        errores.remove(campo);

        switch (campo) {
            case "vehiculo":
                if (vehiculoSeleccionado == null || vehiculoSeleccionado.trim().isEmpty()) {
                    errores.put("vehiculo", "Debe seleccionar un vehículo");
                }
                break;

            case "tablilla":
                if (tablillaVehiculo == null || tablillaVehiculo.trim().isEmpty()) {
                    errores.put("tablilla", "La tablilla del vehículo es requerida");
                } else if (!esTabrillaValida(tablillaVehiculo)) {
                    errores.put("tablilla", "Formato de tablilla inválido (use XXX-XXX)");
                }
                break;

            case "tipo":
                if (tipoMantenimiento == null || tipoMantenimiento.trim().isEmpty()) {
                    errores.put("tipo", "El tipo de mantenimiento es requerido");
                }
                break;

            case "fecha":
                if (fechaServicio == null || fechaServicio.trim().isEmpty()) {
                    errores.put("fecha", "La fecha de servicio es requerida");
                } else if (!esFechaValida(fechaServicio)) {
                    errores.put("fecha", "Formato de fecha inválido (use MM-DD-YYYY)");
                }
                break;

            case "compania":
                if (compania == null || compania.trim().isEmpty()) {
                    errores.put("compania", "La compañía es requerida");
                }
                break;

            case "costo":
                if (costo < COSTO_MINIMO) {
                    errores.put("costo", "El costo no puede ser negativo");
                }
                // Nota: Permitimos costo = 0 para servicios gratuitos/garantía
                break;

            default:

                break;
        }

        actualizarEstadoFormulario();
    }

    /**
     * Valida todos los campos del formulario
     * @return true si el formulario es válido, false si hay errores
     */
    public boolean validarFormulario() {
        validarCampo("vehiculo");
        validarCampo("tablilla");
        validarCampo("tipo");
        validarCampo("fecha");
        validarCampo("compania");
        validarCampo("costo");
        return formularioValido;
    }

    /**
     * Actualiza el estado de validez del formulario basándose en errores
     */
    private void actualizarEstadoFormulario() {
        this.formularioValido = errores.isEmpty();
    }

    /**
     * Valida el formato de la tablilla
     * @param tablilla Tablilla a validar
     * @return true si cumple el patrón XXX-XXX (letras/números)
     */
    private boolean esTabrillaValida(String tablilla) {
        if (tablilla == null) return false;
        return tablilla.matches(PATRON_TABLILLA);
    }

    /**
     * Valida el formato de la fecha
     * @param fecha Fecha a validar
     * @return true si cumple el patrón MM-DD-YYYY
     */
    private boolean esFechaValida(String fecha) {
        if (fecha == null) return false;
        return fecha.matches(PATRON_FECHA);
    }

    // ========================================
    // MANEJO DE RECIBOS
    // ========================================

    /**
     * Simula la subida de un recibo de servicio
     * NOTA: Esta es una implementación placeholder. En producción, usar:
     * - Intent.ACTION_OPEN_DOCUMENT para seleccionar archivo
     * - Firebase Storage o API backend para subir archivo real
     *
     * @return Mensaje de resultado de la operación
     */
    public String subirRecibo() {
        if (reciboSubido) {
            return "Ya existe un recibo subido: " + pathRecibo;
        }

        if (tipoMantenimiento != null && fechaServicio != null) {
            // Generar nombre de archivo basado en tipo y fecha
            String nombreArchivo = tipoMantenimiento.replace(" ", "_") +
                    "_" + fechaServicio.replace("-", "") + ".pdf";

            // TODO: Implementar subida real de archivo
            // Por ahora, solo guardamos un path ficticio
            this.pathRecibo = "/storage/recibos/" + nombreArchivo;
            this.reciboSubido = true;

            return "Recibo registrado: " + nombreArchivo;
        } else {
            return "Complete el tipo de mantenimiento y fecha antes de subir el recibo";
        }
    }

    /**
     * Elimina la referencia al recibo subido
     */
    public void eliminarRecibo() {
        this.reciboSubido = false;
        this.pathRecibo = null;
    }

    // ========================================
    // CREACIÓN DE OBJETOS
    // ========================================

    /**
     * Crea un objeto Servicio validado desde los datos del formulario
     *
     * @param nombreUsuario Usuario que registra el servicio
     * @return Objeto Servicio con los datos del formulario
     * @throws IllegalStateException si el formulario contiene errores
     */
    public Servicio crearServicio(String nombreUsuario) {
        if (!validarFormulario()) {
            throw new IllegalStateException(
                    "El formulario contiene errores: " + errores.toString()
            );
        }

        // Crear servicio con constructor de 4 parámetros
        Servicio servicio = new Servicio(this.tipoMantenimiento, this.fechaServicio, this.compania,this.costo);

        // Establecer campos adicionales
        servicio.setNombreUsuario(nombreUsuario);
        servicio.setTablillaVehiculo(this.tablillaVehiculo);

        // Agregar recibo si fue subido
        if (this.reciboSubido) {
            servicio.setReciboPath(this.pathRecibo);
        }

        return servicio;
    }

    // ========================================
    // UTILIDADES
    // ========================================

    /**
     * Limpia todos los campos del formulario (excepto vehículo y tablilla)
     * Mantiene la información del vehículo para facilitar múltiples registros
     */
    public void limpiarFormulario() {
        // Mantener vehículo y tablilla para facilitar entrada de múltiples servicios
        // Si quieres limpiarlos también, descomenta las siguientes líneas:
        // this.vehiculoSeleccionado = null;
        // this.tablillaVehiculo = null;

        this.tipoMantenimiento = null;
        this.fechaServicio = null;
        this.compania = null;
        this.costo = 0.0f;
        this.reciboSubido = false;
        this.pathRecibo = null;
        this.errores.clear();
        this.formularioValido = false;
    }

    /**
     * Obtiene el costo formateado como moneda
     * @return String con formato "$XX.XX"
     */
    public String getCostoFormateado() {
        return String.format("$%.2f", costo);
    }

    /**
     * Verifica si el formulario tiene errores de validación
     * @return true si hay errores
     */
    public boolean tieneErrores() {
        return !errores.isEmpty();
    }

    /**
     * Obtiene el mensaje de error de un campo específico
     * @param campo Nombre del campo
     * @return Mensaje de error o null si no hay error
     */
    public String getErrorPorCampo(String campo) {
        return errores.get(campo);
    }
}
