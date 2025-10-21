package pupr.capstone.myapplication;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Helper para wiring de la BottomNavigationView en múltiples pantallas.
 * Úsala desde cada Activity con:
 *
 * BottomNavRouter.setup(this, findViewById(R.id.bottomNav), R.id.nav_informe, userEmail);
 *
 * - currentItemId: el item del menú que corresponde a la pantalla actual
 * - userEmail: opcional, se pasará a la siguiente Activity si no es null
 */
public final class BottomNavRouter {

    private BottomNavRouter() { /* no instanciable */ }

    public static void setup(
            Activity activity,
            BottomNavigationView bottomNav,
            @IdRes int currentItemId,
            @Nullable String userEmail
    ) {
        if (bottomNav == null || activity == null) return;

        // Marca el item actual
        bottomNav.setSelectedItemId(currentItemId);

        // Evita recargar si tocan el mismo item
        bottomNav.setOnItemReselectedListener(item -> { /* no-op */ });

        bottomNav.setOnItemSelectedListener(item -> {
            final int clickedId = item.getItemId();

            // Si es el mismo item, consume y no navega
            if (clickedId == currentItemId) return true;

            Class<?> target = resolveTargetActivity(clickedId);
            if (target == null) return false;

            Intent i = new Intent(activity, target);
            if (userEmail != null) {
                i.putExtra("email", userEmail);
            }

            activity.startActivity(i);
            // transición y cierre opcional para que Back no te regrese a la pantalla anterior
            activity.overridePendingTransition(0, 0);
            activity.finish();

            return true;
        });
    }

    /**
     * Mapea los IDs del menú a las Activities de destino.
     * Ajusta estas clases según tu proyecto.
     */
    private static Class<?> resolveTargetActivity(@IdRes int menuItemId) {
        if (menuItemId == R.id.nav_garaje) {
            return GarageActivity.class;
        //} else if (menuItemId == R.id.nav_alerta) {
           // return AlertsActivity.class;      // <-- cámbiala si tu clase se llama distinto
        } else if (menuItemId == R.id.nav_informe) {
            return ServicesSelect.class;      // pantalla “Informe”
        } else if (menuItemId == R.id.nav_perfil) {
            return UserActivity.class;     // <-- cámbiala si tu clase se llama distinto
        }
        return null;
    }
}
