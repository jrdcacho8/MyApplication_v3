package pupr.capstone.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AutoAdapter extends RecyclerView.Adapter<AutoAdapter.AutoViewHolder> {

    private List<Vehicle> listaAutos;

    public String userEmail;

    public AutoAdapter(List<Vehicle> listaAutos, String userEmail) {
        this.listaAutos = listaAutos;
        this.userEmail= userEmail;
    }

    @NonNull
    @Override
    public AutoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_auto, parent, false);
        return new AutoViewHolder(view, listener); // <- Ya pasas el listener correctamente
    }



    @Override
    public void onBindViewHolder(@NonNull AutoViewHolder holder, int position) {
        Vehicle auto = listaAutos.get(position);
        String year= Integer.toString(auto.getYear());

        holder.nombre.setText(auto.getBrand()+" "+ auto.getModel()+" "+year);
        holder.tablilla.setText("Tablilla " + auto.getLicense_plate());
        holder.imagen.setImageBitmap(auto.getImageBitmap());

        holder.itemView.setTag(R.id.tag_vehicle, auto);
        holder.itemView.setTag(R.id.tag_user_email, userEmail);

    }


    @Override
    public int getItemCount() {
        return listaAutos.size();
    }
    public interface OnItemClickListener {
        void onItemClick(Vehicle auto, String email);
        void onItemClick(Vehicle auto);
    }

    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }



    public static class AutoViewHolder extends RecyclerView.ViewHolder {
        ImageView imagen;
        TextView nombre, tablilla;

        public AutoViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            imagen = itemView.findViewById(R.id.imageAuto);
            nombre = itemView.findViewById(R.id.textNombre);
            tablilla = itemView.findViewById(R.id.textTablilla);

            // Asignar clic al ítem completo
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (listener != null && position != RecyclerView.NO_POSITION) {
                        Vehicle vehicle = (Vehicle) v.getTag(R.id.tag_vehicle);
                        String email = (String) v.getTag(R.id.tag_user_email);

                        // Llamar al listener con los objetos recuperados
                        listener.onItemClick(vehicle, email);
                    }
                }
            });
        }
    }

}
