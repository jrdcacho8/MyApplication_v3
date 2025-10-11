package pupr.capstone.myapplication;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class CalendarPicker {
    private String tablillaVehiculo;
    private String vehiculoInfo;
    // ATRIBUTOS DE CALENDARIO


    /** Instancia de Calendar para manipulación de fechas */
    private Calendar calendarInstance;

    /** Mes actual mostrado (0-11, donde 0=Enero) */
    private int mesActual;

    /** Año actual mostrado */
    private int añoActual;


    // ATRIBUTOS DE SELECCIÓN


    /** Fecha inicial del rango seleccionado (modo RANGE) */
    private Date fechaInicial;

    /** Fecha final del rango seleccionado (modo RANGE) */
    private Date fechaFinal;

    /** Fecha única seleccionada (modo SINGLE) */
    private Date fechaSeleccionada;

    /** Indica si se completó la selección de un rango */
    private boolean rangoSeleccionado;

    /** Indica si se seleccionó una fecha única */
    private boolean fechaUnicaSeleccionada;

    /** Modo de selección: "SINGLE" o "RANGE" */
    private String modoSeleccion;


    /** Nombres de meses en español */
    private static final String[] NOMBRES_MESES = {
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    };

    /** Nombres abreviados de días de la semana */
    private static final String[] DIAS_SEMANA = {
            "DOM", "LUN", "MAR", "MIE", "JUE", "VIE", "SAB"
    };

    /** Modo de selección: fecha única */
    public static final String MODO_SINGLE = "SINGLE";

    /** Modo de selección: rango de fechas */
    public static final String MODO_RANGE = "RANGE";

    /** Año mínimo válido para navegación */
    private static final int AÑO_MINIMO = 1980;

    /** Número de semanas a mostrar en la matriz del calendario */
    private static final int SEMANAS_CALENDARIO = 6;

    /** Número de días por semana */
    private static final int DIAS_SEMANA_COUNT = 7;

    // CONSTRUCTORES

    /**
     * Constructor predeterminado - Inicializa el calendario en el mes actual
     * con modo de selección única
     */
    public CalendarPicker() {
        this.calendarInstance = Calendar.getInstance();
        this.mesActual = calendarInstance.get(Calendar.MONTH);
        this.añoActual = calendarInstance.get(Calendar.YEAR);
        this.modoSeleccion = MODO_SINGLE;
        this.rangoSeleccionado = false;
        this.fechaUnicaSeleccionada = false;
    }

    /**
     * Constructor con información de vehículo
     * Útil cuando el calendario está asociado a servicios de un vehículo específico
     *
     * @param vehiculoInfo Información del vehículo
     * @param tablillaVehiculo Tablilla del vehículo
     */
    public CalendarPicker(String vehiculoInfo, String tablillaVehiculo) {
        this();
        this.vehiculoInfo = vehiculoInfo;
        this.tablillaVehiculo = tablillaVehiculo;
    }

    // GETTERS


    /**
     * Obtiene el mes actual mostrado
     * @return Mes (0-11, donde 0=Enero)
     */
    public int getMesActual() {
        return mesActual;
    }

    /**
     * Obtiene el año actual mostrado
     * @return Año (ej: 2025)
     */
    public int getAñoActual() {
        return añoActual;
    }

    /**
     * Obtiene la fecha inicial del rango (modo RANGE)
     * @return Fecha inicial o null si no hay selección
     */
    public Date getFechaInicial() {
        return fechaInicial;
    }

    /**
     * Obtiene la fecha final del rango (modo RANGE)
     * @return Fecha final o null si no se completó el rango
     */
    public Date getFechaFinal() {
        return fechaFinal;
    }

    /**
     * Obtiene la fecha seleccionada (modo SINGLE)
     * @return Fecha seleccionada o null si no hay selección
     */
    public Date getFechaSeleccionada() {
        return fechaSeleccionada;
    }

    public String getVehiculoInfo() {
        return vehiculoInfo;
    }

    public String getTablillaVehiculo() {
        return tablillaVehiculo;
    }

    /**
     * Verifica si se completó la selección de un rango
     * @return true si hay fecha inicial y final seleccionadas
     */
    public boolean isRangoSeleccionado() {
        return rangoSeleccionado;
    }

    /**
     * Verifica si se seleccionó una fecha única
     * @return true si hay una fecha seleccionada en modo SINGLE
     */
    public boolean isFechaUnicaSeleccionada() {
        return fechaUnicaSeleccionada;
    }

    /**
     * Obtiene el modo de selección actual
     * @return "SINGLE" o "RANGE"
     */
    public String getModoSeleccion() {
        return modoSeleccion;
    }

    /**
     * Obtiene el arreglo de nombres de días de la semana
     * @return Arreglo con abreviaturas de días
     */
    public static String[] getDiasSemana() {
        return DIAS_SEMANA.clone();
    }


    // SETTERS


    /**
     * Establece la información del vehículo asociado
     * @param vehiculoInfo Información del vehículo
     * @param tablillaVehiculo Tablilla del vehículo
     */
    public void setVehiculoInfo(String vehiculoInfo, String tablillaVehiculo) {
        this.vehiculoInfo = vehiculoInfo;
        this.tablillaVehiculo = tablillaVehiculo;
    }

    /**
     * Cambia el modo de selección del calendario
     * Al cambiar el modo, se limpia cualquier selección previa
     *
     * @param modo "SINGLE" para fecha única o "RANGE" para rango
     */
    public void setModoSeleccion(String modo) {
        if (MODO_SINGLE.equals(modo) || MODO_RANGE.equals(modo)) {
            this.modoSeleccion = modo;
            limpiarSeleccion();
        }
    }
    /**
     * Navega al mes anterior
     * Si está en enero, retrocede al diciembre del año anterior
     */
    public void mesAnterior() {
        if (mesActual == 0) {
            mesActual = 11;
            añoActual--;
        } else {
            mesActual--;
        }
        actualizarCalendario();
    }

    /**
     * Navega al mes siguiente
     * Si está en diciembre, avanza al enero del año siguiente
     */
    public void mesSiguiente() {
        if (mesActual == 11) {
            mesActual = 0;
            añoActual++;
        } else {
            mesActual++;
        }
        actualizarCalendario();
    }

    /**
     * Navega a un mes y año específico
     *
     * @param mes Mes destino (0-11)
     * @param año Año destino (debe ser mayor a 1900)
     * @return true si la navegación fue exitosa, false si los parámetros son inválidos
     */
    public boolean irAMes(int mes, int año) {
        if (mes >= 0 && mes <= 11 && año > AÑO_MINIMO) {
            this.mesActual = mes;
            this.añoActual = año;
            actualizarCalendario();
            return true;
        }
        return false;
    }

    /**
     * Regresa al mes actual (hoy)
     */
    public void irAlMesActual() {
        Calendar hoy = Calendar.getInstance();
        this.mesActual = hoy.get(Calendar.MONTH);
        this.añoActual = hoy.get(Calendar.YEAR);
        actualizarCalendario();
    }

    /**
     * Actualiza la instancia de Calendar con el mes y año actuales
     */
    private void actualizarCalendario() {
        calendarInstance.set(Calendar.MONTH, mesActual);
        calendarInstance.set(Calendar.YEAR, añoActual);
    }



    /**
     * Obtiene el nombre del mes actual con el año
     * @return String en formato "Mes Año"
     */
    public String getNombreMesActual() {
        return NOMBRES_MESES[mesActual] + " " + añoActual;
    }

    /**
     * Genera una matriz 6x7 representando el calendario del mes actual
     * Cada celda contiene el número del día o 0 si está vacía
     *
     * La matriz sigue este formato:
     * - 6 filas (semanas)
     * - 7 columnas (días: domingo a sábado)
     * - Valores: 1-31 para días del mes, 0 para celdas vacías
     *
     * @return Matriz bidimensional [semana][día] con los números de día
     */
    public int[][] generarMatrizCalendario() {
        Calendar cal = new GregorianCalendar(añoActual, mesActual, 1);
        int diasEnMes = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int primerDiaSemana = cal.get(Calendar.DAY_OF_WEEK) - 1;

        int[][] matriz = new int[SEMANAS_CALENDARIO][DIAS_SEMANA_COUNT];

        int diaActual = 1;
        for (int semana = 0; semana < SEMANAS_CALENDARIO; semana++) {
            for (int dia = 0; dia < DIAS_SEMANA_COUNT; dia++) {
                if (semana == 0 && dia < primerDiaSemana) {
                    // Celdas vacías antes del primer día
                    matriz[semana][dia] = 0;
                } else if (diaActual <= diasEnMes) {
                    // Días del mes
                    matriz[semana][dia] = diaActual;
                    diaActual++;
                } else {
                    // Celdas vacías después del último día
                    matriz[semana][dia] = 0;
                }
            }
        }

        return matriz;
    }

    /**
     * Obtiene el número de días en el mes actual
     * @return Cantidad de días (28-31)
     */
    public int getDiasEnMes() {
        Calendar cal = new GregorianCalendar(añoActual, mesActual, 1);
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH);
    }


    // SELECCIÓN DE FECHAS

    /**
     * Selecciona un día del mes actual
     * El comportamiento depende del modo:
     * - SINGLE: Establece la fecha seleccionada
     * - RANGE: Establece inicio o fin del rango
     *
     * @param dia Día del mes a seleccionar (1-31)
     * @return true si la selección fue exitosa, false si el día es inválido
     */
    public boolean seleccionarFecha(int dia) {
        if (dia <= 0 || dia > getDiasEnMes()) {
            return false;
        }

        Calendar cal = new GregorianCalendar(añoActual, mesActual, dia);

        if (MODO_SINGLE.equals(modoSeleccion)) {
            this.fechaSeleccionada = cal.getTime();
            this.fechaUnicaSeleccionada = true;
            return true;
        } else if (MODO_RANGE.equals(modoSeleccion)) {
            return seleccionarRangoFecha(cal.getTime());
        }

        return false;
    }

    /**
     * Maneja la selección de fechas en modo RANGE
     * Lógica:
     * 1. Primera selección: establece fecha inicial
     * 2. Segunda selección: establece fecha final (auto-ordena si es necesario)
     * 3. Tercera selección: reinicia el rango con nueva fecha inicial
     *
     * @param fecha Fecha a agregar al rango
     * @return true si la operación fue exitosa
     */
    private boolean seleccionarRangoFecha(Date fecha) {
        if (fechaInicial == null) {
            // Primera selección: establecer inicio
            fechaInicial = fecha;
            fechaFinal = null;
            rangoSeleccionado = false;
            return true;
        } else if (fechaFinal == null) {
            // Segunda selección: establecer fin
            if (fecha.before(fechaInicial)) {
                // Si la segunda fecha es anterior, invertir orden
                fechaFinal = fechaInicial;
                fechaInicial = fecha;
            } else {
                fechaFinal = fecha;
            }
            rangoSeleccionado = true;
            return true;
        } else {
            // Tercera selección: reiniciar con nuevo inicio
            fechaInicial = fecha;
            fechaFinal = null;
            rangoSeleccionado = false;
            return true;
        }
    }

    /**
     * Limpia todas las selecciones de fechas
     * Útil al cambiar de modo o reiniciar el calendario
     */
    public void limpiarSeleccion() {
        this.fechaInicial = null;
        this.fechaFinal = null;
        this.fechaSeleccionada = null;
        this.rangoSeleccionado = false;
        this.fechaUnicaSeleccionada = false;
    }

    /**
     * Formatea una fecha en formato MM-DD-YYYY
     * @param fecha Fecha a formatear
     * @return String formateado o null si la fecha es null
     */
    public String getFechaFormateada(Date fecha) {
        if (fecha == null) return null;

        Calendar cal = Calendar.getInstance();
        cal.setTime(fecha);

        int mes = cal.get(Calendar.MONTH) + 1;
        int dia = cal.get(Calendar.DAY_OF_MONTH);
        int año = cal.get(Calendar.YEAR);

        return String.format("%02d-%02d-%04d", mes, dia, año);
    }

    /**
     * Obtiene la fecha seleccionada formateada (modo SINGLE)
     * @return String con formato MM-DD-YYYY o null si no hay selección
     */
    public String getFechaSeleccionadaFormateada() {
        return getFechaFormateada(fechaSeleccionada);
    }

    /**
     * Verifica si un día específico está seleccionado (modo SINGLE)
     * @param dia Día a verificar
     * @return true si el día está seleccionado
     */
    public boolean esFechaSeleccionada(int dia) {
        if (MODO_SINGLE.equals(modoSeleccion) && fechaSeleccionada != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(fechaSeleccionada);
            return cal.get(Calendar.DAY_OF_MONTH) == dia &&
                    cal.get(Calendar.MONTH) == mesActual &&
                    cal.get(Calendar.YEAR) == añoActual;
        }
        return false;
    }

    /**
     * Verifica si un día está dentro del rango seleccionado (modo RANGE)
     * @param dia Día a verificar
     * @return true si el día está en el rango
     */
    public boolean esFechaEnRango(int dia) {
        if (MODO_RANGE.equals(modoSeleccion) && fechaInicial != null && fechaFinal != null) {
            Calendar cal = new GregorianCalendar(añoActual, mesActual, dia);
            Date fechaDia = cal.getTime();
            return !fechaDia.before(fechaInicial) && !fechaDia.after(fechaFinal);
        }
        return false;
    }

    /**
     * Verifica si el calendario tiene una selección válida
     * @return true si hay fecha única o rango completo seleccionado
     */
    public boolean tieneSeleccionValida() {
        if (MODO_SINGLE.equals(modoSeleccion)) {
            return fechaUnicaSeleccionada;
        } else {
            return rangoSeleccionado;
        }
    }
}

