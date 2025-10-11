package pupr.capstone.myapplication;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.widget.*;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Locale;

public class MaintenanceTypeDetails extends AppCompatActivity {
    private ImageView imageMaintenance;
    private TextView txtTitle;           // "Mantenimientos"
    private TextView maintenanceName;    // "Tipo de mantenimiento"
    private TextView txtNote;            // "Nota Educativa del Mantenimiento"
    private EditText txtLastDateMaintenance, txtNextMaintenance;
    private Button btnUpdate;
    private FloatingActionButton backButton, editButton;

    private LocalDate last_maintenance_date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maintenance_screen); // usa el nombre real de TU XML

        // --- bind views ---
        imageMaintenance       = findViewById(R.id.imageMaintenance);
        txtTitle               = findViewById(R.id.txtTitle);
        txtNote                = findViewById(R.id.txtNote);
        txtLastDateMaintenance = findViewById(R.id.txtLastDateMaintenance);
        txtNextMaintenance     = findViewById(R.id.txtNextMaintenance);
        btnUpdate              = findViewById(R.id.btnUpdateMaintenance);
        backButton             = findViewById(R.id.backButton);
        editButton             = findViewById(R.id.editButton);

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



        // --- pinta datos ---
        txtTitle.setText(maintenance_type != null ? maintenance_type : car_brand +"\n"+car_model+"\n"+car_license_plate);
        txtNote.setText((note != null && !note.isEmpty()) ? note : "—");


        // si en el intent ya mandaras un resourceId, podrías usarlo directo.
        // como envías una "key", vuelve a mapearla aquí (idéntico a tu Adapter):
        int resId = mapPictureKeyToDrawable(pictureKey);
        imageMaintenance.setImageResource(resId);

        // DatePickers para fechas (mucho mejor que EditText con inputType)
        txtLastDateMaintenance.setOnClickListener(v -> last_maintenance_date=showDatePicker(txtLastDateMaintenance));
       // txtNextMaintenance.setOnClickListener(v -> showDatePicker(txtNextMaintenance));

        // Botón regresar
        backButton.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        // Botón editar (haz lo que necesites: habilitar campos, abrir otro layout, etc.)
        editButton.setOnClickListener(v -> {
            txtNote.setEnabled(true);
            txtLastDateMaintenance.setEnabled(true);
            txtNextMaintenance.setEnabled(true);
            // podrías mostrar un pequeño Toast o cambiar ícono a "guardar"
        });

        // Actualizar (ejemplo de validación simple)
        btnUpdate.setOnClickListener(v -> {
            String lastDate = txtLastDateMaintenance.getText().toString().trim();
            DateTimeFormatter output = DateTimeFormatter.ofPattern("MMM-dd-yyyy", Locale.US);
            // TODO: validar formato y enviar a tu BD si aplica
            LocalDate nextMaintenanceDate=last_maintenance_date.plusDays(daysToAdd);

            String formattedDate = nextMaintenanceDate.format(output);
            txtNextMaintenance.setText(formattedDate);

            uploadAlertToDB(nextMaintenanceDate, maintenance_type, userEmail, car_license_plate);

            // Por ahora solo cierra o muestra un toast
            Toast.makeText(this, "Actualizado", Toast.LENGTH_SHORT).show();
        });
    }

    private LocalDate showDatePicker(EditText target) {
        // 1. Get the current date to initialize the DatePickerDialog
        Calendar c = Calendar.getInstance();
        int y = c.get(Calendar.YEAR);
        int m = c.get(Calendar.MONTH); // Calendar.MONTH is 0-based (0 for January)
        int d = c.get(Calendar.DAY_OF_MONTH);

        // 2. Create and show the DatePickerDialog
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            // 'month' is 0-based, so add 1 for the correct month number
            // and create a Calendar instance for easy formatting.
            Calendar selectedDate = Calendar.getInstance();
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
                    Toast.makeText(this, "Recordatorio de "+ maintenance_type , Toast.LENGTH_LONG).show();
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


