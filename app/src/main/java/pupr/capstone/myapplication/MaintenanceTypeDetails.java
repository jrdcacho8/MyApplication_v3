package pupr.capstone.myapplication;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Locale;

public class MaintenanceTypeDetails extends AppCompatActivity {
    private ImageView imageMaintenance;

    private TextView maintenanceName;
    private TextView txtNote,lblLastMaintenance,txtTitle;            // "Nota Educativa del Mantenimiento",Tipo de mantenimiento",Mantenimientos"
    private EditText txtLastDateMaintenance, txtNextMaintenance;
    private Button btnUpdate;
    private FloatingActionButton backButton, editButton;
    private NotificationHelper notificationHelper;

    public boolean edit_token= false;

    private LocalDate last_maintenance_date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maintenance_screen); // usa el nombre real de TU XML
        //checkAlertDTB
        // --- bind views ---
        imageMaintenance       = findViewById(R.id.imageMaintenance);
        txtTitle               = findViewById(R.id.txtTitle);
        txtNote                = findViewById(R.id.txtNote);
        txtLastDateMaintenance = findViewById(R.id.txtLastDateMaintenance);
        txtNextMaintenance     = findViewById(R.id.txtNextMaintenance);
        btnUpdate              = findViewById(R.id.btnUpdateMaintenance);
        backButton             = findViewById(R.id.backButton);
        editButton             = findViewById(R.id.editButton);
        lblLastMaintenance      =findViewById(R.id.lblLastMaintenance);

        //to signal de edit of alert
        notificationHelper = new NotificationHelper(this);

        // Create the notification channel
        notificationHelper.createNotificationChannel();
        requestNotificationPermission();

        // --- recibe extras del intent ---
        Intent intent = getIntent();
        String maintenance_type = intent.getStringExtra("type");
        int mileage             = intent.getIntExtra("mileage", 0);
        int mileageOther        = intent.getIntExtra("mileage_other", 0);
        int daysToAdd           = intent.getIntExtra("time", 0);
        String note             = intent.getStringExtra("note");
        int pictureKey          = intent.getIntExtra("picture", 0);

        String car_brand        =intent.getStringExtra("marca");
        String car_license_plate=intent.getStringExtra("license_plate");
        String car_model        =intent.getStringExtra("model");

        String userEmail        =intent.getStringExtra("email");

        //recibe intent vehicle
        // --- 🔑 New Code: Check Database for Existing Alert Date 🔑 ---
        LocalDate existingAlertDate = null;
        if (maintenance_type != null && car_license_plate != null && userEmail != null) {
            try {

                // Call the new method to check the database
                existingAlertDate = getExistingAlertDate(userEmail, car_license_plate, maintenance_type);
            } catch (Exception e) {
                // Handle JDBC instantiation error if necessary
                e.printStackTrace();
            }
        }


        if (existingAlertDate != null) {
            // Use the same formatter you plan to use for display
            DateTimeFormatter output = DateTimeFormatter.ofPattern("MMM-dd-yyyy", Locale.US);
            String formattedDate = existingAlertDate.format(output);

            txtNextMaintenance.setText(formattedDate);

            // OPTIONAL: Disable the update button if the alert is already set
            lblLastMaintenance.setVisibility(View.GONE);
            txtLastDateMaintenance.setVisibility(View.GONE);
            btnUpdate.setEnabled(false);
            btnUpdate.setText("Alerta Establecida");
            Toast.makeText(this, "Alerta ya está establecida para: " + formattedDate, Toast.LENGTH_LONG).show();
        } else {
            // Ensure the button is enabled if no alert exists
            btnUpdate.setEnabled(true);
            btnUpdate.setText("Actualizar");
        }

        // ... (Rest of your onCreate methods) ...




        // --- pinta datos ---
        txtTitle.setText(maintenance_type != null ? maintenance_type : car_brand +"\n"+car_model+"\n"+car_license_plate);
        txtNote.setText((note != null && !note.isEmpty()) ? note : "—");


        // si en el intent ya mandaras un resourceId, podrías usarlo directo.
        // como envías una "key", vuelve a mapearla aquí (idéntico a tu Adapter):
        int resId = mapPictureKeyToDrawable(pictureKey);
        imageMaintenance.setImageResource(resId);

        // DatePickers para fechas (mucho mejor que EditText con inputType)
        txtLastDateMaintenance.setOnClickListener(v -> last_maintenance_date=showDatePicker(txtLastDateMaintenance));


        // Botón regresar
        backButton.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        // Botón editar (haz lo que necesites: habilitar campos, abrir otro layout, etc.)
        editButton.setOnClickListener(v -> {

            String nextDate = txtNextMaintenance.getText().toString().trim();
            DateTimeFormatter output = DateTimeFormatter.ofPattern("MMM-dd-yyyy", Locale.US);
            LocalDate mantLastDate = LocalDate.parse(nextDate,output);

            String formattedDate = mantLastDate.format(output);
            txtNextMaintenance.setText(formattedDate);
            edit_token= true;



            txtLastDateMaintenance.setEnabled(true);
            txtNextMaintenance.setEnabled(true);
            lblLastMaintenance.setVisibility(View.VISIBLE);
            txtLastDateMaintenance.setVisibility(View.VISIBLE);
            btnUpdate.setEnabled(true);
            btnUpdate.setText("Actualizar");





        });

        // Actualizar (ejemplo de validación simple)
        btnUpdate.setOnClickListener(v -> {

            String lastDate = txtLastDateMaintenance.getText().toString().trim();
            DateTimeFormatter output = DateTimeFormatter.ofPattern("MMM-dd-yyyy", Locale.US);
            LocalDate mantLastDate = LocalDate.parse(lastDate,output);

            LocalDate nextMaintenanceDate = mantLastDate.plusDays(daysToAdd);//OJO:aqui esta la data necesaria para la alerta de emails y notificaciones

            String formattedDate = nextMaintenanceDate.format(output);
            txtNextMaintenance.setText(formattedDate);


            if (edit_token== false){
                uploadAlertToDB(nextMaintenanceDate, maintenance_type, userEmail, car_license_plate);
                // Por ahora solo cierra o muestra un toast
                Toast.makeText(this, "Actualizado", Toast.LENGTH_SHORT).show();}
            else if (edit_token= true){
                editAlertToDB(nextMaintenanceDate, maintenance_type, userEmail, car_license_plate);
            }
            // Programar notificación
            scheduleTestNotification(car_brand, car_model, car_license_plate, maintenance_type);

            // Construir asunto y cuerpo con tu formato
            String subject = "Alerta de Mantenimiento";

            // Asegúrate de tener disponibles: brand, model, car_license_plate, maintenance_type
            String body =
                    "Saludo,\n\n" +
                            "El Vehiculo " + car_brand + " Tablilla " + car_license_plate + " necesita " + maintenance_type + " el cual vence hoy.";

            // Programar el correo a las 7:00 AM del nextMaintenanceDate
            NotificationHelper.scheduleMaintenanceEmailAt7am(
                    this,
                    nextMaintenanceDate,
                    userEmail,     // o varios: "a@x.com,b@y.com"
                    subject,
                    body
            );

            Toast.makeText(this,
                    "Actualizado y correo programado para las 7:00 AM del " + formattedDate,
                    Toast.LENGTH_LONG
            ).show();
        });
    }

    private void scheduleTestNotification(String brand, String model,
                                          String plate, String maintenanceType) {
        try {
            // Texto IDENTICO al correo
            String title = "Alerta de Mantenimiento";
            String message =
                    "Saludo,\n\n" +
                            "El Vehiculo " + brand + " " + model +
                            " Tablilla " + plate +
                            " necesita " + maintenanceType +
                            " el cual vence hoy.";

            // Dispara en 10s para probar
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.SECOND, 10);
            long time = calendar.getTimeInMillis();

            notificationHelper.scheduleNotification(time, title, message);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al programar la notificación", Toast.LENGTH_SHORT).show();
        }
    }
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
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
    private LocalDate showDatePicker(EditText target) {
        // 1. Get the current date to initialize the DatePickerDialog
        Calendar selectedDate = Calendar.getInstance();
        int y = selectedDate.get(Calendar.YEAR);
        int m = selectedDate.get(Calendar.MONTH); // Calendar.MONTH is 0-based (0 for January)
        int d = selectedDate.get(Calendar.DAY_OF_MONTH);

        // 2. Create and show the DatePickerDialog
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            // 'month' is 0-based, so add 1 for the correct month number
            // and create a Calendar instance for easy formatting.
            //Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(year, month, dayOfMonth);

            // 3. Format the date as "MMM-DD-YYYY"
            // Use SimpleDateFormat for correct formatting, including month name (MMM).
            // Locale.US is often used to ensure month names are in English (MMM).
            SimpleDateFormat sdf = new SimpleDateFormat("MMM-dd-yyyy", Locale.US);
            String formattedDate = sdf.format(selectedDate.getTime());

            // 4. Set the formatted date to the target EditText
            target.setText(formattedDate);

        }, y, m, d).show(); // Pass Y, M, D to initialize the dialog to today's date

        // The return value here is likely incorrect if you intend to return the *selected* date.
        // However, based on the original code, this returns the *initial* date.
        // If you're using API 26+, you can return a LocalDate object.
        return LocalDate.of(y, m + 1, d); // Return the initial date (M must be 1-based for LocalDate)
    }

    private void uploadAlertToDB(LocalDate alert_date, String maintenance_type, String email, String license_plate){
        try {
            MyJDBC jdbc = new MyJDBC();
            java.sql.Connection con = jdbc.obtenerConexion();

            if (con != null) {
                String query = "INSERT INTO ALERT (EMAIL,LICENSE_PLATE, NAME_ALERT, ALERT_DATE) VALUES (?,?, ?, ?)";
                java.sql.PreparedStatement stmt = con.prepareStatement(query);
                stmt.setString(1, email);
                stmt.setString(2, license_plate);
                stmt.setString(3, maintenance_type);
                stmt.setString(4, alert_date.toString());

                int rows = stmt.executeUpdate();

                if (rows > 0) {
                    Toast.makeText(this, "Recordatorio de "+ maintenance_type+ " activado" , Toast.LENGTH_LONG).show();
                    finish(); // O redirigir a otra actividad
                    Intent i = new Intent(this, GarageActivity.class);
                    i.putExtra("email", email);
                    startActivity(i);
                } else {
                    Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show();
                }

                con.close();
            } else {
                Toast.makeText(this, "No se pudo conectar con la base de datos", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }



    }
    // --- Inside MyJDBC.java ---
    private void editAlertToDB(LocalDate alert_date, String maintenance_type, String email, String license_plate){
        try {
            MyJDBC jdbc = new MyJDBC();
            java.sql.Connection con = jdbc.obtenerConexion();

            if (con != null) {
                String query = "UPDATE ALERT SET ALERT_DATE = ? WHERE NAME_ALERT = ? AND EMAIL = ? AND LICENSE_PLATE = ?";
                java.sql.PreparedStatement stmt = con.prepareStatement(query);
                stmt.setString(1, alert_date.toString());
                stmt.setString(2, maintenance_type);
                stmt.setString(3, email);
                stmt.setString(4, license_plate);

                int rows = stmt.executeUpdate();

                if (rows > 0) {
                    Toast.makeText(this, "Recordatorio de "+ maintenance_type+ " editado" , Toast.LENGTH_LONG).show();
                    finish(); // O redirigir a otra actividad
                    Intent i = new Intent(this, GarageActivity.class);
                    i.putExtra("email", email);
                    startActivity(i);
                } else {
                    Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show();
                }

                con.close();
            } else {
                Toast.makeText(this, "No se pudo conectar con la base de datos", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }



    }


    public LocalDate getExistingAlertDate(String email, String licensePlate, String maintenanceType) {
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
                    // Assuming ALERT_DATE is stored as a standard SQL DATE or VARCHAR
                    String dateString = rs.getString("ALERT_DATE");
                    // Convert the String from the DB back into a LocalDate
                    existingDate = LocalDate.parse(dateString);
                }
            }
        } catch (Exception e) {
            // Log the error but return null so the app doesn't crash
            e.printStackTrace();
        }
        return existingDate; // Returns the date or null if no alert exists
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


