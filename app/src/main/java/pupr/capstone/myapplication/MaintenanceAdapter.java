package pupr.capstone.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;



//Se puede incluir detalles de fechas en cada barra
public class MaintenanceAdapter extends RecyclerView.Adapter<MaintenanceAdapter.MantenimientoViewHolder> {

    private List<Maintenance> listaMaintenances;
    private Vehicle auto;
    private String userEmail;
    private OnItemClickListener listener;

    public MaintenanceAdapter(List<Maintenance> listaMaintenances ) {
        this.listaMaintenances = listaMaintenances;
    }

    public MaintenanceAdapter(List<Maintenance> listaMaintenances, String userEmail ) {
        this.listaMaintenances = listaMaintenances;
        this.userEmail=userEmail;
    }

    public MaintenanceAdapter(List<Maintenance> listaMaintenances, Vehicle auto, String userEmail ) {
        this.listaMaintenances = listaMaintenances;
        this.auto = auto;
        this.userEmail= userEmail;
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
        holder.textMantenimiento.setText(maintenance.getMaintenance());



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

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public static class MantenimientoViewHolder extends RecyclerView.ViewHolder {
        ImageView imageMantenimiento;
        TextView textMantenimiento;

        public MantenimientoViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            imageMantenimiento = itemView.findViewById(R.id.imageMantenimiento);
            textMantenimiento = itemView.findViewById(R.id.textMantenimiento);
            
            //ojo aqui se pueden añadir mas detalles
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                   Maintenance maintenance= (Maintenance) v.getTag(R.id.tag_maintenance);
                    Vehicle vehicle = (Vehicle) v.getTag(R.id.tag_vehicle);
                   String email = (String) v.getTag(R.id.tag_user_email);
                   
                   
                    listener.onItemClick(maintenance, vehicle,email);
                }
            });

        }
    }
}