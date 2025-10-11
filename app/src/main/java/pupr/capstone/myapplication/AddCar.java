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

public class AddCar extends AppCompatActivity {

    Button btnPickImage;
    FloatingActionButton btnBackMaintenance;
    ImageView CarImage;
    String userEmail;
    ActivityResultLauncher<Intent> resultLauncher;
    Spinner spinnerMarca, spinnerModelo, spinnerColor;
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
        //variables para escoger la foto del vehiculo
        btnPickImage = findViewById(R.id.btnPickImageGallery);
        btnBackMaintenance = findViewById(R.id.btnBackMaintenance);

        CarImage = findViewById(R.id.imageViewGallery);
        CarImage.setImageResource(R.drawable.default_car_image);
        registerResult();

        btnPickImage.setOnClickListener(view -> pickImage());

        //Variable del spinner para escojer la marca del Vehiculo
        Spinner spinnerMarca = findViewById(R.id.spinnerMarca);
        Spinner spinnerModelo = findViewById(R.id.spinnerModelo);
        Spinner spinnerColor= findViewById(R.id.spinnerColor);

// Lista de marcas
        String[] marcas = {"Seleccione una marca", "Audi", "BMW", "Chrysler", "Dodge", "Ford", "Hyundai", "Infiniti", "Kia",
                "Lamborghini", "Lexus", "Mazda", "Mercedes-Benz", "Mitsubishi", "Nissan", "Tesla", "Toyota", "Volkswagen", "Volvo"};
// Lista de color
        String[] color = {"Seleccione un color", "Amarillo", "Anaranjado", "Azul", "Beige", "Blanco", "Dorado", "Gris", "Marrón", "Negro", "Plata", "Rojo", "Verde", "Violeta"};
// Mapa de modelos según la marca
        Map<String, String[]> modelosPorMarca = new HashMap<>();

        modelosPorMarca.put("Acura", new String[]{"ILX", "Integra", "MDX", "NSX", "RDX", "RLX", "TLX"});
        modelosPorMarca.put("Audi", new String[]{"A3 – A8", "Q3 – Q8", "R8", "TT", "Versiones S / RS"});
        modelosPorMarca.put("BMW", new String[]{"M3", "M4", "M5", "M8", "Serie 2 – Serie 8", "X1 – X7", "Z4"});
        modelosPorMarca.put("Chrysler", new String[]{"300", "Pacifica", "Voyager"});
        modelosPorMarca.put("Ford", new String[]{"Bronco", "Bronco Sport", "Edge", "Escape", "Expedition", "Explorer", "F-150", "Maverick", "Mustang", "Ranger", "Transit"});
        modelosPorMarca.put("Honda", new String[]{"Accord", "Civic", "Clarity", "CR-V", "HR-V", "Insight", "Passport", "Pilot", "Ridgeline"});
        modelosPorMarca.put("Hyundai", new String[]{"Elantra", "Kona", "Palisade", "Santa Cruz", "Santa Fe", "Sonata", "Tucson", "Venue"});
        modelosPorMarca.put("Infiniti", new String[]{"Q50", "Q60", "QX50", "QX55", "QX60", "QX80"});
        modelosPorMarca.put("Jeep", new String[]{"Cherokee", "Compass", "Gladiator", "Grand Cherokee", "Grand Cherokee L", "Grand Wagoneer", "Renegade", "Wagoneer", "Wrangler"});
        modelosPorMarca.put("Kia", new String[]{"Carnival", "K4", "K5", "Niro", "Seltos", "Sorento", "Soul", "Sportage", "Stinger", "Telluride"});
        modelosPorMarca.put("Lamborghini", new String[]{"Aventador", "Huracán", "Huracán Sterrato", "Revuelto", "Urus"});
        modelosPorMarca.put("Lexus", new String[]{"ES", "GX", "IS", "LBX", "LC", "LM", "LS", "LX", "NX", "RX", "TX", "UX"});
        modelosPorMarca.put("Mazda", new String[]{"CX-30", "CX-5", "CX-50", "CX-70", "CX-90", "Mazda3", "MX-5 Miata"});
        modelosPorMarca.put("Mercedes-Benz", new String[]{"AMG", "Clase A – Clase S", "CLA", "CLS", "G-Class", "GLA – GLS"});
        modelosPorMarca.put("Mini", new String[]{"Convertible", "Countryman", "John Cooper Works", "Mini Hatch"});
        modelosPorMarca.put("Mitsubishi", new String[]{"Eclipse Cross", "Mirage", "Montero", "Outlander", "Outlander Sport"});
        modelosPorMarca.put("Nissan", new String[]{"Altima", "Frontier", "Kicks", "Maxima", "Murano", "Pathfinder", "Rogue", "Sentra", "Titan", "Versa", "Z"});
        modelosPorMarca.put("Porsche", new String[]{"718 Cayman / Boxster", "911", "Cayenne", "Macan", "Panamera"});
        modelosPorMarca.put("RAM", new String[]{"ProMaster", "RAM 1500", "RAM 2500", "RAM 3500"});
        modelosPorMarca.put("Tesla", new String[]{"Cybertruck", "Model 3", "Model S", "Model X", "Model Y", "Roadster"});
        modelosPorMarca.put("Toyota", new String[]{"4Runner", "C-HR", "Camry", "Corolla", "Crown", "Grand Highlander", "GR Corolla", "GR Supra", "GR86", "Highlander", "Land Cruiser", "Mirai", "Prius", "RAV4", "Sequoia", "Sienna", "Tacoma", "Tundra", "Urban Cruiser"});
        modelosPorMarca.put("Volkswagen", new String[]{"Arteon", "Atlas", "Golf", "Jetta", "Taos", "Tiguan"});
        modelosPorMarca.put("Volvo", new String[]{"S60", "S90", "V60", "XC60", "XC90"});

// Adapter de marcas
        ArrayAdapter<String> adapterMarca = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, marcas);
        adapterMarca.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMarca.setAdapter(adapterMarca);
