// FavoritesActivity.java
package com.example.feedback_5_optimizacion_aplicacion_novelas.activity;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.feedback_5_optimizacion_aplicacion_novelas.R;
import com.example.feedback_5_optimizacion_aplicacion_novelas.domain.Novel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity {

    private LinearLayout favoritesLayout;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favorites_activity);

        favoritesLayout = findViewById(R.id.favorites_layout);
        firebaseFirestore = FirebaseFirestore.getInstance();

        loadFavoriteNovelsFromFirebase();
    }

    private void loadFavoriteNovelsFromFirebase() {
        firebaseFirestore.collection("novelas")
                .whereEqualTo("favorite", true)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        // Manejo de errores
                        return;
                    }

                    List<Novel> favoriteNovels = new ArrayList<>();
                    for (QueryDocumentSnapshot document : snapshots) {
                        Novel novel = document.toObject(Novel.class);
                        novel.setId(document.getId());
                        favoriteNovels.add(novel);
                    }
                    displayFavoriteNovels(favoriteNovels);
                });
    }

    private void displayFavoriteNovels(List<Novel> novels) {
        favoritesLayout.removeAllViews();

        if (novels == null || novels.isEmpty()) {
            TextView noFavoritesTextView = new TextView(this);
            noFavoritesTextView.setText("No hay novelas favoritas.");
            favoritesLayout.addView(noFavoritesTextView);
            return;
        }

        for (Novel novel : novels) {
            TextView novelView = new TextView(this);
            novelView.setText(novel.getTitle() + "\n" + novel.getAuthor());
            novelView.setPadding(16, 16, 16, 16);
            favoritesLayout.addView(novelView);
        }
    }
}
