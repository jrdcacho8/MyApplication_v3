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
import java.util.List;
import java.util.Locale;


//Se puede incluir detalles de fechas en cada barra
public class MaintenanceAdapter extends RecyclerView.Adapter<MaintenanceAdapter.MantenimientoViewHolder> {

    private List<Maintenance> listaMaintenances;
    private Vehicle auto;
    private String userEmail;
    private OnItemClickListener listener;


    public MaintenanceAdapter(List<Maintenance> listaMaintenances) {
        this.listaMaintenances = listaMaintenances;
    }

    public MaintenanceAdapter(List<Maintenance> listaMaintenances, String userEmail) {
        this.listaMaintenances = listaMaintenances;
        this.userEmail = userEmail;
    }

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

        DateTimeFormatter date_format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter output = DateTimeFormatter.ofPattern("MMM-dd-yyyy", Locale.US);

        holder.textMantenimiento.setText(maintenance.getMaintenance());

        int mileage_due = checkGetExistingMileageDue(userEmail, auto.getLicense_plate(), maintenance.getMaintenance());
        String alertDate = checkGetExistingAlertDate(userEmail, auto.getLicense_plate(), maintenance.getMaintenance());
        String str, txt;
        if (alertDate != null && mileage_due != 0) {


            holder.lblDueDate.setVisibility(View.VISIBLE);
            holder.txtDueDate.setVisibility(View.VISIBLE);

            LocalDate mantLastDate = LocalDate.parse(alertDate, date_format);
            String formattedDate = mantLastDate.format(output);
            str = Integer.toString(mileage_due);
            txt = formattedDate + "or" + str;
            holder.txtDueDate.setText(txt);

        } else if (alertDate != null) {

            holder.lblDueDate.setVisibility(View.VISIBLE);
            holder.txtDueDate.setVisibility(View.VISIBLE);
            LocalDate mantLastDate = LocalDate.parse(alertDate, date_format);

            String formattedDate = mantLastDate.format(output);

            holder.txtDueDate.setText(formattedDate);

        } else if (mileage_due != 0) {


            str = Integer.toString(mileage_due);

            holder.lblDueDate.setVisibility(View.VISIBLE);
            holder.txtDueDate.setVisibility(View.VISIBLE);

            holder.txtDueDate.setText(str);

        }


        int key_image = maintenance.getImage();

        switch (key_image) {
            case 1:

                holder.imageMantenimiento.setImageResource(R.drawable.aceitemotor);
                break;
            case 2:
                holder.imageMantenimiento.setImageResource(R.drawable.aceitemotor);
                break;

            case 3:
                holder.imageMantenimiento.setImageResource(R.drawable.wheel_check);
                break;

            case 4:
                holder.imageMantenimiento.setImageResource(R.drawable.wiper_blades);
                break;
            case 5:
                holder.imageMantenimiento.setImageResource(R.drawable.filtrodeairemotor);
                break;
            case 6:
                holder.imageMantenimiento.setImageResource(R.drawable.anticongelante);
                break;
            case 7:
                holder.imageMantenimiento.setImageResource(R.drawable.aceite_transmision);
                break;
            case 8:
                holder.imageMantenimiento.setImageResource(R.drawable.liquido_direccion_asistida);
                break;
            case 9:
                holder.imageMantenimiento.setImageResource(R.drawable.wiperfluid);
                break;
            case 10:
                holder.imageMantenimiento.setImageResource(R.drawable.correa_mangas);
                break;
            case 11:
                holder.imageMantenimiento.setImageResource(R.drawable.bateria);
                break;
            case 12:
                holder.imageMantenimiento.setImageResource(R.drawable.rotaciondegomas);
                break;
            case 13:
                holder.imageMantenimiento.setImageResource(R.drawable.sistemadefrenos);
                break;
            case 14:
                holder.imageMantenimiento.setImageResource(R.drawable.filtrodecabina);
                break;
            case 15:
                holder.imageMantenimiento.setImageResource(R.drawable.sistemademuffle);
                break;
            case 16:
                holder.imageMantenimiento.setImageResource(R.drawable.reemplazo_bujias);
                break;
            case 17:
                holder.imageMantenimiento.setImageResource(R.drawable.reemplazo_bujias);
                break;
            case 18:
                holder.imageMantenimiento.setImageResource(R.drawable.wheel_alignment);
                break;
            default:
                holder.imageMantenimiento.setImageResource(R.drawable.generalmaintenance);
                break;


        }

