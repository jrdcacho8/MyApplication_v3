package pupr.capstone.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;

import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GarageActivity extends AppCompatActivity {
    //Trabajr codigo para que utilice arreglo de objetpos en vez de base de dato
    RecyclerView recyclerView;

    List<Vehicle> autos;
    AutoAdapter adapter;
    String userEmail;
    private NotificationHelper notificationHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_garage);

        // Obtener el email del usuario autenticado

        userEmail = getIntent().getStringExtra("email");
        //obtener nombre de usuario  para identificar garaje

        try {
            setGarageName(userEmail);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


        // Change it here
        recyclerView = findViewById(R.id.recyclerViewAutos);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        autos = new ArrayList<>();

        // Cargar autos desde la base de datos según el email
        cargarAutosDesdeBD(userEmail);

        // Configurar adaptador
        adapter = new AutoAdapter(autos, userEmail);
        recyclerView.setAdapter(adapter);


        adapter.setOnItemClickListener(new AutoAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Vehicle autos, String userEmail) {

                Intent intent = new Intent(GarageActivity.this, MaintenanceActivity.class);
                intent.putExtra("marca", autos.getBrand());
                intent.putExtra("license_plate", autos.getLicense_plate());
                intent.putExtra("model", autos.getModel());
                intent.putExtra("email", userEmail);


                startActivity(intent);
            }

            @Override
            public void onItemClick(Vehicle auto) {

            }
        });
        BottomNavRouter.setup(this, findViewById(R.id.bottomNav), R.id.nav_garaje, userEmail);
    }

    private void cargarAutosDesdeBD(String email) {
        try {
            MyJDBC myJDBC = new MyJDBC();
            Connection connection = myJDBC.obtenerConexion();

            if (connection != null) {
                String query = "SELECT BRAND, MODEL, LICENSE_PLATE, IMAGEN FROM VEHICULO WHERE EMAIL = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, email);
                ResultSet rs = statement.executeQuery();

                while (rs.next()) {
                    String marca = rs.getString("BRAND");
                    String modelo = rs.getString("MODEL");
                    String tablilla = rs.getString("LICENSE_PLATE");

                    // Combinar marca y modelo en "nombre"
                    String nombre = marca + " " + modelo;

                    // Obtener imagen como BLOB
                    byte[] imagenBytes = rs.getBytes("IMAGEN");
                    Bitmap bitmap = null;
                    if (imagenBytes != null && imagenBytes.length > 0) {
                        bitmap = BitmapFactory.decodeByteArray(imagenBytes, 0, imagenBytes.length);
                    }

                    // Agregar a la lista
                    autos.add(new Vehicle(nombre, tablilla, bitmap));
                }


                statement.close();
                connection.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleCrearAuto(View v) {
        Intent i = new Intent(this, AddCar.class);
        i.putExtra("email", userEmail); // Pasar email al formulario de nuevo auto
        startActivity(i);
    }

    public void setGarageName(String email) throws SQLException {
        MyJDBC myJDBC1 = new MyJDBC();

        try (Connection connection = myJDBC1.obtenerConexion()) {
            if (connection != null) {
                String query = "SELECT NAME FROM USUARIO WHERE EMAIL = ?";
                try (PreparedStatement stmt = connection.prepareStatement(query)) {
                    stmt.setString(1, email);

                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            String garage_name = rs.getString("NAME");
                            String userName;

                            // 🔒 Evitar error si no hay espacios en el nombre
                            int firstSpaceIndex = garage_name.indexOf(" ");
                            if (firstSpaceIndex > 0) {
                                userName = garage_name.substring(0, firstSpaceIndex);
                            } else {
                                userName = garage_name; // usar el nombre completo
                            }

                            TextView garage_owner = findViewById(R.id.txtGarageName);
                            garage_owner.setText(String.format("Garaje de %s", userName));
                        } else {
                            // Si no se encuentra el usuario, mostrar algo por defecto
                            TextView garage_owner = findViewById(R.id.txtGarageName);
                            garage_owner.setText("Garaje de usuario desconocido");
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            TextView garage_owner = findViewById(R.id.txtGarageName);
            garage_owner.setText("Error al cargar el garaje");
        }
    }

}





