package pupr.capstone.myapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import android.content.Intent;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Muestra un spinner con los autos del usuario (por email) y,
 * al escoger, actualiza la tarjeta con la info del auto elegido.
 */
public class ServicesSelect extends AppCompatActivity {

    // Views del layout activity_services
    private Spinner spinnerVehiculo;                 // ID esperado: spinnerVehiculo
    private ImageView ivAuto;                        // ID esperado: ivAuto
    private TextView tvEntrega;                      // ID esperado: tvEntrega
    private TextView tvTituloAuto;                   // ID esperado: tvTituloAuto
    private TextView tvSubtitulo;                    // ID esperado: tvSubtitulo
    private View btnAddMaintenance;                  // ID esperado: btnAddMaintenance
    private View btnCreateReport;                    // ID esperado: btnCreateReport

    // Datos
    private String userEmail;
    private final List<VehicleItem> vehiculos = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter;

    // Recurso opcional si no hay imagen en la BD
    private static final int PLACEHOLDER_RES = R.drawable.default_car_image; // crea uno si no existe

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_services);

        // 1) Email del intent
        userEmail = getIntent().getStringExtra("email");
        if (userEmail == null || userEmail.trim().isEmpty()) {
            Toast.makeText(this, "No se recibió el email del usuario.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // 2) Bind de vistas
        bindViews();

        // 3) Estado inicial del spinner
        List<String> cargando = new ArrayList<>();
        cargando.add("Cargando vehículos...");
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cargando);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerVehiculo.setAdapter(spinnerAdapter);
        spinnerVehiculo.setEnabled(false);

        // 4) Cargar vehículos desde MySQL
        cargarVehiculosDesdeBD();

        // 5) Listener de selección
        spinnerVehiculo.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                // La lista vehiculos NO incluye el placeholder; si el primer item del adapter es “Seleccione…”
                // entonces el índice en vehiculos será position-1.
                String label = (String) parent.getItemAtPosition(position);
                if (vehiculos.isEmpty() || label.startsWith("Seleccione") || label.startsWith("No hay")) {
                    limpiarCard();
                    return;
                }
                int idx = position - 1; // porque en el adapter agregamos un encabezado "Seleccione..."
                if (idx >= 0 && idx < vehiculos.size()) {
                    mostrarEnCard(vehiculos.get(idx), position); // position visible 1..N para "Entregas #"
                }
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // 6) Clicks opcionales (puedes abrir Activities con el auto seleccionado)
        btnAddMaintenance.setOnClickListener(v -> {
            int pos = spinnerVehiculo.getSelectedItemPosition();
            if (!vehiculoElegidoValido()) {
                Toast.makeText(this, "Selecciona un vehículo primero", Toast.LENGTH_SHORT).show();
                return;
            }
            VehicleItem item = vehiculos.get(pos - 1);
            Intent i = new Intent(this, ServicesActivity.class);
            i.putExtra("email", userEmail);
            i.putExtra("brand", item.brand);
            i.putExtra("model", item.model);
            i.putExtra("license_plate", item.plate);
            i.putExtra("vehiculoSeleccionado", item.brand + " " + item.model + " (" + item.plate + ")");
            startActivity(i);
        });

        btnCreateReport.setOnClickListener(v -> {
            if (!vehiculoElegidoValido()) {
                Toast.makeText(this, "Selecciona un vehículo primero", Toast.LENGTH_SHORT).show();
                return;
            }

            // Item seleccionado
            VehicleItem item = vehiculos.get(spinnerVehiculo.getSelectedItemPosition() - 1);

            // Selector de rango (MaterialDatePicker)
            com.google.android.material.datepicker.MaterialDatePicker.Builder<androidx.core.util.Pair<Long, Long>> builder =
                    com.google.android.material.datepicker.MaterialDatePicker.Builder.dateRangePicker()
                            .setTitleText("Selecciona el rango de fechas");

            final com.google.android.material.datepicker.MaterialDatePicker<androidx.core.util.Pair<Long, Long>> picker =
                    builder.build();

            picker.addOnPositiveButtonClickListener(selection -> {
                if (selection == null || selection.first == null || selection.second == null) {
                    Toast.makeText(this, "Rango inválido", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Normaliza a comienzo/fin del día en la zona del dispositivo
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.setTimeInMillis(selection.first);
                setToStartOfDay(cal);
                java.util.Date startDate = cal.getTime();

                cal.setTimeInMillis(selection.second);
                setToEndOfDay(cal);
                java.util.Date endDate = cal.getTime();

                Integer year = null;
                try { year = item.year == null || item.year.isEmpty() ? null : Integer.parseInt(item.year); }
                catch (Exception ignore) {}

                try {
                    android.net.Uri uri = VehicleReportExporter.export(
                            this,
                            userEmail,
                            item.brand,
                            item.model,
                            year,
                            item.plate,
                            item.image,     // Bitmap (puede ser null)
                            startDate,
                            endDate
                    );
                    Toast.makeText(this, "PDF guardado en Descargas", Toast.LENGTH_LONG).show();
                    VehicleReportExporter.openPdf(this, uri);
                } catch (Exception e) {
                    Toast.makeText(this, "Error al crear PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

            picker.show(getSupportFragmentManager(), "rangePicker");
        });


        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        BottomNavRouter.setup(
                this,
                bottomNav,
                R.id.nav_informe,   // <- id del item que representa ESTA pantalla
                userEmail           // <- opcional; pásalo si lo usas entre pantallas
        );
    }
    private static void setToStartOfDay(java.util.Calendar c) {
        c.set(java.util.Calendar.HOUR_OF_DAY, 0);
        c.set(java.util.Calendar.MINUTE, 0);
        c.set(java.util.Calendar.SECOND, 0);
        c.set(java.util.Calendar.MILLISECOND, 0);
    }

    private static void setToEndOfDay(java.util.Calendar c) {
        c.set(java.util.Calendar.HOUR_OF_DAY, 23);
        c.set(java.util.Calendar.MINUTE, 59);
        c.set(java.util.Calendar.SECOND, 59);
        c.set(java.util.Calendar.MILLISECOND, 999);
    }

    private void bindViews() {
        spinnerVehiculo   = findViewById(R.id.spinnerVehiculo);
        ivAuto            = findViewById(R.id.ivAuto);
        tvEntrega         = findViewById(R.id.tvEntrega);
        tvTituloAuto      = findViewById(R.id.tvTituloAuto);
        tvSubtitulo       = findViewById(R.id.tvSubtitulo);
        btnAddMaintenance = findViewById(R.id.btnAddMaintenance);
        btnCreateReport   = findViewById(R.id.btnCreateReport);
    }

    private void cargarVehiculosDesdeBD() {
        new Thread(() -> {
            List<VehicleItem> tmp = new ArrayList<>();
            try {
                MyJDBC myJDBC = new MyJDBC();
                try (Connection cn = myJDBC.obtenerConexion()) {
                    if (cn != null) {
                        // Si no tienes YEAR, quítalo del SELECT
                        String sql = "SELECT BRAND, MODEL, LICENSE_PLATE, IMAGE, YEAR FROM VEHICLE WHERE EMAIL = ? ORDER BY BRAND, MODEL";
                        try (PreparedStatement ps = cn.prepareStatement(sql)) {
                            ps.setString(1, userEmail);
                            try (ResultSet rs = ps.executeQuery()) {
                                while (rs.next()) {
                                    String brand = safe(rs, "BRAND");
                                    String model = safe(rs, "MODEL");
                                    String plate = safe(rs, "LICENSE_PLATE");
                                    String year  = safe(rs, "YEAR"); // si no existe, quedará vacío

                                    Bitmap bmp = null;
                                    byte[] blob = null;
                                    try { blob = rs.getBytes("IMAGE"); } catch (Exception ignore) {}
                                    if (blob != null && blob.length > 0) {
                                        bmp = BitmapFactory.decodeByteArray(blob, 0, blob.length);
                                    }

                                    tmp.add(new VehicleItem(brand, model, plate, year, bmp));
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Error al cargar vehículos: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }

            runOnUiThread(() -> {
                vehiculos.clear();
                vehiculos.addAll(tmp);

                List<String> labels = new ArrayList<>();
                if (vehiculos.isEmpty()) {
                    labels.add("No hay vehículos registrados");
                    spinnerVehiculo.setEnabled(false);
                } else {
                    labels.add("Seleccione un vehículo...");
                    for (VehicleItem v : vehiculos) {
                        String title = v.brand + " " + v.model + (v.year.isEmpty() ? "" : (" " + v.year));
                        labels.add(title + " (" + v.plate + ")");
                    }
                    spinnerVehiculo.setEnabled(true);
                }

                spinnerAdapter.clear();
                spinnerAdapter.addAll(labels);
                spinnerAdapter.notifyDataSetChanged();

                // Estado inicial de la tarjeta
                limpiarCard();
            });
        }).start();
    }

    private void mostrarEnCard(VehicleItem v, int spinnerPositionVisible) {
        // "Entregas #1", etc. (spinnerPositionVisible empieza en 1 por el encabezado)
        tvEntrega.setText("Vehiculo #" + (spinnerPositionVisible));
        String title = v.brand + " " + v.model + (v.year.isEmpty() ? "" : (" " + v.year));
        tvTituloAuto.setText(title);
        tvSubtitulo.setText("Tablilla " + v.plate);

        if (v.image != null) {
            ivAuto.setImageBitmap(v.image);
        } else {
            ivAuto.setImageResource(PLACEHOLDER_RES);
        }
    }

    private void limpiarCard() {
        tvEntrega.setText("Vehiculo #—");
        tvTituloAuto.setText("Selecciona un vehículo");
        tvSubtitulo.setText("");
        ivAuto.setImageResource(PLACEHOLDER_RES);
    }

    private boolean vehiculoElegidoValido() {
        int pos = spinnerVehiculo.getSelectedItemPosition();
        return !vehiculos.isEmpty() && pos > 0 && (pos - 1) < vehiculos.size();
        // pos 0 es el “Seleccione…”
    }

    private static String safe(ResultSet rs, String col) {
        try {
            String v = rs.getString(col);
            return v == null ? "" : v;
        } catch (Exception e) {
            return ""; // por si la columna (YEAR) no existe
        }
    }

    // Estructura simple para el spinner y la tarjeta
    private static class VehicleItem {
        final String brand;
        final String model;
        final String plate;
        final String year; // puede estar vacío
        final Bitmap image;

        VehicleItem(String brand, String model, String plate, String year, Bitmap image) {
            this.brand = brand;
            this.model = model;
            this.plate = plate;
            this.year = year == null ? "" : year.trim();
            this.image = image;
        }
    }
}
