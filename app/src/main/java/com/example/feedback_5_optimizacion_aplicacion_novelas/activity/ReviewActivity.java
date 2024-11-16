package com.example.feedback_5_optimizacion_aplicacion_novelas.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.feedback_5_optimizacion_aplicacion_novelas.R;
import com.example.feedback_5_optimizacion_aplicacion_novelas.domain.Novel;
import com.example.feedback_5_optimizacion_aplicacion_novelas.domain.Review;
import com.example.feedback_5_optimizacion_aplicacion_novelas.databaseSQL.SQLiteHelper;
import com.example.feedback_5_optimizacion_aplicacion_novelas.ui.review.ReviewViewModel;

import java.util.List;

public class ReviewActivity extends AppCompatActivity {
    private ReviewViewModel reviewViewModel;
    private LinearLayout reviewsLayout;
    private SQLiteHelper sqliteHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        reviewsLayout = findViewById(R.id.reviews_layout);
        reviewViewModel = new ViewModelProvider(this).get(ReviewViewModel.class);
        sqliteHelper = new SQLiteHelper(this);

        // Obtener todas las reseñas desde Firebase y SQLite
        loadAllReviewsFromSQLite();
        loadAllReviewsFromFirebase();
    }

    // Cargar reseñas desde SQLite
    private void loadAllReviewsFromSQLite() {
        List<Review> reviewsFromSQLite = sqliteHelper.getAllReviews();
        displayReviews(reviewsFromSQLite);
    }

    // Cargar reseñas desde Firebase y almacenarlas también en SQLite
    private void loadAllReviewsFromFirebase() {
        reviewViewModel.getAllReviews().observe(this, reviews -> {
            for (Review review : reviews) {
                sqliteHelper.addReview(review);
            }
            displayReviews(reviews);
        });
    }

    @SuppressLint("SetTextI18n")
    private void displayReviews(List<Review> reviews) {
        reviewsLayout.removeAllViews();
        for (Review review : reviews) {
            TextView reviewView = new TextView(this);
            reviewView.setText("Usuario: " + review.getReviewer() + "\n" +
                    "Comentario: " + review.getComment() + "\n" +
                    "Puntuación: " + review.getRating() + "\n" +
                    "Nombre de la novela: " + review.getNovelName());
            reviewView.setPadding(16, 16, 16, 16);

            // Botón para ver más detalles de la novela
            Button viewNovelButton = new Button(this);
            viewNovelButton.setText("Ver Novela");
            viewNovelButton.setOnClickListener(v -> loadNovelDetails(review.getNovelId()));

            reviewsLayout.addView(reviewView);
            reviewsLayout.addView(viewNovelButton);
        }

        if (reviews.isEmpty()) {
            TextView noReviewsView = new TextView(this);
            noReviewsView.setText("No hay reseñas disponibles.");
            noReviewsView.setPadding(16, 16, 16, 16);
            reviewsLayout.addView(noReviewsView);
        }
    }

    private void loadNovelDetails(String novelId) {
        reviewViewModel.getNovelById(novelId).observe(this, new Observer<Novel>() {
            @Override
            public void onChanged(Novel novel) {
                if (novel != null) {
                    showNovelDetails(novel);
                }
            }
        });
    }

    private void showNovelDetails(Novel novel) {
        Toast.makeText(this, "Título: " + novel.getTitle() + "\nAutor: " + novel.getAuthor(), Toast.LENGTH_LONG).show();
    }
}
