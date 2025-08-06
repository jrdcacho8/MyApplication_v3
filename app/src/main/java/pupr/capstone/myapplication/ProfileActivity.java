package pupr.capstone.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class ProfileActivity extends AppCompatActivity {

    TextView new_user_name, user_email, newPassword, reenterPassword;
    Button createAccount, cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_profile);

        new_user_name = findViewById(R.id.txtNewUserName);
        user_email = findViewById(R.id.txtNewUserEmail);
        newPassword = findViewById(R.id.txtNewPassword);
        reenterPassword = findViewById(R.id.txtReenterNewPassword);
        createAccount = findViewById(R.id.btnCreateAccount);
        cancel = findViewById(R.id.btnCancelNewAccount);

        cancel.setOnClickListener(v -> {
            new_user_name.setText("");
            user_email.setText("");
            newPassword.setText("");
            reenterPassword.setText("");
        });

        createAccount.setOnClickListener(v -> {
            String name = new_user_name.getText().toString().trim();
            String email = user_email.getText().toString().trim();
            String password = newPassword.getText().toString().trim();
            String repeatPassword = reenterPassword.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || repeatPassword.isEmpty()) {
                Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(repeatPassword)) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                MyJDBC myJDBC = new MyJDBC();
                Connection connection = myJDBC.obtenerConexion();

                if (connection != null) {
                    String query = "INSERT INTO USUARIO (NAME, EMAIL, PASSWORD) VALUES (?, ?, ?)";
                    PreparedStatement stmt = connection.prepareStatement(query);
                    stmt.setString(1, name);
                    stmt.setString(2, email);
                    stmt.setString(3, password);

                    int rowsInserted = stmt.executeUpdate();

                    stmt.close();
                    connection.close();

                    if (rowsInserted > 0) {
                        Toast.makeText(this, "Cuenta creada con éxito", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(this, GarageActivity.class);
                        i.putExtra("email", email);
                        startActivity(i);
                        finish();
                    } else {
                        Toast.makeText(this, "Error al crear la cuenta", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(this, "Error de conexión a la base de datos", Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
