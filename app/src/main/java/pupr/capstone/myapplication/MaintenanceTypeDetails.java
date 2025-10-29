package pupr.capstone.myapplication;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Locale;

public class MaintenanceTypeDetails extends AppCompatActivity {

    // --- Constants ---
    private static final String DATE_FORMAT = "MMM-dd-yyyy";
    private final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT, Locale.US);

    // --- View Declarations (Grouped by type) ---
    private ImageView imageMaintenance;
    private TextView txtTitle, txtNote, lblLastMaintenance, lblSelectDate, lblNextMaintenance, lblDateMaintenance;
    private EditText txtLastDateMaintenance, txtNextMaintenance, txtSelectDate, txtDateMaintenance;
    private Button btnUpdate;
    private FloatingActionButton backButton, editButton;

    // --- Business Logic Fields ---
    private NotificationHelper notificationHelper;
    private boolean editToken = false;
    private boolean mileageTrigger = false;
    private boolean timeTrigger = false;

    private String maintenanceType, carLicensePlate, userEmail;
    private int daysToAdd, mileageRate, carMileage;
    private int mileageDue = 0; // Initialize to 0

    private LocalDate lastMaintenanceDate;
    private LocalDate alertDate = null;

    // --- Intent Data Fields for Notifications/DB ---
    private String carBrand, carModel;
    private String note;
    private int pictureKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maintenance_screen); // Use your actual XML layout name

        bindViews();
        handleIntentExtras();
        initializeNotificationSystem();
        checkTriggerofMaintenance(mileageRate, daysToAdd);
        setupMaintenanceDisplay();
        checkExistingAlertDate();
        displayInitialData();
        setupClickListeners();
    }

    // --- Initialization & Setup ---

    private void bindViews() {
        imageMaintenance = findViewById(R.id.imageMaintenance);
        txtTitle = findViewById(R.id.txtTitle);
        txtNote = findViewById(R.id.txtNote);
        txtLastDateMaintenance = findViewById(R.id.txtLastDateMaintenance);
        txtNextMaintenance = findViewById(R.id.txtNextMaintenance);
        txtDateMaintenance = findViewById(R.id.txtDateMaintenance);
        txtSelectDate = findViewById(R.id.txtSelectDate);
        btnUpdate = findViewById(R.id.btnUpdateMaintenance);
        backButton = findViewById(R.id.backButton);
        editButton = findViewById(R.id.editButton);
        lblLastMaintenance = findViewById(R.id.lblLastMaintenance);
        lblNextMaintenance = findViewById(R.id.lblNextMaintenance);
        lblDateMaintenance = findViewById(R.id.lblDateMaintenance);
        lblSelectDate = findViewById(R.id.lblSelectDate);
    }

    private void handleIntentExtras() {
        Intent intent = getIntent();
        maintenanceType = intent.getStringExtra("type");
        daysToAdd = intent.getIntExtra("time", 0);
        carLicensePlate = intent.getStringExtra("license_plate");
        carMileage = intent.getIntExtra("car_mileage", 0);
        userEmail = intent.getStringExtra("email");

        mileageRate = intent.getIntExtra("mileage_rate", 0);
        int mileageOther = intent.getIntExtra("mileage_rate_other", 0); // Unused, but kept for context
        pictureKey = intent.getIntExtra("picture", 0);
        carBrand = intent.getStringExtra("marca");
        carModel = intent.getStringExtra("model");
        note = intent.getStringExtra("note");
    }

    private void initializeNotificationSystem() {
        notificationHelper = new NotificationHelper(this);
        notificationHelper.createNotificationChannel();
        requestNotificationPermission();
    }

    private void displayInitialData() {
        // Pinta datos
        String titleText = (maintenanceType != null) ?
                maintenanceType :
                carBrand + "\n" + carModel + "\n" + carLicensePlate;
        txtTitle.setText(titleText);
        txtNote.setText((note != null && !note.isEmpty()) ? note : " ");

        int resId = mapPictureKeyToDrawable(pictureKey);
        imageMaintenance.setImageResource(resId);
    }

    private void setupClickListeners() {
        // Back Button
        backButton.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        // DatePickers
        // Last Maintenance Date Picker (for time-only or both)
        txtLastDateMaintenance.setOnClickListener(v -> lastMaintenanceDate = showDatePicker(txtLastDateMaintenance));
        // Select Date Picker (only for time when 'both' is active)
        txtSelectDate.setOnClickListener(v -> lastMaintenanceDate = showDatePicker(txtSelectDate));


        // Edit Button
        editButton.setOnClickListener(v -> toggleEditMode(true));

        // Update Button
        btnUpdate.setOnClickListener(v -> {
            if (editToken) {
                editAlertInDB();
            } else {
                uploadAlertToDB();
            }
            // Schedule the notification/email regardless of upload/edit success for now
            if (alertDate != null) {
                scheduleNotificationManagement(carBrand, carModel, maintenanceType, carLicensePlate, alertDate, userEmail);
                Toast.makeText(this, "Actualizado", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error: No se pudo calcular la fecha de alerta.", Toast.LENGTH_LONG).show();
            }
            finishAndNavigateToMaintenanceActivity();
        });

    }

    private void toggleEditMode(boolean enable) {
        if (!enable) return;

        editToken = true;

        // Try to parse the next maintenance date to pre-fill the last maintenance date field
        String nextDate = txtNextMaintenance.getText().toString().trim();
        txtNextMaintenance.setText("");
        if (!nextDate.isEmpty() && nextDate.contains("-")) { // Simple check for date format
            try {
                // If it's a date, set it to the last date field to allow easy re-scheduling
                LocalDate mantLastDate = LocalDate.parse(nextDate, DATE_FORMATTER);
                txtLastDateMaintenance.setText(mantLastDate.format(DATE_FORMATTER));
            } catch (Exception e) {
                // If it's a mileage or parsing fails, just leave it alone
                Log.e("EditMode", "Could not parse next maintenance date: " + e.getMessage());
            }
        }

        txtLastDateMaintenance.setEnabled(true);
        txtNextMaintenance.setEnabled(true);
        lblLastMaintenance.setVisibility(View.VISIBLE);
        txtLastDateMaintenance.setVisibility(View.VISIBLE);
        btnUpdate.setEnabled(true);
        btnUpdate.setText("Actualizar");

        // Show date pickers needed for the specific maintenance type
        if (mileageTrigger && timeTrigger) {
            LocalDate mantLastDate = LocalDate.parse(nextDate, DATE_FORMATTER);
            txtLastDateMaintenance.setText(String.format(Locale.US, "%,d mi", carMileage));
            txtNextMaintenance.setText(String.format(Locale.US, "%,d mi", mileageDue));
            lblSelectDate.setVisibility(View.VISIBLE);
            txtSelectDate.setVisibility(View.VISIBLE);
            txtSelectDate.setText(mantLastDate.format(DATE_FORMATTER));
            lblDateMaintenance.setVisibility(View.VISIBLE);
            txtDateMaintenance.setVisibility(View.VISIBLE);
        }
    }


    // --- Core Logic ---

    private void checkTriggerofMaintenance(int mileage, int daysToAdd) {
        this.mileageTrigger = (mileage != 0);
        this.timeTrigger = (daysToAdd != 0);
    }

    private void setupMaintenanceDisplay() {
        if (mileageTrigger && timeTrigger) {
            mileageDue = carMileage + mileageRate;
            displayBoth();
        } else if (timeTrigger) {
            displayTimeOnly();
        } else if (mileageTrigger) {
            mileageDue = carMileage + mileageRate;
            displayMileageOnly();
        } else {
            displayNone();
        }
    }

    private LocalDate calculateAlertDate(EditText dateSource, int days) {
        String lastDate = dateSource.getText().toString().trim();
        if (lastDate.isEmpty()) {
            Toast.makeText(this, "Debe seleccionar una fecha", Toast.LENGTH_LONG).show();
            return null;
        }

        try {
            LocalDate mantLastDate = LocalDate.parse(lastDate, DATE_FORMATTER);
            return mantLastDate.plusDays(days);
        } catch (Exception e) {
            Toast.makeText(this, "Error al procesar la fecha: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return null;
        }
    }

    private void checkExistingAlertDate() {
        LocalDate existingAlertDate = getExistingAlertDate(userEmail, carLicensePlate, maintenanceType);

        if (existingAlertDate != null) {
            alertDate = existingAlertDate;
            String formattedDate = existingAlertDate.format(DATE_FORMATTER);

            // Update the correct date field based on the trigger type
            if (timeTrigger || (mileageTrigger && timeTrigger)) {
                txtNextMaintenance.setText(formattedDate);
            } else if (mileageTrigger && timeTrigger) {
                txtDateMaintenance.setText(formattedDate);
            }

            // Disable editing since an alert is set
            btnUpdate.setEnabled(false);
            lblLastMaintenance.setVisibility(View.GONE);
            txtLastDateMaintenance.setVisibility(View.GONE);
            lblSelectDate.setVisibility(View.GONE);
            txtSelectDate.setVisibility(View.GONE);
            btnUpdate.setText("Alerta Establecida");
            Toast.makeText(this, "Alerta ya está establecida para: " + formattedDate, Toast.LENGTH_LONG).show();
        } else {
            // Ensure the button is enabled if no alert exists
            btnUpdate.setEnabled(true);
            btnUpdate.setText("Guardar Alerta");
        }
    }


    // --- Display Methods (Refined) ---

    private void displayNone() {
        // Hide all relevant UI elements
        lblLastMaintenance.setVisibility(View.GONE);
        txtLastDateMaintenance.setVisibility(View.GONE);
        lblNextMaintenance.setVisibility(View.GONE);
        txtNextMaintenance.setVisibility(View.GONE);
        lblSelectDate.setVisibility(View.GONE);
        txtSelectDate.setVisibility(View.GONE);
        lblDateMaintenance.setVisibility(View.GONE);
        txtDateMaintenance.setVisibility(View.GONE);

        btnUpdate.setEnabled(false);
        btnUpdate.setVisibility(View.GONE);
    }

    private void displayMileageOnly() {
        // First Line: Last Known Mileage
        lblLastMaintenance.setText("Millaje:");
        txtLastDateMaintenance.setText(String.format(Locale.US, "%,d mi", carMileage));
        txtLastDateMaintenance.setEnabled(false);

        // Second Line: Next Maintenance Mileage
        lblNextMaintenance.setText("Próximo Servicio:");
        txtNextMaintenance.setText(String.format(Locale.US, "%,d mi", mileageDue));

        // Hide time-related fields
        lblSelectDate.setVisibility(View.GONE);
        txtSelectDate.setVisibility(View.GONE);
        lblDateMaintenance.setVisibility(View.GONE);
        txtDateMaintenance.setVisibility(View.GONE);
    }

    private void displayTimeOnly() {
        // First Line: Last Maintenance Date
        lblLastMaintenance.setText("Último Servicio:");
        txtLastDateMaintenance.setHint("Seleccione" );
        txtLastDateMaintenance.setEnabled(true);

        // Second Line: Next Maintenance Date (Result)
        lblNextMaintenance.setText("Próximo Servicio:");
        //txtNextMaintenance.setHint("Calculado al guardar");
        txtNextMaintenance.setEnabled(false);

        // Hide secondary fields
        lblSelectDate.setVisibility(View.GONE);
        txtSelectDate.setVisibility(View.GONE);
        lblDateMaintenance.setVisibility(View.GONE);
        txtDateMaintenance.setVisibility(View.GONE);
    }

    private void displayBoth() {
        // First Group: Mileage
        lblLastMaintenance.setText("Millaje:");
        txtLastDateMaintenance.setText(String.format(Locale.US, "%,d mi", carMileage));
        txtLastDateMaintenance.setEnabled(false);
        lblNextMaintenance.setText("Próximo Servicio:");
        txtNextMaintenance.setText(String.format(Locale.US, "%,d mi", mileageDue));
        txtNextMaintenance.setEnabled(false);


        // Second Group: Time
        lblSelectDate.setVisibility(View.VISIBLE);
        txtSelectDate.setVisibility(View.VISIBLE);
        lblDateMaintenance.setVisibility(View.VISIBLE);
        txtDateMaintenance.setVisibility(View.VISIBLE);

        lblSelectDate.setText("Último Servicio:");
        txtSelectDate.setHint("Seleccione");
        txtSelectDate.setEnabled(true);

        lblDateMaintenance.setText("Próxima Fecha:");
        txtDateMaintenance.setHint("Calculado");
        txtDateMaintenance.setEnabled(false);
    }

    // --- DB Interaction ---

    private void uploadAlertToDB() {
        // Re-calculate the alert date based on the *current* state of the input fields
        if (timeTrigger && !mileageTrigger) {
            alertDate = calculateAlertDate(txtLastDateMaintenance, daysToAdd);
            if(alertDate == null) return;
            txtNextMaintenance.setText(alertDate.format(DATE_FORMATTER));

        } else if (timeTrigger && mileageTrigger) {
            alertDate = calculateAlertDate(txtSelectDate, daysToAdd);
            if(alertDate == null) return;
            txtDateMaintenance.setText(alertDate.format(DATE_FORMATTER));
        }

        if (alertDate == null && mileageDue == 0) {
            Toast.makeText(this, "Error: Faltan datos para crear la alerta.", Toast.LENGTH_LONG).show();
            return;
        }

        // Logic for inserting into DB
        try {
            // Use the correct date field for insertion (only one date is needed in the DB)
            LocalDate finalAlertDate = (alertDate != null) ? alertDate : LocalDate.now().plusYears(0); // Placeholder if only mileage is tracked
            int finalMileageDue = mileageDue;

            // Assuming MyJDBC and Connection are correct
            MyJDBC jdbc = new MyJDBC();
            Connection con = jdbc.obtenerConexion();

            if (con != null) {
                String query = "INSERT INTO ALERT (EMAIL,LICENSE_PLATE, NAME_ALERT, ALERT_DATE, MILEAGE_DUE) VALUES (?,?, ?, ?, ?)";
                PreparedStatement stmt = con.prepareStatement(query);
                stmt.setString(1, userEmail);
                stmt.setString(2, carLicensePlate);
                stmt.setString(3, maintenanceType);
                stmt.setString(4, finalAlertDate.toString());
                stmt.setInt(5, finalMileageDue);

                int rows = stmt.executeUpdate();

                if (rows > 0) {
                    Toast.makeText(this, "Recordatorio de " + maintenanceType + " activado", Toast.LENGTH_LONG).show();
                    finishAndNavigateToGarage();
                } else {
                    Toast.makeText(this, "Error al guardar la alerta", Toast.LENGTH_SHORT).show();
                }
                con.close();
            } else {
                Toast.makeText(this, "No se pudo conectar con la base de datos", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error de DB: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    private void editAlertInDB() {
        // Re-calculate the alert date based on the *current* state of the input fields
        if (timeTrigger && !mileageTrigger) {
            alertDate = calculateAlertDate(txtLastDateMaintenance, daysToAdd);
            if(alertDate == null) return;
            txtNextMaintenance.setText(alertDate.format(DATE_FORMATTER));

        } else if (timeTrigger && mileageTrigger) {
            alertDate = calculateAlertDate(txtSelectDate, daysToAdd);
            if(alertDate == null) return;
            txtDateMaintenance.setText(alertDate.format(DATE_FORMATTER));
        }

        if (alertDate == null && mileageDue == 0) {
            Toast.makeText(this, "Error: Faltan datos para editar la alerta.", Toast.LENGTH_LONG).show();
            return;
        }

        // Logic for updating the DB
        try {
            LocalDate finalAlertDate = (alertDate != null) ? alertDate : LocalDate.now().plusYears(0); // Placeholder
            int finalMileageDue = mileageDue;

            MyJDBC jdbc = new MyJDBC();
            Connection con = jdbc.obtenerConexion();

            if (con != null) {
                // *** FIX: Corrected SQL UPDATE syntax ***
                String query = "UPDATE ALERT SET ALERT_DATE = ?, MILEAGE_DUE = ? WHERE NAME_ALERT = ? AND EMAIL = ? AND LICENSE_PLATE = ?";
                PreparedStatement stmt = con.prepareStatement(query);
                stmt.setString(1, finalAlertDate.toString());
                stmt.setInt(2, finalMileageDue);
                stmt.setString(3, maintenanceType);
                stmt.setString(4, userEmail);
                stmt.setString(5, carLicensePlate);

                int rows = stmt.executeUpdate();

                if (rows > 0) {
                    Toast.makeText(this, "Recordatorio de " + maintenanceType + " editado", Toast.LENGTH_LONG).show();
                    finishAndNavigateToGarage();
                } else {
                    Toast.makeText(this, "Error al editar la alerta", Toast.LENGTH_SHORT).show();
                }
                con.close();
            } else {
                Toast.makeText(this, "No se pudo conectar con la base de datos", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error de DB: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void finishAndNavigateToGarage() {
        finish();
        Intent i = new Intent(this, GarageActivity.class);
        i.putExtra("email", userEmail);
        startActivity(i);
    }

    // --- Helper Methods ---

    // The rest of your helper methods (`getExistingAlertDate`, `scheduleTestNotification`,
    // `requestNotificationPermission`, `scheduleNotificationManagement`, `showDatePicker`,
    // and `mapPictureKeyToDrawable`) are functional and included below for completeness.

    /**
     * Shows a DatePicker dialog and updates the target EditText with the selected date.
     * @param target The EditText to display the formatted date in.
     * @return The initial LocalDate (before user selection). You should use the text in `target` to get the *selected* date.
     */
    private LocalDate showDatePicker(EditText target) {
        Calendar selectedDate = Calendar.getInstance();
        int y = selectedDate.get(Calendar.YEAR);
        int m = selectedDate.get(Calendar.MONTH);
        int d = selectedDate.get(Calendar.DAY_OF_MONTH);
        long now=System.currentTimeMillis();

        DatePickerDialog  datePickerDialog=new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar newDate = Calendar.getInstance();
            newDate.set(year, month, dayOfMonth);

            // Use SimpleDateFormat for correct formatting, including month name (MMM).
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(DATE_FORMAT, Locale.US);
            String formattedDate = sdf.format(newDate.getTime());
            target.setText(formattedDate);

        }, y, m, d);

        datePickerDialog.getDatePicker().setMaxDate(now);
        datePickerDialog.show();

        // Returns today's date (or the initial date). The actual selected date is in the EditText.
        return LocalDate.of(y, m + 1, d);
    }

    // [The rest of your existing helper methods: getExistingAlertDate, scheduleTestNotification,
    //  requestNotificationPermission, scheduleNotificationManagement, mapPictureKeyToDrawable remain here]

    public LocalDate getExistingAlertDate(String email, String licensePlate, String maintenanceType) {
        // ... (your existing implementation) ...
        String query = "SELECT ALERT_DATE FROM ALERT WHERE EMAIL = ? AND LICENSE_PLATE = ? AND NAME_ALERT = ?";
        LocalDate existingDate = null;

        MyJDBC jdbc = new MyJDBC();
        java.sql.Connection con = jdbc.obtenerConexion();

        try (
                java.sql.PreparedStatement stmt = con.prepareStatement(query)) {

            stmt.setString(1, email);
            stmt.setString(2, licensePlate);
            stmt.setString(3, maintenanceType);

            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String dateString = rs.getString("ALERT_DATE");
                    existingDate = LocalDate.parse(dateString);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return existingDate;
    }


    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        101
                );
            }
        }
    }

    public void scheduleNotificationManagement(String brand,String model,String maintenanceType, String plate,LocalDate alertDate, String userEmail){

        String title = "⚠️ Recordatorio Urgente de Mantenimiento - AL DIA APP";
        String message =
                "**Estimado(a) usuario(a),\n\n" +
                        "El Equipo de AL DIA APP le envía esta importante notificación para recordarle que " +
                        "su vehículo requiere una atención inmediata.\n\n" +
                        "Detalles del Vehículo y Mantenimiento:\n" +
                        "Vehículo: " + brand + " " + model + "\n" +
                        "Tablilla: " + plate + "\n" +
                        "Mantenimiento Requerido: " + maintenanceType + "\n" +
                        "Estado: Vence Hoy.\n\n" +
                        "Para garantizar el rendimiento óptimo, la seguridad y la validez de su garantía, " +
                        "le solicitamos programar este servicio lo antes posible.\n\n" +
                        "Agradecemos su atención y le deseamos un excelente día.\n\n" +
                        "Saludos cordiales,\n" +
                        "El Equipo de AL DIA APP";

        NotificationHelper.scheduleMaintenanceEmailAt7am(
                this,
                alertDate,
                userEmail,
                title,
                message
        );


        Toast.makeText(this,
                "Actualizado y correo programado para las 7:00 AM del " + alertDate.format(DATE_FORMATTER),
                Toast.LENGTH_LONG
        ).show();
    }

    private void finishAndNavigateToMaintenanceActivity() {
        finish();
        Intent intent = new Intent(this, MaintenanceActivity.class);

        intent.putExtra("marca", carBrand);
        intent.putExtra("license_plate", carLicensePlate);
        intent.putExtra("model", carModel);
        intent.putExtra("email", userEmail);
        intent.putExtra("car_mileage", carMileage);
        startActivity(intent);
        startActivity(intent);
    }

    private int mapPictureKeyToDrawable(int key) {
        switch (key) {
            case 1:  return R.drawable.aceitemotor;
            case 2:  return R.drawable.aceitemotor;
            case 3:  return R.drawable.wheel_check;
            case 4:  return R.drawable.wiper_blades;
            case 5:  return R.drawable.filtrodeairemotor;
            case 6:  return R.drawable.anticongelante;
            case 7:  return R.drawable.aceite_transmision;
            case 8:  return R.drawable.liquido_direccion_asistida;
            case 9:  return R.drawable.wiperfluid;
            case 10: return R.drawable.correa_mangas;
            case 11: return R.drawable.bateria;
            case 12: return R.drawable.rotaciondegomas;
            case 13: return R.drawable.sistemadefrenos;
            case 14: return R.drawable.filtrodecabina;
            case 15: return R.drawable.sistemademuffle;
            case 16: return R.drawable.reemplazo_bujias;
            case 17: return R.drawable.reemplazo_bujias;
            case 18: return R.drawable.wheel_alignment;
            default: return R.drawable.generalmaintenance;
        }
    }
}