package pupr.capstone.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import pupr.capstone.myapplication.databinding.ActivityDetalleAutoBinding;

public class DetalleAuto extends AppCompatActivity {

    ImageView imagen;
    TextView nombre, tablilla;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_auto);

        imagen = findViewById(R.id.imagenAuto);
        nombre = findViewById(R.id.nombreAuto);
        tablilla = findViewById(R.id.tablillaAuto);

        // Obtener datos del intent
        Intent intent = getIntent();
        nombre.setText(intent.getStringExtra("nombre"));
        tablilla.setText(intent.getStringExtra("tablilla"));
        imagen.setImageResource(intent.getIntExtra("imagen", R.drawable.ic_launcher_foreground));
    }
    }

