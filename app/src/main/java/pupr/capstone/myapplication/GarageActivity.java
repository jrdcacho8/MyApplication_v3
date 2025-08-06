package pupr.capstone.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class GarageActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    List<Auto> autos;
    AutoAdapter adapter;
    String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_garage);

        // 1. Recibir email del usuario logueado
        userEmail = getIntent().getStringExtra("email");

        recyclerView = findViewById(R.id.recyclerViewAutos);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        autos = new ArrayList<>();

        // 2. Cargar autos desde base de datos
        cargarAutosDesdeBD(userEmail);

        // 3. Configurar adaptador
        adapter = new AutoAdapter(autos);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new AutoAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Auto auto) {
                Intent intent = new Intent(GarageActivity.this, DetalleAuto.class);
                intent.putExtra("nombre", auto.getNombre());
                intent.putExtra("tablilla", auto.getTablilla());
                intent.putExtra("imagen", auto.getImagenResId()); // Si estás usando BLOB, aquí deberías pasar el bitmap
                startActivity(intent);
            }
        });
    }

    private void cargarAutosDesdeBD(String email) {
        try {
            MyJDBC myJDBC = new MyJDBC();
            Connection connection = myJDBC.obtenerConexion();

            if (connection != null) {
                String query = "SELECT BRAND, MODEL, LICENSE_PLATE, IMAGEN FROM AUTO WHERE USUARIO_EMAIL = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, email);
                ResultSet rs = statement.executeQuery();

                while (rs.next()) {
                    String marca = rs.getString("BRAND");
                    String modelo = rs.getString("MODEL");
                    String tablilla = rs.getString("LICENSE_PLATE");

                    // Combinar marca y modelo para formar el "nombre"
                    String nombre = marca + " " + modelo;

                    // Imagen (opcional: aquí puede ir BLOB o imagen local)
                    byte[] imagenBytes = rs.getBytes("IMAGEN");
                    Bitmap bitmap = null;
                    if (imagenBytes != null) {
                        bitmap = BitmapFactory.decodeByteArray(imagenBytes, 0, imagenBytes.length);
                    }

                    // Si usas un constructor con ID de imagen (local):
                    autos.add(new Auto(nombre, tablilla, R.drawable.default_car_image));

                    // Si usas un constructor con Bitmap (requiere modificar tu clase Auto y Adapter):
                    // autos.add(new Auto(nombre, tablilla, bitmap));
                }

                rs.close();
                statement.close();
                connection.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void handleCrearAuto(View v) {
        Intent i = new Intent(this, AddCar.class);
        i.putExtra("email", userEmail); // Importante para guardar carro con el usuario actual
        startActivity(i);
    }
}

