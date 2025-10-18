package pupr.capstone.myapplication;

import java.util.HashMap;
import java.util.Map;

/**
 * MaintenanceForm - Maneja el formulario de mantenimiento
 * VERSIÓN CORREGIDA - Todos los métodos implementados
 */

public class MaintenanceForm {

    private String vehiculoSeleccionado;
    private String tablillaVehiculo;
    private String tipoMantenimiento;
    private long fechaServicioMs;
    private String compania;
    private float costo;
    private Map<String, String> errores;
    private boolean formularioValido;
    private boolean reciboSubido;
    private String pathRecibo;

    private static final float COSTO_MINIMO = 0.0f;

    public MaintenanceForm() {
        this.errores = new HashMap<>();
        this.formularioValido = false;
        this.reciboSubido = false;
        this.costo = 0.0f;
        this.fechaServicioMs = 0;
    }


    public String getVehiculoSeleccionado() { return vehiculoSeleccionado; }
    public String getTablillaVehiculo() { return tablillaVehiculo; }
    public long getFechaServicioMs() { return fechaServicioMs; }
    public String getCompania() { return compania; }
    public String getTipoMantenimiento() { return tipoMantenimiento; }
    public float getCosto() { return costo; }
    public boolean isReciboSubido() { return reciboSubido; }
    public String getPathRecibo() { return pathRecibo; }
    public Map<String, String> getErrores() { return errores; }


    public void setVehiculoSeleccionado(String vehiculoInfo) {
        this.vehiculoSeleccionado = vehiculoInfo;
        validarCampo("vehiculo");
    }

    public void setTablillaVehiculo(String tablilla) {
        this.tablillaVehiculo = tablilla;
        validarCampo("tablilla");
    }

    public void setCompania(String compania) {
        this.compania = compania;
        validarCampo("compania");
    }

    public void setTipoMantenimiento(String tipoMantenimiento) {
        this.tipoMantenimiento = tipoMantenimiento;
        validarCampo("tipo");
    }

    public void setFechaServicioMs(long fechaServicioMs) {
        this.fechaServicioMs = fechaServicioMs;
        validarCampo("fecha");
    }

    public void setCosto(String costoString) {
        try {
            if (costoString == null) costoString = "";
            String cleanCosto = costoString
                    .replace("$", "")
                    .replace(" ", "")
                    .replace(",", ".")   // ← COMA a PUNTO
                    .trim();
            if (cleanCosto.isEmpty()) {
                this.costo = 0f;
            } else {
                this.costo = Float.parseFloat(cleanCosto);
            }
            validarCampo("costo");
        } catch (NumberFormatException e) {
            errores.put("costo", "El costo debe ser un número válido");
            actualizarEstadoFormulario();
        }
    }




    private void validarCampo(String campo) {
        errores.remove(campo);

        switch (campo) {
            case "vehiculo":
                if (vehiculoSeleccionado == null || vehiculoSeleccionado.trim().isEmpty()) {
                    errores.put("vehiculo", "Seleccione un vehículo");
                }
                break;
            case "tablilla":
                if (tablillaVehiculo == null || tablillaVehiculo.trim().isEmpty()) {
                    errores.put("tablilla", "La tablilla es requerida");
                }
                break;
            case "tipo":
                if (tipoMantenimiento == null || tipoMantenimiento.trim().isEmpty() ||
                        tipoMantenimiento.equals("Seleccionar tipo...")) {
                    errores.put("tipo", "Seleccione el tipo de mantenimiento");
                }
                break;
            case "fecha":
                if (fechaServicioMs <= 0) {
                    errores.put("fecha", "Seleccione la fecha del servicio");
                }
                break;
            case "compania":
                if (compania == null || compania.trim().isEmpty()) {
                    errores.put("compania", "Ingrese la compañía o taller");
                }
                break;
            case "costo":
                if (costo <= COSTO_MINIMO) {
                    errores.put("costo", "Ingrese el costo del servicio");
                }
                break;
        }

        actualizarEstadoFormulario();
    }

    public boolean validarFormulario() {
        validarCampo("vehiculo");
        validarCampo("tablilla");
        validarCampo("tipo");
        validarCampo("fecha");
        validarCampo("compania");
        validarCampo("costo");
        return errores.isEmpty();
    }

    private void actualizarEstadoFormulario() {
        this.formularioValido = errores.isEmpty();
    }

    public boolean tieneErrores() {
        return !errores.isEmpty();
    }

    // ========================================
    // RECIBO - CORREGIDO
    // ========================================

    public void subirReciboExitoso(String pathRecibo) {
        this.reciboSubido = true;
        this.pathRecibo = pathRecibo;
    }



    public Servicio crearServicio(String nombreUsuario) {
        if (!validarFormulario()) {
            throw new IllegalStateException(
                    "El formulario contiene errores: " + errores.toString()
            );
        }

        Servicio servicio = new Servicio(
                this.tipoMantenimiento,
                this.fechaServicioMs,
                this.compania,
                this.costo
        );

        servicio.setNombreUsuario(nombreUsuario);
        servicio.setTablillaVehiculo(this.tablillaVehiculo);

        if (this.reciboSubido && this.pathRecibo != null) {
            servicio.setReciboPath(this.pathRecibo);
        }

        return servicio;
    }



    public void limpiarFormulario() {
        // NO limpiamos vehículo y tablilla
        this.tipoMantenimiento = null;
        this.fechaServicioMs = 0;
        this.compania = null;
        this.costo = 0.0f;
        this.reciboSubido = false;
        this.pathRecibo = null;
        this.errores.clear();
        this.formularioValido = false;
    }
}