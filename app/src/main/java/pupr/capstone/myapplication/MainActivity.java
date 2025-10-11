package pupr.capstone.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
}
