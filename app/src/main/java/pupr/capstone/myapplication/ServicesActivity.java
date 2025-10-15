package pupr.capstone.myapplication;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

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
                    String query = "SELECT BRAND, MODEL, LICENSE_PLATE FROM VEHICULO WHERE EMAIL = ? ORDER BY BRAND, MODEL";
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
                "Seleccionar tipo...", "Aceite de Motor", "Coolant/Anticongelante",
                "Rotación de Gomas", "Filtro de Aire", "Sistema de Frenos"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, tiposMantenimiento);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipo.setAdapter(adapter);

        // (opcional) reflejar selección en el formulario y validar al vuelo
        spinnerTipo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String tipo = parent.getItemAtPosition(pos).toString();
                formulario.setTipoMantenimiento(
                        "Seleccionar tipo...".equals(tipo) ? null : tipo
                );
                mostrarErrores();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupListeners() {
        btnFecha.setOnClickListener(v -> mostrarDatePicker());

        // NUEVO: abrir selector del sistema para elegir una imagen
        btnRecibo.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        btnGuardar.setOnClickListener(v -> guardarServicio());
        btnLimpiar.setOnClickListener(v -> limpiarFormulario());
    }

    private void guardarServicio() {
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
                    String query = "INSERT INTO SERVICIOS (EMAIL, VEHICULO, TIPO, COMPANIA, COSTO, FECHA) " +
                            "VALUES (?, ?, ?, ?, ?, ?)";
                    PreparedStatement stmt = connection.prepareStatement(query);
                    stmt.setString(1, userEmail);
                    stmt.setString(2, formulario.getVehiculoSeleccionado());
                    stmt.setString(3, formulario.getTipoMantenimiento());
                    stmt.setString(4, etCompania.getText().toString());
                    stmt.setString(5, etCosto.getText().toString());
                    stmt.setLong(6, formulario.getFechaServicioMs());
                    stmt.executeUpdate();

                    stmt.close();
                    connection.close();
                    resultado = "✓ Servicio guardado exitosamente";
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
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    String fecha = String.format(Locale.US, "%02d-%02d-%04d", day, month + 1, year);
                    tvFechaSeleccionada.setText("Fecha: " + fecha);
                    Calendar seleccion = Calendar.getInstance();
                    seleccion.set(year, month, day);
                    formulario.setFechaServicioMs(seleccion.getTimeInMillis());
                    mostrarErrores();
                },
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
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
}
