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

/*
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
*/
/**
 * MainActivity - Pantalla de autenticación de la aplicación
 *
 * Activity principal que maneja la autenticación de usuarios mediante Google Sign-In
 * y Firebase Authentication. Proporciona:
 * - Login con cuenta de Google
 * - Validación de sesión existente
 * - Redirección automática si el usuario ya está autenticado
 *
 * Esta Activity sirve como punto de entrada de la aplicación y valida
 * que el usuario esté autenticado antes de permitir acceso a otras pantallas.

 */


import java.sql.Connection;




public class MainActivity extends AppCompatActivity {

    Connection connect;
    String ConnectionResult="";

    // 🌐 Google Sign In
    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Configurar Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Botón login normal
        Button accessButton = findViewById(R.id.accessButton);
        accessButton.setOnClickListener(v -> handleAccess(v));

        // Botón crear cuenta
        Button createButton = findViewById(R.id.createButton);
        createButton.setOnClickListener(v -> handleCreateAccount(v));

        // Botón Google
        Button googleButton = findViewById(R.id.google_signIn);
        googleButton.setOnClickListener(v -> signIn());
    }

    public void handleAccess(View v){
        EditText emailInput = findViewById(R.id.editTextEmailAddress);
        EditText passInput = findViewById(R.id.editTextPassword);

        String userEmail = emailInput.getText().toString().trim();
        String userPass = passInput.getText().toString().trim();
        String userName= null;
        // Validación básica
        if (userEmail.isEmpty() || userPass.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            MyJDBC myJDBC = new MyJDBC();
            Connection connection = myJDBC.obtenerConexion();

            if (connection != null) {
                String query = "SELECT * FROM USUARIO WHERE EMAIL = ? AND PASSWORD = ?";
                java.sql.PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, userEmail);
                statement.setString(2, userPass);

                java.sql.ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    // Usuario autenticado con éxito
                    Toast.makeText(this, "Acceso concedido", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(this, GarageActivity.class);
                    i.putExtra("email",userEmail);
                    startActivity(i);
                } else {
                    Toast.makeText(this, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                }

                resultSet.close();
                statement.close();
                connection.close();
            } else {
                Toast.makeText(this, "Error de conexión a la base de datos", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    public void handleCreateAccount(View v){
        Intent i= new Intent(this, ProfileActivity.class);
        startActivity(i);
    }


    // 🌐 Google Sign In
    private void signIn() {
        //Forzar logout antes de iniciar sesión
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        101
                );
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                String email = account.getEmail();

                Toast.makeText(this, "Bienvenido: " + account.getDisplayName(), Toast.LENGTH_SHORT).show();

                // 🚗 Redirigir a la misma pantalla que login normal
                Intent i = new Intent(this, GarageActivity.class);
                i.putExtra("email", email);
                startActivity(i);

            } catch (ApiException e) {
                Log.w("GoogleSignIn", "Error en login: " + e.getStatusCode());
                Toast.makeText(this, "Fallo login con Google", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
