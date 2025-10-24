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

        userEmail = getIntent().getStringExtra("email");
        String nameExtra = getIntent().getStringExtra("name"); // puede ser null

        // 1) Intent → 2) Google account → 3) BD → 4) Desconocido
        resolveAndSetGarageName(userEmail, nameExtra);

        recyclerView = findViewById(R.id.recyclerViewAutos);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        autos = new ArrayList<>();

        cargarAutosDesdeBD(userEmail);

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
                intent.putExtra("car_mileage", autos.getMileage());
                startActivity(intent);
            }
            @Override public void onItemClick(Vehicle auto) {}
        });

        BottomNavRouter.setup(this, findViewById(R.id.bottomNav), R.id.nav_garaje, userEmail);
    }

    private void resolveAndSetGarageName(String email, String nameExtra) {
        TextView garage_owner = findViewById(R.id.txtGarageName);

        // 1️⃣ Nombre pasado desde MainActivity
        if (nameExtra != null && !nameExtra.isEmpty()) {
            garage_owner.setText("Garaje de " + safeFirstName(nameExtra));
            return;
        }

        // 2️⃣ Intentar obtenerlo de la BD
        new Thread(() -> {
            String dbName = null;
            try {
                MyJDBC myJDBC = new MyJDBC();
                try (Connection connection = myJDBC.obtenerConexion()) {
                    if (connection != null) {
                        String query = "SELECT `NAME` FROM `USER` WHERE `EMAIL` = ?";
                        try (PreparedStatement stmt = connection.prepareStatement(query)) {
                            stmt.setString(1, email);
                            try (ResultSet rs = stmt.executeQuery()) {
                                if (rs.next()) dbName = rs.getString("NAME");
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            final String finalName = safeFirstName(dbName);
            runOnUiThread(() -> {
                if (finalName != null && !finalName.isEmpty()) {
                    garage_owner.setText("Garaje de " + finalName);
                } else {
                    garage_owner.setText("Garaje de usuario desconocido");
                }
            });
        }).start();
    }


    private String safeFirstName(String fullName) {
        if (fullName == null) return null;
        fullName = fullName.trim();
        if (fullName.isEmpty()) return null;
        int space = fullName.indexOf(" ");
        return (space > 0) ? fullName.substring(0, space) : fullName;
    }


    private void cargarAutosDesdeBD(String email) {
        try {
            MyJDBC myJDBC = new MyJDBC();
            Connection connection = myJDBC.obtenerConexion();

            if (connection != null) {
                String query = "SELECT BRAND, MODEL, YEAR, LICENSE_PLATE, IMAGE, MILEAGE FROM VEHICLE WHERE EMAIL = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, email);
                ResultSet rs = statement.executeQuery();

                while (rs.next()) {
                    String marca = rs.getString("BRAND");
                    String model = rs.getString("MODEL");
                    int year = rs.getInt("YEAR");
                    String tablilla = rs.getString("LICENSE_PLATE");
                    // Combinar marca y modelo en "nombre"
                    //String nombre = marca + " " + model;//ojo por eso sale null en garageactvity title of layout

                    // Obtener imagen como BLOB
                    byte[] imagenBytes = rs.getBytes("IMAGE");
                    int mileage = rs.getInt("MILEAGE");
                    Bitmap bitmap = null;
                    if (imagenBytes != null && imagenBytes.length > 0) {
                        bitmap = BitmapFactory.decodeByteArray(imagenBytes, 0, imagenBytes.length);
                    }

                    // Agregar a la lista
                    autos.add(new Vehicle(marca,model, tablilla,year, bitmap, mileage));
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
                String query = "SELECT NAME FROM USER WHERE EMAIL = ?";
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





