package pupr.capstone.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

/**
 * CalendarActivity - Interfaz de usuario para selección de fechas
 *
 * Esta Activity proporciona una interfaz visual interactiva para el calendario,
 * permitiendo al usuario:
 * - Navegar entre meses y años
 * - Seleccionar una fecha única o un rango de fechas
 * - Visualizar las fechas seleccionadas
 * - Confirmar la selección
 *
 * Utiliza la clase CalendarPicker para toda la lógica de negocio.
 *
 * @author Tu nombre
 * @version 1.1
 */
public class CalendarActivity extends AppCompatActivity {


    /** Lógica del calendario */
    private CalendarPicker calendar;

    /** TextView que muestra el mes y año actual */
    private TextView tvMesAño;

    /** TextView que muestra la fecha(s) seleccionada(s) */
    private TextView tvSeleccion;

    /** Botones de navegación y control */
    private Button btnAnterior, btnSiguiente, btnHoy;
    private Button btnModoSingle, btnModoRange, btnConfirmar;

    /** Grid donde se renderiza el calendario */
    private GridLayout gridCalendario;


    /** Color de fondo para fecha seleccionada (azul) */
    private static final int COLOR_SELECCIONADO = 0xFF2196F3;

    /** Color de fondo para fechas en rango (azul claro) */
    private static final int COLOR_RANGO = 0xFF90CAF9;

    /** Color de texto para fechas seleccionadas (blanco) */
    private static final int COLOR_TEXTO_SELECCIONADO = 0xFFFFFFFF;

    /** Altura de cada celda del calendario en píxeles */
    private static final int ALTURA_CELDA = 120;

    /** Margen entre celdas en píxeles */
    private static final int MARGEN_CELDA = 4;

    /** Padding interno de las celdas */
    private static final int PADDING_CELDA = 24;

    /** Tamaño de texto en las celdas */
    private static final float TAMAÑO_TEXTO_CELDA = 16f;

    /** Padding de los headers de días */
    private static final int PADDING_HEADER = 16;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        // Inicializar calendario (puede recibir datos de vehículo del Intent)
        String vehiculoInfo = getIntent().getStringExtra("vehiculoInfo");
        String tablilla = getIntent().getStringExtra("tablilla");

        if (vehiculoInfo != null && tablilla != null) {
            calendar = new CalendarPicker(vehiculoInfo, tablilla);
        } else {
            calendar = new CalendarPicker();
        }