        holder.itemView.setTag(R.id.tag_maintenance, maintenance);
        holder.itemView.setTag(R.id.tag_user_email, userEmail);
        holder.itemView.setTag(R.id.tag_vehicle, auto);

    }

    @Override
    public int getItemCount() {
        return listaMaintenances.size();
    }

    // Interfaz para clics
    public interface OnItemClickListener {

        void onItemClick(Maintenance maintenance);

        void onItemClick(Maintenance maintenance, String userEmail);

        void onItemClick(Maintenance maintenance, Vehicle auto, String userEmail);
    }

    public String checkGetExistingAlertDate(String email, String licensePlate, String maintenanceType) {
        String query = "SELECT ALERT_DATE FROM ALERT WHERE EMAIL = ? AND LICENSE_PLATE = ? AND NAME_ALERT = ?";
        String existingDate = null;

        MyJDBC jdbc = new MyJDBC();
        java.sql.Connection con = jdbc.obtenerConexion();

        try (
                java.sql.PreparedStatement stmt = con.prepareStatement(query)) {

            stmt.setString(1, email);
            stmt.setString(2, licensePlate);
            stmt.setString(3, maintenanceType);

            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Assuming ALERT_DATE is stored as a standard SQL DATE or VARCHAR
                    existingDate = rs.getString("ALERT_DATE");
                    // Convert the String from the DB back into a LocalDate

                    if (existingDate != null) {
                        // We convert it to a LocalDate just to validate/parse it if needed,
                        // but we return the original string for compatibility with the caller.
                        // LocalDate existingDate = LocalDate.parse(dateStringResult); // Optional validation
                        return existingDate; // Return the valid date string
                    }
                }
            }
        } catch (Exception e) {
            // Log the error but return null so the app doesn't crash
            System.err.println("Error de base de dato durante búsqueda de fecha de alerta: " + e.getMessage());
            e.printStackTrace();
        }

        return null;

    }

    public int checkGetExistingMileageDue(String email, String licensePlate, String maintenanceType) {
        String query = "SELECT MILEAGE_DUE FROM ALERT WHERE EMAIL = ? AND LICENSE_PLATE = ? AND NAME_ALERT = ?";
        int existingMileage = 0;

        MyJDBC jdbc = new MyJDBC();
        java.sql.Connection con = jdbc.obtenerConexion();

        try (
                java.sql.PreparedStatement stmt = con.prepareStatement(query)) {

            stmt.setString(1, email);
            stmt.setString(2, licensePlate);
            stmt.setString(3, maintenanceType);

            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Assuming ALERT_DATE is stored as a standard SQL DATE or VARCHAR
                    existingMileage = rs.getInt("MILEAGE_DUE");
                    // Convert the String from the DB back into a LocalDate

                    if (existingMileage != 0) {
                        // We convert it to a LocalDate just to validate/parse it if needed,
                        // but we return the original string for compatibility with the caller.
                        // LocalDate existingDate = LocalDate.parse(dateStringResult); // Optional validation
                        return existingMileage; // Return the valid date string
                    }
                }
            }
        } catch (Exception e) {
            // Log the error but return null so the app doesn't crash
            System.err.println("Error de base de dato durante búsqueda de millaje: " + e.getMessage());
            e.printStackTrace();
        }

        return 0;

    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public static class MantenimientoViewHolder extends RecyclerView.ViewHolder {
        public TextView txtDueDate;
        public TextView lblDueDate;
        ImageView imageMantenimiento;
        TextView textMantenimiento;

        public MantenimientoViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            imageMantenimiento = itemView.findViewById(R.id.imageMantenimiento);
            textMantenimiento = itemView.findViewById(R.id.textMantenimiento);
            lblDueDate = itemView.findViewById(R.id.lblDueDate);
            txtDueDate = itemView.findViewById(R.id.txtDueDate);

            lblDueDate.setVisibility(View.GONE);
            txtDueDate.setVisibility(View.GONE);

            //ojo aqui se pueden añadir mas detalles
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    Maintenance maintenance = (Maintenance) v.getTag(R.id.tag_maintenance);
                    Vehicle vehicle = (Vehicle) v.getTag(R.id.tag_vehicle);
                    String email = (String) v.getTag(R.id.tag_user_email);


                    listener.onItemClick(maintenance, vehicle, email);
                }
            });

        }
    }

}