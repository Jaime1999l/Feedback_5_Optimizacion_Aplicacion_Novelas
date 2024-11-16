// PantallaPrincipalActivity.java
package com.example.feedback_5_optimizacion_aplicacion_novelas;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;

import com.example.feedback_5_optimizacion_aplicacion_novelas.activity.AddNovelActivity;
import com.example.feedback_5_optimizacion_aplicacion_novelas.activity.FavoritesActivity;
import com.example.feedback_5_optimizacion_aplicacion_novelas.activity.ReviewActivity;
import com.example.feedback_5_optimizacion_aplicacion_novelas.activity.SettingsActivity;
import com.example.feedback_5_optimizacion_aplicacion_novelas.databaseSQL.SQLiteHelper;
import com.example.feedback_5_optimizacion_aplicacion_novelas.domain.Novel;
import com.example.feedback_5_optimizacion_aplicacion_novelas.ui.fragments.NovelDetailFragment;
import com.example.feedback_5_optimizacion_aplicacion_novelas.ui.fragments.NovelListFragment;
import com.example.feedback_5_optimizacion_aplicacion_novelas.widget.NovelWidgetProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PantallaPrincipalActivity extends AppCompatActivity implements NovelListFragment.OnNovelSelectedListener {

    private DrawerLayout drawerLayout;
    private FirebaseFirestore db;
    private LinearLayout novelsLayout;
    private LinearLayout favoritesLayout;
    private List<Novel> novelList;
    private ExecutorService executorService;
    private SQLiteHelper sqliteHelper;

    private static final int ADD_NOVEL_REQUEST_CODE = 1; // Un código de solicitud único para identificar la actividad de agregar novela


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadThemePreference();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        drawerLayout = findViewById(R.id.drawer_layout);
        novelsLayout = findViewById(R.id.novels_layout);
        favoritesLayout = findViewById(R.id.favorites_layout);
        novelList = new ArrayList<>();
        sqliteHelper = new SQLiteHelper(this);
        executorService = Executors.newSingleThreadExecutor();

        // Configuración de la imagen de la pantalla principal
        ImageView imageView = findViewById(R.id.home_image);
        imageView.setImageResource(R.drawable.libros);

        // Botón para abrir el menú lateral
        Button openMenuButton = findViewById(R.id.open_menu_button);
        openMenuButton.setOnClickListener(v -> drawerLayout.openDrawer(findViewById(R.id.menu_layout)));

        setupNavigation();
        loadNovelsFromFirebase();
        loadFavoriteNovelsFromFirebase(); // Cargar favoritos también
    }

    public void refreshFavoritesList() {
        // Cargar y mostrar favoritos actualizados
        loadFavoriteNovelsFromFirebase();
        updateWidget();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNovelsFromFirebase(); // Asegura que se carguen todas las novelas
        loadFavoriteNovelsFromFirebase(); // Asegura que los favoritos también se recarguen
        updateWidget(); // Actualiza el widget cada vez que se regresa a la actividad
    }

    private void loadThemePreference() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }

    private void setupNavigation() {
        TextView navAddNovel = findViewById(R.id.nav_add_novel);
        navAddNovel.setOnClickListener(v -> {
            Intent intent = new Intent(PantallaPrincipalActivity.this, AddNovelActivity.class);
            startActivityForResult(intent, ADD_NOVEL_REQUEST_CODE); // Cambiado para recibir el resultado
            drawerLayout.closeDrawers();
        });

        TextView navViewFavorites = findViewById(R.id.nav_view_favorites);
        navViewFavorites.setOnClickListener(v -> {
            Intent intent = new Intent(PantallaPrincipalActivity.this, FavoritesActivity.class);
            startActivity(intent);
            drawerLayout.closeDrawers();
        });

        TextView navViewReviews = findViewById(R.id.nav_view_reviews);
        navViewReviews.setOnClickListener(v -> {
            Intent intent = new Intent(PantallaPrincipalActivity.this, ReviewActivity.class);
            startActivity(intent);
            drawerLayout.closeDrawers();
        });

        TextView navSettings = findViewById(R.id.nav_settings);
        navSettings.setOnClickListener(v -> {
            Intent intent = new Intent(PantallaPrincipalActivity.this, SettingsActivity.class);
            startActivity(intent);
            drawerLayout.closeDrawers();
        });
    }

    private void loadNovelsFromFirebase() {
        db.collection("novelas").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                novelList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Novel novel = document.toObject(Novel.class);
                    novel.setId(document.getId());
                    novelList.add(novel);
                    sqliteHelper.addNovel(novel);
                }
                displayNovels(novelList);
            }
        });
    }

    private void updateWidget() {
        Intent intent = new Intent(this, NovelWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

        // Obtener IDs de los widgets activos
        int[] ids = AppWidgetManager.getInstance(getApplication())
                .getAppWidgetIds(new ComponentName(getApplication(), NovelWidgetProvider.class));

        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent);
    }

    private void loadFavoriteNovelsFromFirebase() {
        db.collection("novelas")
                .whereEqualTo("favorite", true)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Novel> favoriteNovels = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Novel novel = document.toObject(Novel.class);
                            novel.setId(document.getId());
                            favoriteNovels.add(novel);
                        }
                        displayFavorites(favoriteNovels);
                    }
                });
    }

    private void displayNovels(List<Novel> novels) {
        novelsLayout.removeAllViews();
        for (Novel novel : novels) {
            displayNovel(novel, novelsLayout);
        }
    }

    private void displayFavorites(List<Novel> favoriteNovels) {
        favoritesLayout.removeAllViews();
        for (Novel novel : favoriteNovels) {
            displayNovel(novel, favoritesLayout);
        }
    }

    private void displayNovel(Novel novel, LinearLayout layout) {
        // Crear el CardView para la novela
        CardView cardView = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 8, 0, 8); // Margen para cada tarjeta
        cardView.setLayoutParams(cardParams);
        cardView.setPadding(16, 16, 16, 16);
        cardView.setCardElevation(4);
        cardView.setBackgroundColor(getResources().getColor(R.color.card_background_color)); // Fondo del CardView

        LinearLayout cardLayout = new LinearLayout(this);
        cardLayout.setOrientation(LinearLayout.VERTICAL);

        // Título de la novela
        TextView novelTitle = new TextView(this);
        novelTitle.setText(novel.getTitle());
        novelTitle.setTextSize(18);
        novelTitle.setTextColor(getResources().getColor(R.color.primary_text_color));
        novelTitle.setTypeface(novelTitle.getTypeface(), Typeface.BOLD); // Negrita para el título
        novelTitle.setOnClickListener(v -> onNovelSelected(novel));

        // Autor de la novela
        TextView novelAuthor = new TextView(this);
        novelAuthor.setText("Autor: " + novel.getAuthor());
        novelAuthor.setTextSize(14);
        novelAuthor.setTextColor(getResources().getColor(R.color.secondary_text_color));

        // Agregar los elementos al layout interno del CardView
        cardLayout.addView(novelTitle);
        cardLayout.addView(novelAuthor);
        cardView.addView(cardLayout);

        // Añadir el CardView al layout de novelas
        layout.addView(cardView);

        // Crear un separador
        View separator = new View(this);
        LinearLayout.LayoutParams separatorParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                2); // Altura de 2 píxeles para que sea consistente
        separatorParams.setMargins(0, 0, 0, 8); // Espacio debajo del separador
        separator.setLayoutParams(separatorParams);
        separator.setBackgroundColor(getResources().getColor(R.color.divider_color)); // Color sólido para el separador
        layout.addView(separator);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_NOVEL_REQUEST_CODE && resultCode == RESULT_OK) {
            loadNovelsFromFirebase(); // Recargar novelas desde Firebase
            updateWidget(); // Actualizar el widget después de agregar una novela
        }
    }

    public void onNovelSelected(Novel novel) {
        NovelDetailFragment detailFragment = new NovelDetailFragment();
        Bundle args = new Bundle();
        args.putString("novelId", novel.getId());
        detailFragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, detailFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }
}
