package pupr.capstone.myapplication;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

/**
 * ServicesActivity - Formulario para registrar servicios de mantenimiento vehicular
 *
 * Esta Activity maneja la entrada de datos para un nuevo servicio de mantenimiento,
 * incluyendo:
 * - Información del vehículo
 * - Tipo de servicio
 * - Fecha del servicio
 * - Compañía/taller
 * - Costo
 * - Recibo (placeholder)
 *
 * Utiliza MaintenanceForm para validación en tiempo real y creación de objetos Servicio.

 */
public class ServicesActivity extends AppCompatActivity {


    /**
     * Objeto que maneja la validación del formulario
     */
    private MaintenanceForm formulario;

    /**
     * Campos de texto editables
     */
    private EditText etVehiculo, etTablilla, etCompania, etCosto;

    /**
     * Selector de tipo de mantenimiento
     */
    private Spinner spinnerTipo;

    /**
     * TextViews informativos
     */
    private TextView tvFechaSeleccionada, tvErrores;

    /**
     * Botones de acción
     */
    private Button btnFecha, btnRecibo, btnGuardar, btnLimpiar;


    /**
     * Código de request para el CalendarActivity (si se usa startActivityForResult)
     */
    private static final int REQUEST_CODE_FECHA = 100;

    /**
     * Placeholder para usuario (en producción, obtener del login)
     */
    private static final String USUARIO_DEFAULT = "Usuario1";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintenance);

        // Inicializar formulario
        inicializarFormulario();

        // Configurar UI
        initViews();
        setupSpinner();
        setupListeners();

        // Prellenar datos si vienen del Intent
        prellenarDatosIntent();
    }


    /**
     * Inicializa el objeto MaintenanceForm
     * Puede usar el constructor con parámetros si vienen datos del Intent
     */
    private void inicializarFormulario() {
        String vehiculoInfo = getIntent().getStringExtra("vehiculoInfo");
        String tablilla = getIntent().getStringExtra("tablilla");

        if (vehiculoInfo != null && tablilla != null) {
            formulario = new MaintenanceForm(vehiculoInfo, tablilla);
        } else {
            formulario = new MaintenanceForm();
        }
    }

    /**
     * Inicializa todas las referencias a vistas del layout
     */
    private void initViews() {
        etVehiculo = findViewById(R.id.etVehiculo);
        etTablilla = findViewById(R.id.etTablilla);
        etCompania = findViewById(R.id.etCompania);
        etCosto = findViewById(R.id.etCosto);
        spinnerTipo = findViewById(R.id.spinnerTipo);
        tvFechaSeleccionada = findViewById(R.id.tvFechaSeleccionada);
        tvErrores = findViewById(R.id.tvErrores);
        btnFecha = findViewById(R.id.btnSeleccionarFecha);
        btnRecibo = findViewById(R.id.btnSubirRecibo);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnLimpiar = findViewById(R.id.btnLimpiar);
    }

    /**
     * Configura el Spinner con los tipos de mantenimiento disponibles
     */
    private void setupSpinner() {
        String[] tipos = {
                "Seleccionar tipo...",
                "Cambio de Aceite",
                "Frenos",
                "Batería",
                "Rotación de Gomas",
                "Transmisión",
                "Alineación",
                "Filtro de Aire",
                "Bujías",
                "Coolant/Anticongelante",
                "Otro"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                tipos
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipo.setAdapter(adapter);
    }

    /**
     * Configura los listeners de todos los botones
     */
    private void setupListeners() {
        btnFecha.setOnClickListener(v -> mostrarDatePicker());
        btnRecibo.setOnClickListener(v -> subirRecibo());
        btnGuardar.setOnClickListener(v -> guardarServicio());
        btnLimpiar.setOnClickListener(v -> limpiarFormulario());
    }

    /**
     * Pre-llena los campos si vienen datos del Intent
     */
    private void prellenarDatosIntent() {
        if (formulario.getVehiculoSeleccionado() != null) {
            etVehiculo.setText(formulario.getVehiculoSeleccionado());
        }
        if (formulario.getTablillaVehiculo() != null) {
            etTablilla.setText(formulario.getTablillaVehiculo());
        }
    }


    /**
     * Muestra el DatePicker para seleccionar la fecha del servicio
     * Usa el DatePickerDialog nativo de Android
     */
    private void mostrarDatePicker() {
        Calendar cal = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    // Formato MM-DD-YYYY
                    String fecha = String.format("%02d-%02d-%04d", month + 1, day, year);
                    tvFechaSeleccionada.setText("Fecha: " + fecha);
                    formulario.setFechaServicio(fecha);
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_FECHA && resultCode == RESULT_OK && data != null) {
            String fecha = data.getStringExtra("fecha");
            if (fecha != null) {
                tvFechaSeleccionada.setText("Fecha: " + fecha);
                formulario.setFechaServicio(fecha);
            }
        }
    }


    /**
     * Registra un recibo para el servicio
     * NOTA: Esta es una implementación placeholder
     * TODO: Implementar selección real de archivo con Intent.ACTION_OPEN_DOCUMENT
     */
    private void subirRecibo() {
        actualizarFormulario();
        String resultado = formulario.subirRecibo();

        Toast.makeText(this, resultado, Toast.LENGTH_SHORT).show();

        if (formulario.isReciboSubido()) {
            btnRecibo.setText("✓ Recibo Subido");
            btnRecibo.setEnabled(false);
            btnRecibo.setAlpha(0.6f);
        }
    }


    /**
     * Actualiza el objeto MaintenanceForm con los valores actuales de la UI
     */
    private void actualizarFormulario() {
        String vehiculo = etVehiculo.getText().toString().trim();
        String tablilla = etTablilla.getText().toString().trim();
        String tipo = spinnerTipo.getSelectedItem().toString();
        String compania = etCompania.getText().toString().trim();
        String costo = etCosto.getText().toString().trim();

        // Actualizar solo si los campos no están vacíos
        if (!vehiculo.isEmpty()) {
            formulario.setVehiculoSeleccionado(vehiculo);
        }

        if (!tablilla.isEmpty()) {
            formulario.setTablillaVehiculo(tablilla);
        }

        if (!tipo.equals("Seleccionar tipo...")) {
            formulario.setTipoMantenimiento(tipo);
        }

        if (!compania.isEmpty()) {
            formulario.setCompania(compania);
        }

        if (!costo.isEmpty()) {
            formulario.setCosto(costo);
        }
    }

    /**
     * Valida y guarda el servicio
     * Si la validación falla, muestra los errores
     * Si es exitosa, crea el objeto Servicio y lo guarda
     */
    private void guardarServicio() {
        actualizarFormulario();

        // Validar formulario
        if (!formulario.validarFormulario()) {
            mostrarErrores();
            return;
        }

        try {
            // Crear servicio validado
            // TODO: Obtener usuario real del sistema de autenticación
            Servicio servicio = formulario.crearServicio(USUARIO_DEFAULT);

            // TODO: Guardar en base de datos (Room, Firebase, etc.)
            guardarEnBaseDatos(servicio);

            // Mostrar confirmación
            mostrarConfirmacionGuardado(servicio);

            // Limpiar y cerrar
            limpiarFormulario();
            finish();

        } catch (IllegalStateException e) {
            Toast.makeText(this,
                    "Error al crear servicio: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Guarda el servicio en la base de datos
     * TODO: Implementar con Room, Firebase, o backend API
     *
     * @param servicio Objeto servicio a guardar
     */
    private void guardarEnBaseDatos(Servicio servicio) {
        // Placeholder - en producción usar:
        // - Room Database: AppDatabase.getInstance(this).servicioDao().insertarServicio(...)
        // - Firebase: FirebaseDatabase.getInstance().getReference("servicios").push().setValue(...)
        // - API REST: Retrofit call para POST /servicios
    }

    /**
     * Muestra un mensaje de confirmación con los datos guardados
     *
     * @param servicio Servicio guardado
     */
    private void mostrarConfirmacionGuardado(Servicio servicio) {
        String mensaje = "✓ Servicio guardado exitosamente\n\n" +
                "Tipo: " + servicio.getTipoServicio() + "\n" +
                "Fecha: " + servicio.getFechaFormateada() + "\n" +
                "Costo: " + servicio.getCostoFormateado();

        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
    }


    /**
     * Muestra los errores de validación en el TextView de errores
     * Construye una lista con viñetas de todos los errores encontrados
     */
    private void mostrarErrores() {
        if (formulario.tieneErrores()) {
            StringBuilder errores = new StringBuilder("⚠ Errores de validación:\n\n");

            for (String error : formulario.getErrores().values()) {
                errores.append("• ").append(error).append("\n");
            }

            tvErrores.setText(errores.toString());
            tvErrores.setVisibility(View.VISIBLE);

            // Scroll al inicio para que el usuario vea los errores
            tvErrores.requestFocus();
        } else {
            tvErrores.setVisibility(View.GONE);
        }
    }


    /**
     * Limpia el formulario y resetea todos los campos a su estado inicial
     * Nota: MaintenanceForm.limpiarFormulario() mantiene vehículo y tablilla
     * intencionalmente para facilitar múltiples entradas
     */
    private void limpiarFormulario() {
        // Limpiar el objeto formulario
        formulario.limpiarFormulario();

        // Limpiar campos de UI
        limpiarCamposUI();

        // Ocultar errores
        tvErrores.setVisibility(View.GONE);

        // Resetear botón de recibo
        resetearBotonRecibo();

        // Notificar al usuario
        Toast.makeText(this, "Formulario limpiado", Toast.LENGTH_SHORT).show();
    }

    /**
     * Limpia todos los campos de la UI
     */
    private void limpiarCamposUI() {
        etVehiculo.setText("");
        etTablilla.setText("");
        etCompania.setText("");
        etCosto.setText("");
        spinnerTipo.setSelection(0);
        tvFechaSeleccionada.setText("Fecha no seleccionada");
    }

    /**
     * Resetea el botón de recibo a su estado inicial
     */
    private void resetearBotonRecibo() {
        btnRecibo.setText("Subir Recibo");
        btnRecibo.setEnabled(true);
        btnRecibo.setAlpha(1.0f);
    }
}
