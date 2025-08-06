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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.util.*;

public class AddCar extends AppCompatActivity {

    Button btnPickImage;
    ImageView imageView;
    ActivityResultLauncher<Intent> resultLauncher;
    Spinner spinnerMarca, spinnerModelo;
    Map<String, List<String>> modelos = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_car);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;


        });
        //variables para escoger la foto del vehiculo
        btnPickImage = findViewById(R.id.btnPickImageGallery);
        imageView= findViewById(R.id.imageViewGallery);
        registerResult();

        btnPickImage.setOnClickListener(view -> pickImage());

        //Variable del spinner para escojer la marca del Vehiculo
        Spinner spinnerMarca = findViewById(R.id.spinnerMarca);
        Spinner spinnerModelo = findViewById(R.id.spinnerModelo);

// Lista de marcas
        String[] marcas = {"Seleccione una marca", "Toyota", "Hyundai", "Kia", "Nissan", "Mitsubishi",
                "Ford", "Jeep", "RAM", "Honda", "Lamborghini", "Mazda", "BMW", "Audi",
                "Tesla", "Chrysler", "Lexus", "Volkswagen", "Mercedes-Benz", "Acura", "Volvo", "Porsche", "Mini", "Infiniti"};

// Mapa de modelos según la marca
        Map<String, String[]> modelosPorMarca = new HashMap<>();

        modelosPorMarca.put("Toyota", new String[]{"Camry", "Corolla", "RAV4", "Highlander", "Grand Highlander", "4Runner", "Sequoia", "Land Cruiser", "Tacoma", "Tundra", "Sienna", "Prius", "Mirai", "GR86", "GR Corolla", "GR Supra", "Crown", "C-HR", "Urban Cruiser"});
        modelosPorMarca.put("Hyundai", new String[]{"Venue", "Kona", "Tucson", "Santa Fe", "Palisade", "Sonata", "Elantra", "Santa Cruz"});
        modelosPorMarca.put("Kia", new String[]{"Soul", "Seltos", "Sportage", "Sorento", "Telluride", "Carnival", "K5", "K4", "Stinger", "Niro"});
        modelosPorMarca.put("Nissan", new String[]{"Versa", "Sentra", "Kicks", "Rogue", "Murano", "Pathfinder", "Frontier", "Titan", "Altima", "Maxima", "Z"});
        modelosPorMarca.put("Mitsubishi", new String[]{"Mirage", "Outlander", "Eclipse Cross", "Outlander Sport", "Montero"});
        modelosPorMarca.put("Ford", new String[]{"Maverick", "Ranger", "F-150", "Escape", "Edge", "Explorer", "Expedition", "Mustang", "Bronco", "Bronco Sport", "Transit"});
        modelosPorMarca.put("Jeep", new String[]{"Wrangler", "Gladiator", "Grand Cherokee", "Grand Cherokee L", "Cherokee", "Compass", "Renegade", "Wagoneer", "Grand Wagoneer"});
        modelosPorMarca.put("RAM", new String[]{"RAM 1500", "RAM 2500", "RAM 3500", "ProMaster"});
        modelosPorMarca.put("Honda", new String[]{"Civic", "Accord", "Insight", "HR-V", "CR-V", "Passport", "Pilot", "Ridgeline", "Clarity"});
        modelosPorMarca.put("Lamborghini", new String[]{"Huracán", "Aventador", "Urus", "Revuelto", "Huracán Sterrato"});
        modelosPorMarca.put("Mazda", new String[]{"Mazda3", "CX-30", "CX-5", "CX-50", "CX-70", "CX-90", "MX-5 Miata"});
        modelosPorMarca.put("BMW", new String[]{"Serie 2 – Serie 8", "X1 – X7", "Z4", "M3", "M4", "M5", "M8"});
        modelosPorMarca.put("Audi", new String[]{"A3 – A8", "Q3 – Q8", "TT", "R8", "Versiones S / RS"});
        modelosPorMarca.put("Tesla", new String[]{"Model S", "Model 3", "Model X", "Model Y", "Cybertruck", "Roadster"});
        modelosPorMarca.put("Chrysler", new String[]{"Pacifica", "Voyager", "300"});
        modelosPorMarca.put("Lexus", new String[]{"IS", "ES", "LS", "LC", "UX", "NX", "RX", "GX", "LX", "TX", "LM", "LBX"});
        modelosPorMarca.put("Volkswagen", new String[]{"Jetta", "Golf", "Tiguan", "Taos", "Atlas", "Arteon"});
        modelosPorMarca.put("Mercedes-Benz", new String[]{"Clase A – Clase S", "CLA", "CLS", "GLA – GLS", "G-Class", "AMG"});
        modelosPorMarca.put("Acura", new String[]{"ILX", "TLX", "RLX", "Integra", "RDX", "MDX", "NSX"});
        modelosPorMarca.put("Volvo", new String[]{"S60", "S90", "V60", "XC60", "XC90"});
        modelosPorMarca.put("Porsche", new String[]{"911", "718 Cayman / Boxster", "Panamera", "Cayenne", "Macan"});
        modelosPorMarca.put("Mini", new String[]{"Mini Hatch", "Convertible", "Countryman", "John Cooper Works"});
        modelosPorMarca.put("Infiniti", new String[]{"Q50", "Q60", "QX50", "QX55", "QX60", "QX80"});


// Adapter de marcas
        ArrayAdapter<String> adapterMarca = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, marcas);
        adapterMarca.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMarca.setAdapter(adapterMarca);

// Inicialmente desactivar modelo
        spinnerModelo.setEnabled(false);

// Listener para marca
        spinnerMarca.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String marcaSeleccionada = parent.getItemAtPosition(position).toString();

                if (!marcaSeleccionada.equals("Seleccione una marca")) {
                    // Activar modelo
                    spinnerModelo.setEnabled(true);

                    // Obtener modelos según la marca
                    String[] modelos = modelosPorMarca.get(marcaSeleccionada);

                    // Asignar modelos al Spinner
                    ArrayAdapter<String> adapterModelo = new ArrayAdapter<>(AddCar.this,
                            android.R.layout.simple_spinner_item, modelos);
                    adapterModelo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerModelo.setAdapter(adapterModelo);
                } else {
                    spinnerModelo.setEnabled(false); // Vuelve a desactivar si vuelve a seleccionar default
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
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
                            imageView.setImageURI(imageUri);
                        }
                        catch (Exception e){
                            Toast.makeText(AddCar.this, "No se ha podido cargar imagen", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }
    public void goBack(View v){
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }





}