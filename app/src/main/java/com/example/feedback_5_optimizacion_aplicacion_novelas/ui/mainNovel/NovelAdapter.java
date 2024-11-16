package com.example.feedback_5_optimizacion_aplicacion_novelas.ui.mainNovel;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.feedback_5_optimizacion_aplicacion_novelas.R;
import com.example.feedback_5_optimizacion_aplicacion_novelas.domain.Novel;

import java.util.ArrayList;
import java.util.List;

public class NovelAdapter extends RecyclerView.Adapter<NovelAdapter.NovelHolder> {

    private List<Novel> novelList = new ArrayList<>();
    private final OnNovelClickListener onNovelClickListener;

    // Constructor recibe la interfaz
    public NovelAdapter(OnNovelClickListener onNovelClickListener) {
        this.onNovelClickListener = onNovelClickListener;
    }

    // Método para actualizar la lista de novelas en el adaptador
    public void setNovels(List<Novel> novels) {
        this.novelList = novels;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NovelHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.novel_item, parent, false);
        return new NovelHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NovelHolder holder, int position) {
        Novel currentNovel = novelList.get(position);
        holder.textViewTitle.setText(currentNovel.getTitle());
        holder.textViewAuthor.setText(currentNovel.getAuthor());

        if (currentNovel.getImageUri() != null && !currentNovel.getImageUri().isEmpty()) {
            holder.imageViewCover.setVisibility(View.VISIBLE);
            holder.imageViewCover.setImageURI(Uri.parse(currentNovel.getImageUri()));
        } else {
            holder.imageViewCover.setVisibility(View.GONE);
        }

        // Evento de clic para el título
        holder.textViewTitle.setOnClickListener(v -> onNovelClickListener.onNovelClick(currentNovel));

        // Evento de clic para el botón de favorito
        holder.favoriteButton.setText(currentNovel.isFavorite() ? "Eliminar de Favoritos" : "Añadir a Favoritos");
        holder.favoriteButton.setOnClickListener(v -> onNovelClickListener.onFavoriteClick(currentNovel));

        // Evento de clic para el botón de reseña
        holder.reviewButton.setOnClickListener(v -> onNovelClickListener.onReviewClick(currentNovel));
    }

    @Override
    public int getItemCount() {
        return novelList.size();
    }

    // Interfaz para manejar clics en el adaptador
    public interface OnNovelClickListener {
        void onNovelClick(Novel novel);
        void onFavoriteClick(Novel novel);
        void onReviewClick(Novel novel);
    }

    public static class NovelHolder extends RecyclerView.ViewHolder {
        private final TextView textViewTitle;
        private final TextView textViewAuthor;
        private final ImageView imageViewCover;
        private final Button favoriteButton;
        private final Button reviewButton;

        public NovelHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.text_view_title);
            textViewAuthor = itemView.findViewById(R.id.text_view_author);
            imageViewCover = itemView.findViewById(R.id.image_view_cover);
            favoriteButton = itemView.findViewById(R.id.favorite_button);
            reviewButton = itemView.findViewById(R.id.review_button);
        }
    }
}
