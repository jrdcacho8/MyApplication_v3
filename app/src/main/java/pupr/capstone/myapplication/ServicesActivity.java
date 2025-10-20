package pupr.capstone.myapplication;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.widget.*;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ServicesActivity extends AppCompatActivity {

    private MaintenanceForm formulario;

    private EditText etCompania, etCosto;
    private Spinner spinnerTipo, spinnerVehiculo;
    private TextView tvFechaSeleccionada, tvErrores, tvTablillaSeleccionada;
    private Button btnFecha, btnRecibo, btnGuardar, btnLimpiar;

    // Uri del recibo seleccionado
    private Uri reciboUri;

    // NUEVO: launcher para seleccionar imagen
    private ActivityResultLauncher<String> pickImageLauncher;

    // (Opcional) ImageView para previsualizar el recibo si existe en tu layout
    private ImageView receiptImage;

    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_services_pdf);

        userEmail = getIntent().getStringExtra("email");

        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(this, "Error: No se encontró el email del usuario", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        formulario = new MaintenanceForm();

        initViews();
        initPickImageLauncher();    // ← registra el Activity Result API

        setupSpinnerVehiculos();
        setupSpinner();
        setupListeners();
    }

    private void initViews() {
        spinnerVehiculo = findViewById(R.id.spinnerVehiculo);
        tvTablillaSeleccionada = findViewById(R.id.tvTablillaSeleccionada);
        etCompania = findViewById(R.id.etCompania);
        etCosto = findViewById(R.id.etCosto);
        spinnerTipo = findViewById(R.id.spinnerTipo);
        tvFechaSeleccionada = findViewById(R.id.tvFechaSeleccionada);
        tvErrores = findViewById(R.id.tvErrores);
        btnFecha = findViewById(R.id.btnSeleccionarFecha);
        btnRecibo = findViewById(R.id.btnSubirRecibo);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnLimpiar = findViewById(R.id.btnLimpiar);

        // (Opcional) solo si tienes esta ImageView en el layout
        // receiptImage = findViewById(R.id.receiptImage);
    }

    // NUEVO: registra el launcher para seleccionar contenido (imágenes)
    private void initPickImageLauncher() {
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        reciboUri = uri;
                        // Si tienes una ImageView para preview
                        if (receiptImage != null) {
                            receiptImage.setImageURI(uri);
                        }
                        formulario.subirReciboExitoso(uri.toString());
                        btnRecibo.setText("✓ Recibo Seleccionado");
                        btnRecibo.setEnabled(false);
                        btnRecibo.setAlpha(0.6f);
                        mostrarErrores();
                    }
                }
        );
    }

    private void setupSpinnerVehiculos() {
        List<String> vehiculosCargando = new ArrayList<>();
        vehiculosCargando.add("Cargando vehículos...");

        ArrayAdapter<String> adapterTemp = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, vehiculosCargando);
        adapterTemp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerVehiculo.setAdapter(adapterTemp);
        spinnerVehiculo.setEnabled(false);

        new Thread(() -> {
            List<String> vehiculosDelUsuario = new ArrayList<>();
            vehiculosDelUsuario.add("Seleccione un vehículo...");

            try {
                MyJDBC myJDBC = new MyJDBC();
                Connection connection = myJDBC.obtenerConexion();

                if (connection != null) {
                    String query = "SELECT BRAND, MODEL, LICENSE_PLATE FROM VEHICLE WHERE EMAIL = ? ORDER BY BRAND, MODEL";
                    PreparedStatement statement = connection.prepareStatement(query);
                    statement.setString(1, userEmail);
                    ResultSet rs = statement.executeQuery();

                    while (rs.next()) {
                        String marca = rs.getString("BRAND");
                        String modelo = rs.getString("MODEL");
                        String tablilla = rs.getString("LICENSE_PLATE");
                        vehiculosDelUsuario.add(marca + " " + modelo + " (" + tablilla + ")");
                    }

                    rs.close();
                    statement.close();
                    connection.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            runOnUiThread(() -> {
                if (vehiculosDelUsuario.size() == 1) {
                    vehiculosDelUsuario.clear();
                    vehiculosDelUsuario.add("No hay vehículos registrados");
                    Toast.makeText(this, "Registra un vehículo primero en Garage", Toast.LENGTH_LONG).show();
                    spinnerVehiculo.setEnabled(false);
                    btnGuardar.setEnabled(false);
                } else {
                    spinnerVehiculo.setEnabled(true);
                    btnGuardar.setEnabled(true);
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this, android.R.layout.simple_spinner_item, vehiculosDelUsuario);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerVehiculo.setAdapter(adapter);
                configurarListenerVehiculo();
            });
        }).start();
    }

    private void configurarListenerVehiculo() {
        spinnerVehiculo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(
                    AdapterView<?> parent, View view, int position, long id) {
                String seleccion = parent.getItemAtPosition(position).toString();

                if (position > 0 && !seleccion.contains("No hay vehículos")) {
                    int start = seleccion.lastIndexOf('(') + 1;
                    int end = seleccion.lastIndexOf(')');
                    if (start > 0 && end > start) {
                        String vehiculoInfo = seleccion.substring(0, start - 2).trim();
                        String tablilla = seleccion.substring(start, end).trim();
                        // Seteamos ambos en el formulario
                        formulario.setVehiculoSeleccionado(vehiculoInfo);
                        formulario.setTablillaVehiculo(tablilla);
                        tvTablillaSeleccionada.setText("Tablilla: " + tablilla);
                    }
                } else {
                    formulario.setVehiculoSeleccionado(null);
                    formulario.setTablillaVehiculo(null);
                    tvTablillaSeleccionada.setText("Tablilla: (se seleccionará automáticamente)");
                }
                mostrarErrores();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupSpinner() {
        String[] tiposMantenimiento = {
                "Seleccionar tipo...", "Cambio de Aceite de Motor", "Reemplazo de 'Coolant'('Flushing')",
                "Rotación de Gomas", "Alineamiento de gomas","Reemplazo de Gomas", "Reemplazo de Discos y/o Frenos","Otro"
        };
        // We use an ArrayList for the data source so we can add new items dynamically
        List<String> tiposMantenimientoList = new ArrayList<>(Arrays.asList(tiposMantenimiento));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, tiposMantenimiento);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipo.setAdapter(adapter);

        // (opcional) reflejar selección en el formulario y validar al vuelo
        spinnerTipo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String tipo = parent.getItemAtPosition(pos).toString();
                if ("Otro".equals(tipo)) {
                    // If the user selects "Otro", show the input dialog
                    showCustomTypeInputDialog(tiposMantenimientoList, adapter);
                } else {
                    // For any fixed selection, update the form
                    formulario.setTipoMantenimiento(
                            "Seleccionar tipo...".equals(tipo) ? null : tipo
                    );
                }
                mostrarErrores();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
    abstract class SimpleTextWatcher implements android.text.TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }
    private void setupListeners() {
        btnFecha.setOnClickListener(v -> mostrarDatePicker());

        // NUEVO: abrir selector del sistema para elegir una imagen
        btnRecibo.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        btnGuardar.setOnClickListener(v -> guardarServicio());
        btnLimpiar.setOnClickListener(v -> limpiarFormulario());

        // ✅ Actualizar validación en vivo
        etCompania.addTextChangedListener(new SimpleTextWatcher() {
            @Override public void afterTextChanged(Editable s) {
                formulario.setCompania(s.toString().trim());
                mostrarErrores();
            }
        });

        etCosto.addTextChangedListener(new SimpleTextWatcher() {
            @Override public void afterTextChanged(Editable s) {
                formulario.setCosto(s.toString());
                mostrarErrores();
            }
        });
    }

    private void guardarServicio() {
        // 1) Pasar valores actuales de los EditText al formulario
        formulario.setCompania(etCompania.getText().toString().trim());
        formulario.setCosto(etCosto.getText().toString().trim());  // usa tu setCosto(String)

        if (!formulario.validarFormulario()) {
            mostrarErrores();
            return;
        }

        new Thread(() -> {
            String resultado;
            try {
                MyJDBC myJDBC = new MyJDBC();
                Connection connection = myJDBC.obtenerConexion();

                if (connection != null) {
                    // Asegúrate de que estas columnas existen exactamente así en tu tabla
                    String sql = "INSERT INTO SERVICE " +
                            "(SERVICE_TYPE, EMAIL, LICENSE_PLATE, DESCRIPTION, COST_SERVICE, COMPANY_SERVICE, DATE_SERVICE) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)";

                    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                        // SERVICE_TYPE: del spinner (tipo de mantenimiento)
                        String serviceType = formulario.getTipoMantenimiento(); // ej. "Coolant/Anticongelante"
                        // EMAIL: viene del intent
                        String email = userEmail;
                        // LICENSE_PLATE: SOLO la tablilla, no "Marca Modelo (Tablilla)"
                        String licensePlate = formulario.getTablillaVehiculo();
                        // DESCRIPTION: obligatorio NOT NULL en tu DDL
                        String description = "Servicio registrado en app"; // o usa otro campo de UI si tienes
                        // COST_SERVICE: DECIMAL — usa BigDecimal
                        java.math.BigDecimal cost = null;
                        String costoStr = etCosto.getText().toString().trim();
                        if (!costoStr.isEmpty()) {
                            // Normaliza coma/punto si el usuario escribió con coma
                            costoStr = costoStr.replace(',', '.');
                            cost = new java.math.BigDecimal(costoStr);
                        }
                        // COMPANY_SERVICE
                        String company = etCompania.getText().toString().trim();
                        // DATE_SERVICE: si la columna es DATE usa java.sql.Date
                        long fechaMs = formulario.getFechaServicioMs(); // viene del DatePicker (00:00:00 del día)
                        java.sql.Date dateService = new java.sql.Date(fechaMs);
                        // Si tu columna es DATETIME, usa:
                        // java.sql.Timestamp dateService = new java.sql.Timestamp(fechaMs);

                        // Valida claves foráneas: email y tablilla deben existir previamente
                        stmt.setString(1, serviceType);
                        stmt.setString(2, email);
                        stmt.setString(3, licensePlate);
                        stmt.setString(4, description);
                        if (cost != null) {
                            stmt.setBigDecimal(5, cost);
                        } else {
                            stmt.setNull(5, java.sql.Types.DECIMAL);
                        }
                        stmt.setString(6, company);
                        stmt.setDate(7, dateService); // o setTimestamp si usas DATETIME

                        int rows = stmt.executeUpdate();
                        resultado = (rows == 1) ? "✓ Servicio guardado exitosamente"
                                : "No se insertó registro (rows=" + rows + ")";
                    }

                    connection.close();
                } else {
                    resultado = "Error: No se pudo conectar a la base de datos";
                }
            } catch (SQLException e) {
                resultado = "Error SQL: " + e.getMessage();
            } catch (Exception e) {
                resultado = "Error: " + e.getMessage();
            }

            final String finalResultado = resultado;
            runOnUiThread(() -> Toast.makeText(this, finalResultado, Toast.LENGTH_LONG).show());
        }).start();
    }

    private void mostrarDatePicker() {
        Calendar hoy = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String fecha = String.format(Locale.US, "%02d-%02d-%04d", dayOfMonth, month + 1, year);
                    tvFechaSeleccionada.setText("Fecha: " + fecha);

                    Calendar seleccion = Calendar.getInstance();
                    seleccion.set(year, month, dayOfMonth, 0, 0, 0);
                    seleccion.set(Calendar.MILLISECOND, 0);

                    formulario.setFechaServicioMs(seleccion.getTimeInMillis());
                    mostrarErrores();
                },
                hoy.get(Calendar.YEAR),
                hoy.get(Calendar.MONTH),
                hoy.get(Calendar.DAY_OF_MONTH)
        );

        // 🔒 Bloquear fechas futuras (permitir hoy y pasadas)
        // Opción 1 (sencilla): hasta este instante
        // dialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        // Opción 2 (más precisa): hasta el final del día de hoy (23:59:59.999)
        Calendar finDeHoy = Calendar.getInstance();
        finDeHoy.set(Calendar.HOUR_OF_DAY, 23);
        finDeHoy.set(Calendar.MINUTE, 59);
        finDeHoy.set(Calendar.SECOND, 59);
        finDeHoy.set(Calendar.MILLISECOND, 999);
        dialog.getDatePicker().setMaxDate(finDeHoy.getTimeInMillis());

        dialog.show();
    }

    private void mostrarErrores() {
        if (formulario.tieneErrores()) {
            StringBuilder errores = new StringBuilder("Errores:\n");
            for (Object error : formulario.getErrores().values()) {
                errores.append("• ").append(error).append("\n");
            }
            tvErrores.setText(errores.toString());
            tvErrores.setVisibility(View.VISIBLE);
        } else {
            tvErrores.setVisibility(View.GONE);
        }
    }

    private void limpiarFormulario() {
        formulario.limpiarFormulario();
        etCompania.setText("");
        etCosto.setText("");
        spinnerTipo.setSelection(0);
        spinnerVehiculo.setSelection(0);
        tvTablillaSeleccionada.setText("Tablilla: (se seleccionará automáticamente)");
        tvFechaSeleccionada.setText("Fecha no seleccionada");
        tvErrores.setVisibility(View.GONE);
        resetearBotonRecibo();
    }

    private void resetearBotonRecibo() {
        btnRecibo.setText("Subir Recibo");
        btnRecibo.setEnabled(true);
        btnRecibo.setAlpha(1f);
        reciboUri = null;
        if (receiptImage != null) {
            receiptImage.setImageDrawable(null);
        }
    }

    /**
     * Muestra un AlertDialog con un campo EditText para que el usuario introduzca un
     * tipo de mantenimiento personalizado y lo añade al Spinner.
     * @param dataList La lista de opciones del Spinner
     * @param adapter El adaptador del Spinner
     */
    private void showCustomTypeInputDialog(List<String> dataList, ArrayAdapter<String> adapter) {
        // Necesitamos una referencia a la última opción seleccionada para restaurarla si el usuario cancela
        final int lastPosition = spinnerTipo.getSelectedItemPosition();

        // Crear el EditText para la entrada del usuario
        final EditText input = new EditText(this);
        input.setHint("Ej. Reemplazo de Filtro de Aire");

        new AlertDialog.Builder(this)
                .setTitle("Tipo de Mantenimiento Personalizado")
                .setMessage("Introduce el nuevo tipo de servicio:")
                .setView(input) // Añadir el EditText al diálogo
                .setPositiveButton("Añadir", (dialog, whichButton) -> {
                    String customOption = input.getText().toString().trim();

                    if (!customOption.isEmpty()) {
                        // 1. Insertar la nueva opción ANTES de "Otro"
                        // Esto mantiene a "Otro" siempre al final
                        int otroIndex = dataList.indexOf("Otro");
                        if (otroIndex != -1) {
                            dataList.add(otroIndex, customOption);
                        } else {
                            dataList.add(customOption); // En caso de que "Otro" no se encuentre
                        }

                        // 2. Notificar al adaptador para que actualice el Spinner
                        adapter.notifyDataSetChanged();

                        // 3. Establecer el nuevo elemento como la selección actual
                        spinnerTipo.setSelection(dataList.indexOf(customOption));

                        // 4. Actualizar el formulario con el nuevo tipo
                        formulario.setTipoMantenimiento(customOption);

                        Toast.makeText(this, "Tipo de servicio añadido: " + customOption, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "El campo no puede estar vacío.", Toast.LENGTH_SHORT).show();
                        // Si el usuario deja vacío, forzamos la selección a volver al elemento anterior
                        spinnerTipo.setSelection(lastPosition);
                    }
                })
                .setNegativeButton("Cancelar", (dialog, whichButton) -> {
                    // Si el usuario cancela, volvemos a la opción que estaba seleccionada antes de "Otro"
                    spinnerTipo.setSelection(lastPosition);
                    dialog.cancel();
                })
                .show();
    }
}
