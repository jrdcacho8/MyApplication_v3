package pupr.capstone.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;

import java.sql.Connection;

public class MainActivity extends AppCompatActivity {

    Connection connect;
    String ConnectionResult = "";
    ProgressBar progressBar;

    // 🌐 Google Sign In
    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 🔍 Verificar si ya hay sesión guardada
        String savedEmail = getSharedPreferences("UserSession", MODE_PRIVATE)
                .getString("email", null);

        if (savedEmail != null) {
            // ✅ Ya hay usuario autenticado → saltar login
            Intent i = new Intent(this, GarageActivity.class);
            i.putExtra("email", savedEmail);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        // ✅ Configurar Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Botones
        Button accessButton = findViewById(R.id.accessButton);
        Button createButton = findViewById(R.id.createButton);
        Button googleButton = findViewById(R.id.google_signIn);
        progressBar = findViewById(R.id.progressBar);

        accessButton.setOnClickListener(this::handleAccess);
        createButton.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        googleButton.setOnClickListener(v -> signIn());
    }

    // 🚪 LOGIN NORMAL
    public void handleAccess(View v) {
        EditText emailInput = findViewById(R.id.editTextEmailAddress);
        EditText passInput = findViewById(R.id.editTextPassword);

        String userEmail = emailInput.getText().toString().trim();
        String userPass = passInput.getText().toString().trim();

        if (userEmail.isEmpty() || userPass.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        v.setEnabled(false);

        new Thread(() -> {
            boolean ok = false;
            String errorMsg = null;
            try {
                MyJDBC myJDBC = new MyJDBC();
                Connection connection = myJDBC.obtenerConexion();

                if (connection != null) {
                    String query = "SELECT 1 FROM `USER` WHERE `EMAIL` = ? AND `PASSWORD` = ?";
                    try (java.sql.PreparedStatement statement = connection.prepareStatement(query)) {
                        statement.setString(1, userEmail);
                        statement.setString(2, userPass);
                        try (java.sql.ResultSet rs = statement.executeQuery()) {
                            ok = rs.next();
                        }
                    }
                    connection.close();
                } else {
                    errorMsg = "Error de conexión a la base de datos";
                }
            } catch (Exception e) {
                e.printStackTrace();
                errorMsg = "Error: " + e.getMessage();
            }

            boolean finalOk = ok;
            String finalError = errorMsg;

            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                v.setEnabled(true);

                if (finalError != null) {
                    Toast.makeText(this, finalError, Toast.LENGTH_LONG).show();
                    return;
                }

                if (finalOk) {
                    // Guardar sesión local
                    getSharedPreferences("UserSession", MODE_PRIVATE)
                            .edit()
                            .putString("email", userEmail)
                            .apply();
                    Toast.makeText(this, "Acceso concedido", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(this, GarageActivity.class);
                    i.putExtra("email", userEmail);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // 🚫 Evita volver atrás
                    startActivity(i);
                    finish();
                } else {
                    Toast.makeText(this, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    // 🌐 LOGIN CON GOOGLE
    private void signIn() {
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                String email = account.getEmail();
                String name = account.getDisplayName();

                saveGoogleUserToDB(email, name);
                // Guardar sesión local
                getSharedPreferences("UserSession", MODE_PRIVATE)
                        .edit()
                        .putString("email", email)
                        .apply();
                Toast.makeText(this, "Bienvenido: " + name, Toast.LENGTH_SHORT).show();

                Intent i = new Intent(this, GarageActivity.class);
                i.putExtra("email", email);
                i.putExtra("name", name);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // 🚫 Evita volver atrás
                startActivity(i);
                finish();

            } catch (ApiException e) {
                Log.w("GoogleSignIn", "Error en login: " + e.getStatusCode());
                Toast.makeText(this, "Error al iniciar sesión con Google", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 💾 Guarda usuario Google en BD con contraseña aleatoria
    private void saveGoogleUserToDB(String email, String name) {
        new Thread(() -> {
            try {
                MyJDBC myJDBC = new MyJDBC();
                Connection cn = myJDBC.obtenerConexion();
                if (cn == null) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "No se pudo conectar a la BD", Toast.LENGTH_SHORT).show()
                    );
                    return;
                }

                String randomPassword = generateRandomPassword(12);
                String sql = "INSERT INTO `USER` (`EMAIL`, `NAME`, `PASSWORD`) " +
                        "VALUES (?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE `NAME` = VALUES(`NAME`)";

                try (java.sql.PreparedStatement ps = cn.prepareStatement(sql)) {
                    ps.setString(1, email);
                    ps.setString(2, name != null ? name : "");
                    ps.setString(3, randomPassword);
                    ps.executeUpdate();
                }

                cn.close();

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Error guardando usuario Google: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }

    // 🔑 Generador de contraseña aleatoria
    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#&$!";
        StringBuilder password = new StringBuilder();
        java.util.Random random = new java.util.Random();

        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }

}