// Adapter de color
        ArrayAdapter<String> adapterColor = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, color);
        adapterColor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerColor.setAdapter(adapterColor);
// Inicialmente desactivar modelo
        spinnerModelo.setEnabled(false);
        spinnerColor.setEnabled(false);

// Listener para marca
        spinnerMarca.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String marcaSeleccionada = parent.getItemAtPosition(position).toString();
                EditText editTextYear = findViewById(R.id.editTextYear);
                EditText editTextMilleage = findViewById(R.id.editTextMilleage);
                EditText editTextLicensePlate = findViewById(R.id.editTextLicensePlate);
                if (!marcaSeleccionada.equals("Seleccione una marca")) {
                    // Activar modelo
                    spinnerModelo.setEnabled(true);
                    spinnerColor.setEnabled(true);
                    editTextYear.setEnabled(true);
                    editTextMilleage.setEnabled(true);
                    editTextLicensePlate.setEnabled(true);


                    // Obtener modelos según la marca
                    String[] modelos = modelosPorMarca.get(marcaSeleccionada);

                    // Asignar modelos al Spinner
                    ArrayAdapter<String> adapterModelo = new ArrayAdapter<>(AddCar.this,
                            android.R.layout.simple_spinner_item, modelos);
                    adapterModelo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerModelo.setAdapter(adapterModelo);
                } else {
                    // Vuelve a desactivar si vuelve a seleccionar default
                    spinnerModelo.setEnabled(false);
                    editTextYear.setEnabled(false);
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

    private void pickImage(){
        Intent intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
        resultLauncher.launch(intent);
    }

    private void registerResult(){
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        try{
                            Uri imageUri = result.getData().getData();
                            CarImage.setImageURI(imageUri);
                        }
                        catch (Exception e){
                            Toast.makeText(AddCar.this, "No se ha podido cargar imagen", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }
    public void guardarVehiculo(View v){
        EditText editTextYear = findViewById(R.id.editTextYear);
        EditText editTextMilleage = findViewById(R.id.editTextMilleage);
        EditText editTextLicensePlate = findViewById(R.id.editTextLicensePlate);
        Spinner spinnerMarca = findViewById(R.id.spinnerMarca);
        Spinner spinnerModelo = findViewById(R.id.spinnerModelo);
        Spinner spinnerColor = findViewById(R.id.spinnerColor);
        ImageView imageView = findViewById(R.id.imageViewGallery);

        String year = editTextYear.getText().toString();
        String milleage = editTextMilleage.getText().toString();
        String licensePlate = editTextLicensePlate.getText().toString();
        String marca = spinnerMarca.getSelectedItem().toString();
        String modelo = spinnerModelo.getSelectedItem().toString();
        String color = spinnerColor.getSelectedItem().toString();

        // Validaciones mínimas
        if (year.isEmpty() || milleage.isEmpty() || licensePlate.isEmpty() ||
                marca.equals("Seleccione una marca") || modelo.isEmpty()) {
            Toast.makeText(this, "Todos los campos son requeridos.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convertir la imagen a bytes

        imageView.setDrawingCacheEnabled(true);
        imageView.buildDrawingCache();
        android.graphics.Bitmap bitmap = imageView.getDrawingCache();
        java.io.ByteArrayOutputStream stream = new java.io.ByteArrayOutputStream();
        bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream);
        byte[] imagenBytes = stream.toByteArray();

        // Conexión e inserción
        try {
            MyJDBC jdbc = new MyJDBC();
            java.sql.Connection con = jdbc.obtenerConexion();

            if (con != null) {
                String query = "INSERT INTO VEHICULO (YEAR, MODEL, COLOR, MILLEAGE, LICENSE_PLATE, IMAGEN, EMAIL, BRAND) VALUES (?,?, ?, ?, ?, ?, ?, ?)";
                java.sql.PreparedStatement stmt = con.prepareStatement(query);
                stmt.setInt(1, Integer.parseInt(year));
                stmt.setString(2, modelo);
                stmt.setString(3, color);
                stmt.setInt(4, Integer.parseInt(milleage));
                stmt.setString(5, licensePlate);
                stmt.setBytes(6, imagenBytes);
                stmt.setString(7, userEmail);  // Pasado por Intent
                stmt.setString(8, marca);

                int rows = stmt.executeUpdate();

                if (rows > 0) {
                    Toast.makeText(this, "Vehículo guardado exitosamente", Toast.LENGTH_SHORT).show();
                    finish(); // O redirigir a otra actividad
                    Intent i = new Intent(this, GarageActivity.class);
                    i.putExtra("email", userEmail);
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

    public void goBack(View v){
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        //Intent i = new Intent(this, GarageActivity.class);
        //i.putExtra("email", userEmail);
        //startActivity(i);
    }





}