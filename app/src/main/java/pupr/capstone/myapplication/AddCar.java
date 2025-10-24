package pupr.capstone.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.*;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.*;
import java.sql.*;

public class AddCar extends AppCompatActivity {

    Button btnPickImage;
    FloatingActionButton btnBackMaintenance;
    ImageView CarImage;
    String userEmail;
    ActivityResultLauncher<Intent> resultLauncher;
    Spinner spinnerMarca, spinnerModelo, spinnerColor, spinnerYear;
    Map<String, List<String>> modelos = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_car);

        userEmail = getIntent().getStringExtra("email");

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Variables UI
        btnPickImage = findViewById(R.id.btnPickImageGallery);
        btnBackMaintenance = findViewById(R.id.btnBackMaintenance);
        CarImage = findViewById(R.id.imageViewGallery);
        CarImage.setImageResource(R.drawable.default_car_image);
        registerResult();

        btnPickImage.setOnClickListener(view -> pickImage());

        // Spinners
        spinnerMarca = findViewById(R.id.spinnerMarca);
        spinnerModelo = findViewById(R.id.spinnerModelo);
        spinnerColor = findViewById(R.id.spinnerColor);
        spinnerYear = findViewById(R.id.spinnerYear);

        // Listas base
        String[] marcas = {"Seleccione una marca", "Audi", "BMW", "Chrysler", "Dodge", "Ford", "Hyundai", "Infiniti", "Kia",
                "Lamborghini", "Lexus", "Mazda", "Mercedes-Benz", "Mitsubishi", "Nissan", "Tesla", "Toyota", "Volkswagen", "Volvo"};

        String[] color = {"Seleccione un color", "Amarillo", "Anaranjado", "Azul", "Beige", "Blanco", "Dorado",
                "Gris", "Marrón", "Negro", "Plata", "Rojo", "Verde", "Violeta", "Otro"};

        // Modelos por marca
        Map<String, String[]> modelosPorMarca = new HashMap<>();
        modelosPorMarca.put("Audi", new String[]{"A3 – A8", "Q3 – Q8", "R8", "TT", "Versiones S/RS", "Otro"});
        modelosPorMarca.put("BMW", new String[]{"M3", "M4", "M5", "M8", "Serie 2 – Serie 8", "X1 – X7", "Z4", "Otro"});
        modelosPorMarca.put("Chrysler", new String[]{"300", "Pacifica", "Voyager", "Otro"});
        modelosPorMarca.put("Dodge", new String[]{"Charger", "Challenger", "Durango", "Hornet", "Grand Caravan", "Ram 1500", "Viper", "Journey", "Dart", "Otro"});
        modelosPorMarca.put("Ford", new String[]{"Bronco", "Bronco Sport", "Edge", "Escape", "Expedition", "Explorer", "F150", "Maverick", "Mustang", "Ranger", "Transit", "Otro"});
        modelosPorMarca.put("Hyundai", new String[]{"Elantra", "Kona", "Palisade", "Santa Cruz", "Santa Fe", "Sonata", "Tucson", "Venue", "Otro"});
        modelosPorMarca.put("Infiniti", new String[]{"Q50", "Q60", "QX50", "QX55", "QX60", "QX80", "Otro"});
        modelosPorMarca.put("Kia", new String[]{"Carnival", "K4", "K5", "Niro", "Seltos", "Sorento", "Soul", "Sportage", "Stinger", "Telluride", "Otro"});
        modelosPorMarca.put("Lamborghini", new String[]{"Aventador", "Huracán", "Revuelto", "Urus", "Otro"});
        modelosPorMarca.put("Lexus", new String[]{"ES", "GX", "IS", "LC", "LM", "LS", "LX", "NX", "RX", "TX", "UX", "Otro"});
        modelosPorMarca.put("Mazda", new String[]{"CX30", "CX5", "CX50", "CX70", "CX90", "Mazda3", "MX5 Miata", "Otro"});
        modelosPorMarca.put("Mercedes-Benz", new String[]{"AMG", "Clase A–Clase S", "CLA", "CLS", "G-Class", "GLA–GLS", "Otro"});
        modelosPorMarca.put("Mitsubishi", new String[]{"Eclipse Cross", "Mirage", "Outlander", "Outlander Sport", "Otro"});
        modelosPorMarca.put("Nissan", new String[]{"Altima", "Frontier", "Kicks", "Maxima", "Pathfinder", "Rogue", "Sentra", "Titan", "Versa", "Z", "Otro"});
        modelosPorMarca.put("Tesla", new String[]{"Cybertruck", "Model 3", "Model S", "Model X", "Model Y", "Roadster", "Otro"});
        modelosPorMarca.put("Toyota", new String[]{"4Runner", "Camry", "Corolla", "GR Supra", "Highlander", "Tacoma", "Tundra", "RAV4", "Sienna", "Otro"});
        modelosPorMarca.put("Volkswagen", new String[]{"Arteon", "Atlas", "Golf", "Jetta", "Taos", "Tiguan", "Otro"});
        modelosPorMarca.put("Volvo", new String[]{"S60", "S90", "V60", "XC60", "XC90", "Otro"});

        // Adapter de marca
        ArrayAdapter<String> adapterMarca = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, marcas);
        adapterMarca.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMarca.setAdapter(adapterMarca);

        // Adapter de color
        ArrayAdapter<String> adapterColor = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, color);
        adapterColor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerColor.setAdapter(adapterColor);

        // Spinner de años dinámico
        List<String> years = new ArrayList<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int year = currentYear; year >= 1975; year--) {
            years.add(String.valueOf(year));
        }
        ArrayAdapter<String> adapterYear = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, years);
        adapterYear.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(adapterYear);

        // Inicialmente desactivar modelo y color
        spinnerModelo.setEnabled(false);
        spinnerColor.setEnabled(false);

        // Listener para marca
        spinnerMarca.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String marcaSeleccionada = parent.getItemAtPosition(position).toString();
                EditText editTextMilleage = findViewById(R.id.editTextMilleage);
                EditText editTextLicensePlate = findViewById(R.id.editTextLicensePlate);

                editTextLicensePlate.setFilters(new android.text.InputFilter[]{
                        new android.text.InputFilter.AllCaps(),
                        new android.text.InputFilter.LengthFilter(6)
                });

                if (!marcaSeleccionada.equals("Seleccione una marca")) {
                    spinnerModelo.setEnabled(true);
                    spinnerColor.setEnabled(true);
                    spinnerYear.setEnabled(true);
                    editTextMilleage.setEnabled(true);
                    editTextLicensePlate.setEnabled(true);

                    String[] modelos = modelosPorMarca.get(marcaSeleccionada);
                    ArrayAdapter<String> adapterModelo = new ArrayAdapter<>(AddCar.this,
                            android.R.layout.simple_spinner_item, modelos);
                    adapterModelo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerModelo.setAdapter(adapterModelo);
                } else {
                    spinnerModelo.setEnabled(false);
                    spinnerYear.setEnabled(false);
                    editTextMilleage.setEnabled(false);
                    editTextLicensePlate.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Botón regresar
        btnBackMaintenance.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });
    }

    private void pickImage() {
        Intent intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
        resultLauncher.launch(intent);
    }

    private void registerResult() {
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        try {
                            Uri imageUri = result.getData().getData();
                            CarImage.setImageURI(imageUri);
                        } catch (Exception e) {
                            Toast.makeText(AddCar.this, "No se ha podido cargar imagen", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    public void guardarVehiculo(View v) {
        EditText editTextMilleage = findViewById(R.id.editTextMilleage);
        EditText editTextLicensePlate = findViewById(R.id.editTextLicensePlate);
        Spinner spinnerMarca = findViewById(R.id.spinnerMarca);
        Spinner spinnerModelo = findViewById(R.id.spinnerModelo);
        Spinner spinnerColor = findViewById(R.id.spinnerColor);
        Spinner spinnerYear = findViewById(R.id.spinnerYear);
        ImageView imageView = findViewById(R.id.imageViewGallery);

        String year = spinnerYear.getSelectedItem().toString();
        String milleage = editTextMilleage.getText().toString();
        String licensePlate = editTextLicensePlate.getText().toString();
        String marca = spinnerMarca.getSelectedItem().toString();
        String modelo = spinnerModelo.getSelectedItem().toString();
        String color = spinnerColor.getSelectedItem().toString();

        if (year.isEmpty() || milleage.isEmpty() || licensePlate.isEmpty() ||
                marca.equals("Seleccione una marca") || modelo.isEmpty()) {
            Toast.makeText(this, "Todos los campos son requeridos.", Toast.LENGTH_SHORT).show();
            return;
        }

        imageView.setDrawingCacheEnabled(true);
        imageView.buildDrawingCache();
        android.graphics.Bitmap bitmap = imageView.getDrawingCache();
        java.io.ByteArrayOutputStream stream = new java.io.ByteArrayOutputStream();
        bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream);
        byte[] imagenBytes = stream.toByteArray();

        try {
            MyJDBC jdbc = new MyJDBC();
            Connection con = jdbc.obtenerConexion();

            if (con != null) {
                String query = "INSERT INTO VEHICLE (YEAR, MODEL, COLOR, MILEAGE, LICENSE_PLATE, IMAGE, EMAIL, BRAND) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement stmt = con.prepareStatement(query);
                stmt.setInt(1, Integer.parseInt(year));
                stmt.setString(2, modelo);
                stmt.setString(3, color);
                stmt.setInt(4, Integer.parseInt(milleage));
                stmt.setString(5, licensePlate);
                stmt.setBytes(6, imagenBytes);
                stmt.setString(7, userEmail);
                stmt.setString(8, marca);

                int rows = stmt.executeUpdate();

                if (rows > 0) {
                    Toast.makeText(this, "Vehículo guardado exitosamente", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(this, GarageActivity.class);
                    i.putExtra("email", userEmail);
                    startActivity(i);
                    finish();
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
}
