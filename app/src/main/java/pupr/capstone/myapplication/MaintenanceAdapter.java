package pupr.capstone.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MaintenanceAdapter extends RecyclerView.Adapter<MaintenanceAdapter.MantenimientoViewHolder> {

    private final List<Maintenance> listaMaintenances;
    private final Vehicle auto;
    private final String userEmail;
    private OnItemClickListener listener;

    // Use a static map for efficient image lookup
    private static final Map<Integer, Integer> IMAGE_MAP = createImageMap();

    // The preferred constructor, requiring necessary data
    public MaintenanceAdapter(List<Maintenance> listaMaintenances, Vehicle auto, String userEmail) {
        this.listaMaintenances = listaMaintenances;
        this.auto = auto;
        this.userEmail = userEmail;
    }

    @NonNull
    @Override
    public MantenimientoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mantenimiento, parent, false);
        return new MantenimientoViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull MantenimientoViewHolder holder, int position) {
        Maintenance maintenance = listaMaintenances.get(position);

        // Define formatters once or as static finals if used in other methods
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("MMM-dd-yyyy", Locale.US);

        holder.textMantenimiento.setText(maintenance.getMaintenance());

        // ⚠️ WARNING: These database calls should be asynchronous in a real application ⚠️
        int mileageDue = checkGetExistingMileageDue(userEmail, auto.getLicense_plate(), maintenance.getMaintenance());
        String alertDate = checkGetExistingAlertDate(userEmail, auto.getLicense_plate(), maintenance.getMaintenance());

        // --- Display Logic Refactoring ---

        // 1. Process and set the Date TextView
        if (alertDate != null) {
            try {
                LocalDate mantLastDate = LocalDate.parse(alertDate, dateFormat);
                String formattedDate = mantLastDate.format(outputFormat);

                holder.txtDueDate.setText(formattedDate);
                holder.txtDueDate.setVisibility(View.VISIBLE);
            } catch (java.time.format.DateTimeParseException e) {
                // Handle parsing error if date format is unexpected
                holder.txtDueDate.setVisibility(View.GONE);
                System.err.println("Date parsing error for: " + alertDate + e.getMessage());
            }
        } else {
            holder.txtDueDate.setVisibility(View.GONE);
        }

        // 2. Process and set the Mileage TextView
        if (mileageDue != 0) {
            String mileageStr = Integer.toString(mileageDue);
            holder.lblMileageDue.setVisibility(View.VISIBLE);
            holder.txtMileageDue.setText(mileageStr);
            holder.txtMileageDue.setVisibility(View.VISIBLE);
        } else {
            holder.lblMileageDue.setVisibility(View.GONE);
            holder.txtMileageDue.setVisibility(View.GONE);
        }

        // 3. Set visibility for the label based on if any alert is present
        if (alertDate != null || mileageDue != 0) {
            holder.lblDueDate.setVisibility(View.VISIBLE);
        } else {
            holder.lblDueDate.setVisibility(View.GONE);
        }

        // --- Image Loading Refactoring ---

        int key_image = maintenance.getImage();
        Integer resourceId = IMAGE_MAP.get(key_image);

        if (resourceId != null) {
            holder.imageMantenimiento.setImageResource(resourceId);
        } else {
            holder.imageMantenimiento.setImageResource(R.drawable.generalmaintenance);
        }

        // --- Set tags for the click listener ---

        holder.itemView.setTag(R.id.tag_maintenance, maintenance);
        holder.itemView.setTag(R.id.tag_user_email, userEmail);
        holder.itemView.setTag(R.id.tag_vehicle, auto);
    }

    @Override
    public int getItemCount() {
        return listaMaintenances.size();
    }

    // --- Helper method for the image map ---
    private static Map<Integer, Integer> createImageMap() {
        Map<Integer, Integer> map = new HashMap<>();
        map.put(1, R.drawable.aceitemotor);
        map.put(2, R.drawable.aceitemotor);
        map.put(3, R.drawable.wheel_check);
        map.put(4, R.drawable.wiper_blades);
        map.put(5, R.drawable.filtrodeairemotor);
        map.put(6, R.drawable.anticongelante);
        map.put(7, R.drawable.aceite_transmision);
        map.put(8, R.drawable.liquido_direccion_asistida);
        map.put(9, R.drawable.wiperfluid);
        map.put(10, R.drawable.correa_mangas);
        map.put(11, R.drawable.bateria);
        map.put(12, R.drawable.rotaciondegomas);
        map.put(13, R.drawable.sistemadefrenos);
        map.put(14, R.drawable.filtrodecabina);
        map.put(15, R.drawable.sistemademuffle);
        map.put(16, R.drawable.reemplazo_bujias);
        map.put(17, R.drawable.reemplazo_bujias);
        map.put(18, R.drawable.wheel_alignment);
        return map;
    }

    // --- Database Access Methods (Async Refactoring Required) ---

    // ⚠️ These methods should be wrapped in an AsyncTask, Thread, or use a proper
    // persistence library like Room, which handles background threading.
    public String checkGetExistingAlertDate(String email, String licensePlate, String maintenanceType) {
        // Implementation remains the same but requires proper connection closure (see notes)
        // AND MUST BE RUN OFF THE UI THREAD.
        String query = "SELECT ALERT_DATE FROM ALERT WHERE EMAIL = ? AND LICENSE_PLATE = ? AND NAME_ALERT = ?";
        String existingDate = null;
        java.sql.Connection con = null;

        try {
            MyJDBC jdbc = new MyJDBC();
            con = jdbc.obtenerConexion();
            try (java.sql.PreparedStatement stmt = con.prepareStatement(query)) {
                stmt.setString(1, email);
                stmt.setString(2, licensePlate);
                stmt.setString(3, maintenanceType);
                try (java.sql.ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        existingDate = rs.getString("ALERT_DATE");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Database error retrieving alert date: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (con != null) {
                try { con.close(); } catch (java.sql.SQLException e) { /* Log close error */ }
            }
        }

        return existingDate;
    }

    public int checkGetExistingMileageDue(String email, String licensePlate, String maintenanceType) {
        // Implementation remains the same but requires proper connection closure (see notes)
        // AND MUST BE RUN OFF THE UI THREAD.
        String query = "SELECT MILEAGE_DUE FROM ALERT WHERE EMAIL = ? AND LICENSE_PLATE = ? AND NAME_ALERT = ?";
        int existingMileage = 0;
        java.sql.Connection con = null;

        try {
            MyJDBC jdbc = new MyJDBC();
            con = jdbc.obtenerConexion();
            try (java.sql.PreparedStatement stmt = con.prepareStatement(query)) {
                stmt.setString(1, email);
                stmt.setString(2, licensePlate);
                stmt.setString(3, maintenanceType);
                try (java.sql.ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        existingMileage = rs.getInt("MILEAGE_DUE");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Database error retrieving mileage due: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (con != null) {
                try { con.close(); } catch (java.sql.SQLException e) { /* Log close error */ }
            }
        }

        return existingMileage;
    }

    // --- Interface and ViewHolder ---

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        // You can simplify this interface to one method if the other two are redundant
        void onItemClick(Maintenance maintenance, Vehicle auto, String userEmail);
    }

    public static class MantenimientoViewHolder extends RecyclerView.ViewHolder {
        public final TextView txtDueDate, txtMileageDue, lblDueDate, lblMileageDue, textMantenimiento;
        public final ImageView imageMantenimiento;

        public MantenimientoViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            imageMantenimiento = itemView.findViewById(R.id.imageMantenimiento);
            textMantenimiento = itemView.findViewById(R.id.textMantenimiento);
            lblDueDate = itemView.findViewById(R.id.lblMileageDue);
            txtDueDate = itemView.findViewById(R.id.txtDueDate);
            lblMileageDue= itemView.findViewById(R.id.lblMileageDue);
            txtMileageDue= itemView.findViewById(R.id.txtMileageDue);

            // Set initial visibility to GONE in the ViewHolder

            lblDueDate.setVisibility(View.GONE);
            txtDueDate.setVisibility(View.GONE);
            lblMileageDue.setVisibility(View.GONE);
            txtMileageDue.setVisibility(View.GONE);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    // It's safer to get the actual item from the adapter's list
                    // or ensure the tags are correctly set for every item.
                    Maintenance maintenance = (Maintenance) v.getTag(R.id.tag_maintenance);
                    Vehicle vehicle = (Vehicle) v.getTag(R.id.tag_vehicle);
                    String email = (String) v.getTag(R.id.tag_user_email);

                    if (maintenance != null && vehicle != null && email != null) {
                        listener.onItemClick(maintenance, vehicle, email);
                    }
                }
            });
        }
    }
}