        initViews();
        setupListeners();
        renderizarCalendario();
    }


    /**
     * Inicializa todas las referencias a vistas del layout
     */
    private void initViews() {
        tvMesAño = findViewById(R.id.tvMesAño);
        tvSeleccion = findViewById(R.id.tvFechaSeleccionada);
        btnAnterior = findViewById(R.id.btnAnterior);
        btnSiguiente = findViewById(R.id.btnSiguiente);
        btnHoy = findViewById(R.id.btnHoy);
        btnModoSingle = findViewById(R.id.btnModoSingle);
        btnModoRange = findViewById(R.id.btnModoRange);
        btnConfirmar = findViewById(R.id.btnConfirmar);
        gridCalendario = findViewById(R.id.gridCalendario);
    }

    /**
     * Configura los listeners de todos los botones
     */
    private void setupListeners() {
        // Navegación: mes anterior
        btnAnterior.setOnClickListener(v -> {
            calendar.mesAnterior();
            renderizarCalendario();
        });

        // Navegación: mes siguiente
        btnSiguiente.setOnClickListener(v -> {
            calendar.mesSiguiente();
            renderizarCalendario();
        });

        // Navegación: volver al mes actual
        btnHoy.setOnClickListener(v -> {
            calendar.irAlMesActual();
            renderizarCalendario();
        });

        // Cambiar a modo fecha única
        btnModoSingle.setOnClickListener(v -> {
            calendar.setModoSeleccion(CalendarPicker.MODO_SINGLE);
            actualizarBotonesModo();
            renderizarCalendario();
        });

        // Cambiar a modo rango de fechas
        btnModoRange.setOnClickListener(v -> {
            calendar.setModoSeleccion(CalendarPicker.MODO_RANGE);
            actualizarBotonesModo();
            renderizarCalendario();
        });

        // Confirmar selección y cerrar
        btnConfirmar.setOnClickListener(v -> confirmarSeleccion());
    }




    /**
     * Renderiza completamente el calendario:
     * - Actualiza el título con mes/año
     * - Genera los headers de días
     * - Genera las celdas de días
     * - Actualiza el texto de selección
     */
    private void renderizarCalendario() {
        // Actualizar título
        tvMesAño.setText(calendar.getNombreMesActual());

        // Limpiar grid y configurar columnas
        gridCalendario.removeAllViews();
        gridCalendario.setColumnCount(7);

        // Renderizar headers de días de la semana
        renderizarHeadersDias();

        // Renderizar días del mes
        renderizarDiasMes();

        // Actualizar texto de selección
        actualizarTextoSeleccion();
    }

    /**
     * Renderiza los headers de días de la semana (D, L, M, M, J, V, S)
     */
    private void renderizarHeadersDias() {
        String[] dias = CalendarPicker.getDiasSemana();

        for (String dia : dias) {
            TextView tv = new TextView(this);
            tv.setText(dia);
            tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            tv.setPadding(PADDING_HEADER, PADDING_HEADER, PADDING_HEADER, PADDING_HEADER);
            tv.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));

            // Agregar al grid
            gridCalendario.addView(tv);
        }
    }

    /**
     * Renderiza todas las celdas de días del mes actual
     */
    private void renderizarDiasMes() {
        int[][] matriz = calendar.generarMatrizCalendario();

        for (int semana = 0; semana < matriz.length; semana++) {
            for (int dia = 0; dia < matriz[semana].length; dia++) {
                int numeroDia = matriz[semana][dia];
                TextView tv = crearCeldaDia(numeroDia);
                gridCalendario.addView(tv);
            }
        }
    }

    /**
     * Crea una celda de día con su estilo y funcionalidad
     *
     * @param numeroDia Número del día (1-31) o 0 para celda vacía
     * @return TextView configurado como celda de calendario
     */
    private TextView crearCeldaDia(int numeroDia) {
        TextView tv = new TextView(this);

        if (numeroDia == 0) {
            // Celda vacía (antes/después del mes)
            tv.setText("");
            tv.setEnabled(false);
        } else {
            // Celda con día válido
            configurarCeldaActiva(tv, numeroDia);
        }

        // Configurar layout params
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = ALTURA_CELDA;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(MARGEN_CELDA, MARGEN_CELDA, MARGEN_CELDA, MARGEN_CELDA);
        tv.setLayoutParams(params);

        return tv;
    }

    /**
     * Configura una celda activa (día válido del mes)
     *
     * @param tv TextView de la celda
     * @param numeroDia Número del día
     */
    private void configurarCeldaActiva(TextView tv, int numeroDia) {
        tv.setText(String.valueOf(numeroDia));
        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tv.setPadding(PADDING_CELDA, PADDING_CELDA, PADDING_CELDA, PADDING_CELDA);
        tv.setTextSize(TAMAÑO_TEXTO_CELDA);

        // Aplicar estilos según estado de selección
        aplicarEstilosCelda(tv, numeroDia);

        // Configurar click listener
        tv.setOnClickListener(v -> {
            calendar.seleccionarFecha(numeroDia);
            renderizarCalendario();
        });
    }

    /**
     * Aplica colores de fondo y texto según el estado de la celda
     *
     * @param tv TextView de la celda
     * @param numeroDia Número del día
     */
    private void aplicarEstilosCelda(TextView tv, int numeroDia) {
        if (calendar.esFechaSeleccionada(numeroDia)) {
            // Fecha seleccionada (modo SINGLE)
            tv.setBackgroundColor(COLOR_SELECCIONADO);
            tv.setTextColor(COLOR_TEXTO_SELECCIONADO);
        } else if (calendar.esFechaEnRango(numeroDia)) {
            // Fecha dentro del rango (modo RANGE)
            tv.setBackgroundColor(COLOR_RANGO);
            tv.setTextColor(COLOR_TEXTO_SELECCIONADO);
        } else {
            // Fecha normal (sin seleccionar)
            tv.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
            tv.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        }
    }

    // ACTUALIZACIÓN DE UI


    /**
     * Actualiza el texto que muestra la(s) fecha(s) seleccionada(s)
     */
    private void actualizarTextoSeleccion() {
        if (CalendarPicker.MODO_SINGLE.equals(calendar.getModoSeleccion())) {
            actualizarTextoSeleccionSingle();
        } else {
            actualizarTextoSeleccionRango();
        }
    }

    /**
     * Actualiza el texto para modo fecha única
     */
    private void actualizarTextoSeleccionSingle() {
        if (calendar.isFechaUnicaSeleccionada()) {
            tvSeleccion.setText("Fecha seleccionada: " + calendar.getFechaSeleccionadaFormateada());
            tvSeleccion.setVisibility(View.VISIBLE);
        } else {
            tvSeleccion.setVisibility(View.GONE);
        }
    }

    /**
     * Actualiza el texto para modo rango de fechas
     */
    private void actualizarTextoSeleccionRango() {
        if (calendar.isRangoSeleccionado()) {
            // Rango completo seleccionado
            String inicio = calendar.getFechaFormateada(calendar.getFechaInicial());
            String fin = calendar.getFechaFormateada(calendar.getFechaFinal());
            tvSeleccion.setText("Rango: " + inicio + " - " + fin);
            tvSeleccion.setVisibility(View.VISIBLE);
        } else if (calendar.getFechaInicial() != null) {
            // Solo fecha inicial seleccionada
            String inicio = calendar.getFechaFormateada(calendar.getFechaInicial());
            tvSeleccion.setText("Inicio: " + inicio + " (seleccione fecha final)");
            tvSeleccion.setVisibility(View.VISIBLE);
        } else {
            tvSeleccion.setVisibility(View.GONE);
        }
    }

    /**
     * Actualiza el estilo visual de los botones de modo
     */
    private void actualizarBotonesModo() {
        if (CalendarPicker.MODO_SINGLE.equals(calendar.getModoSeleccion())) {
            btnModoSingle.setBackgroundColor(COLOR_SELECCIONADO);
            btnModoRange.setBackgroundColor(ContextCompat.getColor(this, android.R.color.darker_gray));
        } else {
            btnModoRange.setBackgroundColor(COLOR_SELECCIONADO);
            btnModoSingle.setBackgroundColor(ContextCompat.getColor(this, android.R.color.darker_gray));
        }
    }



    /**
     * Confirma la selección y cierra la Activity
     * Muestra un Toast con la selección y puede devolver el resultado via Intent
     */
    private void confirmarSeleccion() {
        if (!calendar.tieneSeleccionValida()) {
            Toast.makeText(this,
                    "Por favor seleccione una fecha" +
                            (CalendarPicker.MODO_RANGE.equals(calendar.getModoSeleccion()) ? " o complete el rango" : ""),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (CalendarPicker.MODO_SINGLE.equals(calendar.getModoSeleccion())) {
            confirmarSeleccionSingle();
        } else {
            confirmarSeleccionRango();
        }
    }

    /**
     * Confirma y procesa la selección en modo fecha única
     */
    private void confirmarSeleccionSingle() {
        String fecha = calendar.getFechaSeleccionadaFormateada();

        // Mostrar confirmación
        Toast.makeText(this, "Fecha confirmada: " + fecha, Toast.LENGTH_SHORT).show();

        Intent resultIntent = new Intent();
        resultIntent.putExtra("fecha", fecha);
        setResult(RESULT_OK, resultIntent);

        finish();
    }

    /**
     * Confirma y procesa la selección en modo rango
     */
    private void confirmarSeleccionRango() {
        String inicio = calendar.getFechaFormateada(calendar.getFechaInicial());
        String fin = calendar.getFechaFormateada(calendar.getFechaFinal());

        // Mostrar confirmación
        Toast.makeText(this,
                "Rango confirmado: " + inicio + " - " + fin,
                Toast.LENGTH_SHORT).show();


        Intent resultIntent = new Intent();
        resultIntent.putExtra("fechaInicio", inicio);
        resultIntent.putExtra("fechaFin", fin);
        setResult(RESULT_OK, resultIntent);

        finish();
    }
}

