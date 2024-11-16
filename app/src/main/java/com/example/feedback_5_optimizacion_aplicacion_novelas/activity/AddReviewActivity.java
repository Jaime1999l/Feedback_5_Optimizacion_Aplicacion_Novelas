package com.example.feedback_5_optimizacion_aplicacion_novelas.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.feedback_5_optimizacion_aplicacion_novelas.R;
import com.example.feedback_5_optimizacion_aplicacion_novelas.domain.Review;
import com.example.feedback_5_optimizacion_aplicacion_novelas.databaseSQL.SQLiteHelper;
import com.example.feedback_5_optimizacion_aplicacion_novelas.ui.review.ReviewViewModel;

public class AddReviewActivity extends AppCompatActivity {
    private EditText editTextReviewer, editTextComment, editTextRating;
    private Button buttonAddReview;
    private ReviewViewModel reviewViewModel;
    private SQLiteHelper sqliteHelper;
    private String novelId;
    private String novelName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_review);

        novelId = getIntent().getStringExtra("EXTRA_NOVEL_ID");
        novelName = getIntent().getStringExtra("EXTRA_NOVEL_NAME");

        editTextReviewer = findViewById(R.id.edit_text_reviewer);
        editTextComment = findViewById(R.id.edit_text_comment);
        editTextRating = findViewById(R.id.edit_text_rating);
        buttonAddReview = findViewById(R.id.button_add_review);

        reviewViewModel = new ViewModelProvider(this).get(ReviewViewModel.class);
        sqliteHelper = new SQLiteHelper(this);

        buttonAddReview.setOnClickListener(v -> addReview());
    }

    private void addReview() {
        String reviewer = editTextReviewer.getText().toString().trim();
        String comment = editTextComment.getText().toString().trim();
        String ratingStr = editTextRating.getText().toString().trim();

        if (reviewer.isEmpty() || comment.isEmpty() || ratingStr.isEmpty()) {
            Toast.makeText(this, "Por favor, rellene todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        int rating = Integer.parseInt(ratingStr);
        Review review = new Review(novelId, reviewer, comment, rating, novelName);

        // Guardar reseña en Firebase
        reviewViewModel.addReview(review);

        // Guardar reseña en SQLite
        sqliteHelper.addReview(review);

        Toast.makeText(this, "Reseña añadida", Toast.LENGTH_SHORT).show();
        finish();
    }
}
