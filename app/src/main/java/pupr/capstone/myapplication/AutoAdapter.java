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

    private List<Auto> listaAutos;

    public AutoAdapter(List<Auto> listaAutos) {
        this.listaAutos = listaAutos;
    }

    @NonNull
    @Override
    public AutoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_auto, parent, false);
        return new AutoViewHolder(view, listener); // <- Ya pasas el listener correctamente
    }



    @Override
    public void onBindViewHolder(@NonNull AutoViewHolder holder, int position) {
        Auto auto = listaAutos.get(position);
        holder.nombre.setText(auto.getNombre());
        holder.tablilla.setText("Tablilla " + auto.getTablilla());
        holder.imagen.setImageBitmap(auto.getImagenBitmap());

        holder.itemView.setTag(auto);  // <- Aquí se guarda el objeto Auto
    }


    @Override
    public int getItemCount() {
        return listaAutos.size();
    }
    public interface OnItemClickListener {
        void onItemClick(Auto auto);
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
                        listener.onItemClick((Auto) v.getTag());
                    }
                }
            });
        }
    }

}
