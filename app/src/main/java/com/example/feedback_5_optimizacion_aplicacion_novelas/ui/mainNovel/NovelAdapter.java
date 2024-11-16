package com.example.feedback_5_optimizacion_aplicacion_novelas.ui.mainNovel;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class NovelAdapter extends RecyclerView.Adapter<NovelAdapter.NovelHolder> {

    private List<Novel> novelList = new ArrayList<>();
    private final OnNovelClickListener onNovelClickListener;
    private final Context context;

    public NovelAdapter(OnNovelClickListener onNovelClickListener, Context context) {
        this.onNovelClickListener = onNovelClickListener;
        this.context = context;
    }

    public void setNovels(List<Novel> novels) {
        this.novelList = novels;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NovelHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflar el diseño del item
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.novel_item, parent, false);
        return new NovelHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NovelHolder holder, int position) {
        Novel currentNovel = novelList.get(position);

        holder.textViewTitle.setText(currentNovel.getTitle());
        holder.textViewAuthor.setText(currentNovel.getAuthor());

        // Cargar imagen usando Glide o Bitmap
        if (currentNovel.getImageUri() != null && !currentNovel.getImageUri().isEmpty()) {
            Bitmap bitmap = decodeSampledBitmapFromUri(Uri.parse(currentNovel.getImageUri()), 100, 100);
            if (bitmap != null) {
                holder.imageViewCover.setImageBitmap(bitmap);
            } else {
                holder.imageViewCover.setImageResource(R.drawable.error_image);
            }
        } else {
            holder.imageViewCover.setImageResource(R.drawable.placeholder_image);
        }

        // Configurar evento de clic para navegar al detalle de la novela
        holder.itemView.setOnClickListener(v -> onNovelClickListener.onNovelClick(currentNovel));
    }


    @Override
    public int getItemCount() {
        return novelList.size();
    }

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

    /**
     * Decodifica un Bitmap desde un URI con redimensionamiento para evitar problemas de memoria.
     */
    private Bitmap decodeSampledBitmapFromUri(Uri uri, int reqWidth, int reqHeight) {
        try {
            // Obtener las dimensiones del Bitmap original
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            BitmapFactory.decodeStream(inputStream, null, options);
            if (inputStream != null) {
                inputStream.close();
            }

            // Calcular el inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            // Decodificar el Bitmap con inSampleSize
            options.inJustDecodeBounds = false;
            inputStream = context.getContentResolver().openInputStream(uri);
            Bitmap scaledBitmap = BitmapFactory.decodeStream(inputStream, null, options);
            if (inputStream != null) {
                inputStream.close();
            }
            return scaledBitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Calcula el inSampleSize para redimensionar un Bitmap.
     */
    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calcular la mayor potencia de 2 que sigue siendo válida
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}
