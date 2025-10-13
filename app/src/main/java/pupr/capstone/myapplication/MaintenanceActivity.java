
package pupr.capstone.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class MaintenanceActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    MaintenanceAdapter adapter;
    List<Maintenance> listaMaintenances = new ArrayList<>();
    private Vehicle car;

    //private String userEmail;

    private TextView textActivityMaintenance;

    FloatingActionButton buttonBackGarage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintenance);


        buttonBackGarage= findViewById(R.id.buttonBackGarage);
        textActivityMaintenance = findViewById(R.id.textActivityMaintenance);
        recyclerView = findViewById(R.id.recyclerViewMaintenance);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        // Capturar marca pasada desde GarageActivity
        String marca = getIntent().getStringExtra("marca");
        String license_plate = getIntent().getStringExtra("license_plate");
        String model = getIntent().getStringExtra("model");

        String email =getIntent().getStringExtra("email");

        //Set Title of activity_maintenance according to selected vehicle

        textActivityMaintenance.setText(String.join(" ", marca, license_plate));

        // Cargar mantenimientos de la BD usando la marca como nombre de tabla
        cargarMantenimientosDesdeBD(marca);

        car= new Vehicle(marca, license_plate,model);

        adapter = new MaintenanceAdapter(listaMaintenances,car, email);
        recyclerView.setAdapter(adapter);

       //Maintenance maintenance= new Maintenance(listaMaintenances.get(0));
        //Escrito por Esdras

        // Manejar el clic de cada item
        adapter.setOnItemClickListener(new MaintenanceAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Maintenance maintenance) {

            }

            @Override
            public void onItemClick(Maintenance maintenance, String userEmail) {

            }

            //MaintenanceAdapter.OnItemClickListener()
             @Override
            public void onItemClick(Maintenance listaMaintenances, Vehicle car, String email ){
                Intent intent = new Intent(MaintenanceActivity.this, MaintenanceTypeDetails.class);

                // Ajusta los nombres de getters según tu clase Maintenance
                intent.putExtra("type", listaMaintenances.getMaintenance());
                intent.putExtra("mileage", listaMaintenances.getMileageRate());
                intent.putExtra("mileage_other", listaMaintenances.getMileageRateOther());
                intent.putExtra("time", listaMaintenances.getTimeRate());
                intent.putExtra("note", listaMaintenances.getNote());
                intent.putExtra("picture", listaMaintenances.getImage());

                // Ajusta los nombres de getters según tu clase Vehicle
                intent.putExtra("marca",car.getBrand());
                intent.putExtra("license_plate",car.getLicense_plate());
                intent.putExtra("model", car.getModel());

                //User email
                intent.putExtra("email",email);

                startActivity(intent);
            }
        });

        //Escrito por Esdras

        // Botón regresar
        buttonBackGarage.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

    }
//Trabajar desde aqui los detalles del mantenimiento 09/29/2025
    private void cargarMantenimientosDesdeBD(String marca) {
        try {
            MyJDBC myJDBC = new MyJDBC();
            Connection connection = myJDBC.obtenerConexion();

            if (connection != null) {
                String query = "SELECT TYPE_OF_MAINTENANCE, MILEAGE_RATE,MILEAGE_RATE_OTHER, TIME_RATE, NOTE FROM " + marca;
                PreparedStatement statement = connection.prepareStatement(query);
                ResultSet rs = statement.executeQuery();

                while (rs.next()) {
                    String tipo = rs.getString("TYPE_OF_MAINTENANCE");
                    int mileage_rate= Integer.parseInt(rs.getString("MILEAGE_RATE"));
                    int mileage_rate_other = Integer.parseInt(rs.getString("MILEAGE_RATE_OTHER"));
                    int time_rate = Integer.parseInt(rs.getString("TIME_RATE"));
                    String note= rs.getString("NOTE");

                    int maintenance_picture;


                    switch (tipo) {
                        case "Aceite de Motor Convencional":
                            maintenance_picture = 1;

                            break;
                        case "Aceite de Motor Sintético":
                            maintenance_picture = 2;
                            break;
                        case "Presión y profundidad de los neumáticos":
                            maintenance_picture = 3;
                            break;
                        case "Cambio de escobillas del limpiaparabrisas":
                            maintenance_picture = 4;
                            break;
                        case "Filtro de aire (motor)":
                            maintenance_picture = 5;
                            break;
                        case "Líquido refrigerante/anticongelante":
                            maintenance_picture = 6;
                            break;
                        case "Líquido de la transmisión automática":
                            maintenance_picture = 7;
                            break;
                        case "Líquido de la dirección asistida (power steering)":
                            maintenance_picture = 8;
                            break;
                        case "Líquido limpiaparabrisas":
                            maintenance_picture = 9;
                            break;
                        case "Correas y mangas":
                            maintenance_picture = 10;
                            break;
                        case "Batería":
                            maintenance_picture = 11;
                            break;
                        case "Rotación de gomas":
                            maintenance_picture = 12;
                            break;
                        case "Frenos":
                            maintenance_picture = 13;
                            break;
                        case "Filtro de aire (cabina)":
                            maintenance_picture = 14;
                            break;
                        case "Sistema de escape":
                            maintenance_picture = 15;
                            break;
                        case "Bujías Convencionales":
                            maintenance_picture = 16;
                            break;
                        case "Bujías de Iridio":
                            maintenance_picture = 17;
                            break;
                        case "Alineación de gomas":
                            maintenance_picture = 18;
                            break;
                        default:
                            maintenance_picture = 19;
                            break;
                    }//poner setters

                    listaMaintenances.add(new Maintenance(tipo,mileage_rate,mileage_rate_other, time_rate, note, maintenance_picture));
                }

                statement.close();
                connection.close();
                adapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
    public void goBack(View v){
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}