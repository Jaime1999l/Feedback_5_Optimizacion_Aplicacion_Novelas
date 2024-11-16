package com.example.feedback_5_optimizacion_aplicacion_novelas.ui.fragments;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.feedback_5_optimizacion_aplicacion_novelas.PantallaPrincipalActivity;
import com.example.feedback_5_optimizacion_aplicacion_novelas.R;
import com.example.feedback_5_optimizacion_aplicacion_novelas.activity.AddReviewActivity;
import com.example.feedback_5_optimizacion_aplicacion_novelas.domain.Novel;
import com.example.feedback_5_optimizacion_aplicacion_novelas.ui.mainNovel.NovelViewModel;
import com.example.feedback_5_optimizacion_aplicacion_novelas.widget.NovelWidgetProvider;
import com.google.firebase.firestore.FirebaseFirestore;

public class NovelDetailFragment extends Fragment {

    private TextView titleTextView, authorTextView, synopsisTextView;
    private Button favoriteButton, reviewButton;
    private NovelViewModel novelViewModel;
    private FirebaseFirestore firebaseFirestore;
    private Novel currentNovel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_novel_detail, container, false);

        titleTextView = view.findViewById(R.id.text_view_title);
        authorTextView = view.findViewById(R.id.text_view_author);
        synopsisTextView = view.findViewById(R.id.text_view_synopsis);
        favoriteButton = view.findViewById(R.id.favorite_button);
        reviewButton = view.findViewById(R.id.review_button);

        firebaseFirestore = FirebaseFirestore.getInstance();
        novelViewModel = new ViewModelProvider(this).get(NovelViewModel.class);

        String novelId = getArguments() != null ? getArguments().getString("novelId") : null;

        if (novelId != null) {
            novelViewModel.getNovelById(novelId).observe(getViewLifecycleOwner(), novel -> {
                if (novel != null) {
                    currentNovel = novel;
                    displayNovelDetails(novel);

                    // Configuración del botón de favoritos
                    favoriteButton.setText(novel.isFavorite() ? "Eliminar de Favoritos" : "Añadir a Favoritos");
                    favoriteButton.setOnClickListener(v -> {
                        novel.setFavorite(!novel.isFavorite());
                        updateFavoriteStatusInFirebase(novel); // Actualiza en Firebase y el widget
                        favoriteButton.setText(novel.isFavorite() ? "Eliminar de Favoritos" : "Añadir a Favoritos");

                        // Notificar a PantallaPrincipalActivity para actualizar los favoritos en la UI
                        if (getActivity() instanceof PantallaPrincipalActivity) {
                            ((PantallaPrincipalActivity) getActivity()).refreshFavoritesList();
                        }
                    });

                    // Configuración del botón de reseña
                    reviewButton.setOnClickListener(v -> {
                        Intent intent = new Intent(getActivity(), AddReviewActivity.class);
                        intent.putExtra("EXTRA_NOVEL_ID", novel.getId());
                        intent.putExtra("EXTRA_NOVEL_NAME", novel.getTitle());
                        startActivity(intent);
                    });
                }
            });
        }

        return view;
    }

    private void displayNovelDetails(Novel novel) {
        titleTextView.setText(novel.getTitle());
        authorTextView.setText("Autor: " + novel.getAuthor());
        synopsisTextView.setText(novel.getSynopsis());
    }

    private void updateFavoriteStatusInFirebase(Novel novel) {
        firebaseFirestore.collection("novelas")
                .document(novel.getId())
                .update("favorite", novel.isFavorite())
                .addOnSuccessListener(aVoid -> {
                    // Después de que la actualización en Firebase se haya realizado, actualizar el widget y la interfaz
                    updateWidget();

                    // Llamar a refreshFavoritesList() en PantallaPrincipalActivity si estamos en esa actividad
                    if (getActivity() instanceof PantallaPrincipalActivity) {
                        ((PantallaPrincipalActivity) getActivity()).refreshFavoritesList();
                    }
                })
                .addOnFailureListener(e -> {
                });
    }

    private void updateWidget() {
        // Enviar un broadcast para actualizar el widget
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getContext());
        Intent intent = new Intent(getContext(), NovelWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

        // Obtener los IDs de los widgets actuales
        int[] ids = appWidgetManager.getAppWidgetIds(new ComponentName(getContext(), NovelWidgetProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);

        // Enviar el broadcast
        getContext().sendBroadcast(intent);
    }
}
