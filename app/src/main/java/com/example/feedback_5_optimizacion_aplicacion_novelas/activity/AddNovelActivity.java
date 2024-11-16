package com.example.feedback_5_optimizacion_aplicacion_novelas.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.feedback_5_optimizacion_aplicacion_novelas.R;
import com.example.feedback_5_optimizacion_aplicacion_novelas.domain.Novel;
import com.example.feedback_5_optimizacion_aplicacion_novelas.databaseSQL.SQLiteHelper;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.UUID;

public class AddNovelActivity extends AppCompatActivity {

    private EditText editTextTitle, editTextAuthor, editTextYear, editTextSynopsis;
    private ImageView imageViewCover;
    private Uri selectedImageUri;
    private FirebaseFirestore db;
    private SQLiteHelper sqliteHelper;
    private String novelId;

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    Glide.with(this).load(selectedImageUri).into(imageViewCover);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_novel);

        db = FirebaseFirestore.getInstance();
        sqliteHelper = new SQLiteHelper(this);

        editTextTitle = findViewById(R.id.edit_text_title);
        editTextAuthor = findViewById(R.id.edit_text_author);
        editTextYear = findViewById(R.id.edit_text_year);
        editTextSynopsis = findViewById(R.id.edit_text_synopsis);
        imageViewCover = findViewById(R.id.image_view_cover);
        Button buttonSave = findViewById(R.id.button_save);
        Button buttonSelectImage = findViewById(R.id.button_select_image);

        if (getIntent().hasExtra("EXTRA_ID")) {
            novelId = getIntent().getStringExtra("EXTRA_ID");
            loadNovelDetails(novelId);
        }

        buttonSelectImage.setOnClickListener(v -> openGallery());
        buttonSave.setOnClickListener(v -> saveNovel());
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void saveNovel() {
        String title = editTextTitle.getText().toString().trim();
        String author = editTextAuthor.getText().toString().trim();
        String yearString = editTextYear.getText().toString().trim();
        String synopsis = editTextSynopsis.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(author) || TextUtils.isEmpty(yearString) || TextUtils.isEmpty(synopsis)) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        int year = Integer.parseInt(yearString);
        String imageUri = selectedImageUri != null ? selectedImageUri.toString() : "";

        Novel novel = new Novel(title, author, year, synopsis, imageUri);

        if (novelId != null) {
            novel.setId(novelId);
            db.collection("novelas").document(novelId).set(novel)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(AddNovelActivity.this, "Novela actualizada", Toast.LENGTH_SHORT).show();
                        sqliteHelper.updateNovel(novel);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(AddNovelActivity.this, "Error al actualizar la novela", Toast.LENGTH_SHORT).show();
                    });
        } else {
            String randomId = UUID.randomUUID().toString();
            novel.setId(randomId);
            db.collection("novelas").document(randomId).set(novel)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(AddNovelActivity.this, "Novela agregada", Toast.LENGTH_SHORT).show();
                        sqliteHelper.addNovel(novel);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(AddNovelActivity.this, "Error al agregar la novela", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void loadNovelDetails(String novelId) {
        db.collection("novelas").document(novelId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Novel novel = documentSnapshot.toObject(Novel.class);
                    if (novel != null) {
                        editTextTitle.setText(novel.getTitle());
                        editTextAuthor.setText(novel.getAuthor());
                        editTextYear.setText(String.valueOf(novel.getYear()));
                        editTextSynopsis.setText(novel.getSynopsis());
                        if (!TextUtils.isEmpty(novel.getImageUri())) {
                            selectedImageUri = Uri.parse(novel.getImageUri());
                            Glide.with(this).load(selectedImageUri).into(imageViewCover);
                        }
                    }
                });
    }
}
