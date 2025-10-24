package pupr.capstone.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserActivity extends AppCompatActivity {

    private TextView tvUserName, tvUserEmail;
    private Button btnSignOut;

    // Sencillo pool para I/O
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user); // usa el layout que creaste

        // Referencias UI
        tvUserName  = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        btnSignOut  = findViewById(R.id.btnSignOut);



        // Toma el email de la sesión/intent
        String email = getIntent().getStringExtra("email");
        if (email == null || email.trim().isEmpty()) {
            Toast.makeText(this, "No se recibió el email del usuario", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        tvUserEmail.setText(email); // mostrar de una vez
        cargarPerfilDesdeBD(email);
        BottomNavRouter.setup(this, findViewById(R.id.bottomNav), R.id.nav_perfil, email);
        // Logout: limpia la pila para que no pueda volver con "Back"
        btnSignOut.setOnClickListener(v -> {
            getSharedPreferences("UserSession", MODE_PRIVATE)
                    .edit()
                    .clear()
                    .apply();
            Intent i = new Intent(this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        });
    }

    private void cargarPerfilDesdeBD(String email) {
        ioExecutor.execute(() -> {
            String nombre = null;
            String error = null;

            try {
                MyJDBC myJDBC = new MyJDBC();
                Connection cn = myJDBC.obtenerConexion();
                if (cn == null) {
                    error = "No se pudo conectar a la base de datos";
                } else {
                    // Ajusta a tu esquema real. Backticks por si `USER` es reservado.
                    String sql = "SELECT `NAME`, `EMAIL` FROM `USER` WHERE `EMAIL` = ? LIMIT 1";
                    try (PreparedStatement ps = cn.prepareStatement(sql)) {
                        ps.setString(1, email);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                nombre   = safe(rs.getString("NAME"));
                            } else {
                                error = "Usuario no encontrado";
                            }
                        }
                    }
                    cn.close();
                }
            } catch (Exception e) {
                Log.e("UserActivity", "Error consultando perfil", e);
                error = "Error consultando perfil: " + e.getMessage();
            }

            String finalNombre = nombre;
            String finalError = error;
            runOnUiThread(() -> {
                if (finalError != null) {
                    Toast.makeText(this, finalError, Toast.LENGTH_LONG).show();
                    return;
                }
                // Pinta datos
                tvUserName.setText(!finalNombre.isEmpty() ? finalNombre : "Usuario");
            });
        });
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ioExecutor.shutdownNow();
    }
}
