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
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.feedback_5_optimizacion_aplicacion_novelas.activity.AddNovelActivity;
import com.example.feedback_5_optimizacion_aplicacion_novelas.activity.FavoritesActivity;
import com.example.feedback_5_optimizacion_aplicacion_novelas.activity.ReviewActivity;
import com.example.feedback_5_optimizacion_aplicacion_novelas.activity.SettingsActivity;
import com.example.feedback_5_optimizacion_aplicacion_novelas.databaseSQL.SQLiteHelper;
import com.example.feedback_5_optimizacion_aplicacion_novelas.domain.Novel;
import com.example.feedback_5_optimizacion_aplicacion_novelas.ui.fragments.NovelDetailFragment;
import com.example.feedback_5_optimizacion_aplicacion_novelas.ui.fragments.NovelListFragment;
import com.example.feedback_5_optimizacion_aplicacion_novelas.ui.mainNovel.NovelAdapter;
import com.example.feedback_5_optimizacion_aplicacion_novelas.ui.mainNovel.NovelViewModel;
import com.example.feedback_5_optimizacion_aplicacion_novelas.widget.NovelWidgetProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PantallaPrincipalActivity extends AppCompatActivity implements NovelListFragment.OnNovelSelectedListener {

    private DrawerLayout drawerLayout;
    private FirebaseFirestore db;
    private RecyclerView recyclerViewNovels;
    private RecyclerView recyclerViewFavorites;
    private List<Novel> novelList;
    private List<Novel> favoriteNovels;
    private NovelAdapter novelAdapter;
    private NovelAdapter favoriteAdapter;
    private ExecutorService executorService;
    private SQLiteHelper sqliteHelper;
    private NovelViewModel novelViewModel;


    private static final int ADD_NOVEL_REQUEST_CODE = 1; // Código único para agregar novela

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadThemePreference();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        novelViewModel = new ViewModelProvider(this).get(NovelViewModel.class);


        // Configuración inicial
        db = FirebaseFirestore.getInstance();
        drawerLayout = findViewById(R.id.drawer_layout);
        recyclerViewNovels = findViewById(R.id.recyclerViewNovels);
        recyclerViewFavorites = findViewById(R.id.recyclerViewFavorites);
        sqliteHelper = new SQLiteHelper(this);
        executorService = Executors.newSingleThreadExecutor();
        novelList = new ArrayList<>();
        favoriteNovels = new ArrayList<>();

        // Configuración de la imagen de la pantalla principal
        ImageView imageView = findViewById(R.id.home_image);
        imageView.setImageResource(R.drawable.libros);

        // Botón para abrir el menú lateral
        Button openMenuButton = findViewById(R.id.open_menu_button);
        openMenuButton.setOnClickListener(v -> drawerLayout.openDrawer(findViewById(R.id.menu_layout)));

        setupNavigation();

        // Configurar RecyclerView y Adaptadores
        setupRecyclerViews();

        // Cargar datos
        loadNovelsFromFirebase();
        loadFavoriteNovelsFromFirebase();
    }

    private void setupRecyclerViews() {
        // Configuración del RecyclerView para todas las novelas
        novelAdapter = new NovelAdapter(new NovelAdapter.OnNovelClickListener() {
            @Override
            public void onNovelClick(Novel novel) {
                onNovelSelected(novel);
            }

            @Override
            public void onFavoriteClick(Novel novel) {
                // Manejar el cambio de estado de favorito
                novel.setFavorite(!novel.isFavorite());
                // Actualizar en Firestore usando el ViewModel
                novelViewModel.updateFavoriteStatus(novel);
                refreshFavoritesList();
            }

            @Override
            public void onReviewClick(Novel novel) {
                // Navegar a la pantalla de reseñas
                Intent intent = new Intent(PantallaPrincipalActivity.this, ReviewActivity.class);
                intent.putExtra("EXTRA_NOVEL_ID", novel.getId());
                startActivity(intent);
            }
        }, this); // 'this' pasa el contexto actual

        recyclerViewNovels.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewNovels.setAdapter(novelAdapter);

        // Observa los datos de las novelas desde el ViewModel
        novelViewModel.getAllNovels().observe(this, novels -> {
            novelAdapter.setNovels(novels);
        });

        // Configuración del RecyclerView para favoritos
        favoriteAdapter = new NovelAdapter(new NovelAdapter.OnNovelClickListener() {
            @Override
            public void onNovelClick(Novel novel) {
                onNovelSelected(novel);
            }

            @Override
            public void onFavoriteClick(Novel novel) {
                // Manejar el cambio de estado de favorito
                novel.setFavorite(!novel.isFavorite());
                // Actualizar en Firestore usando el ViewModel
                novelViewModel.updateFavoriteStatus(novel);
                refreshFavoritesList();
            }

            @Override
            public void onReviewClick(Novel novel) {
                // Navegar a la pantalla de reseñas
                Intent intent = new Intent(PantallaPrincipalActivity.this, ReviewActivity.class);
                intent.putExtra("EXTRA_NOVEL_ID", novel.getId());
                startActivity(intent);
            }
        }, this); // 'this' pasa el contexto actual

        recyclerViewFavorites.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewFavorites.setAdapter(favoriteAdapter);

        // Observa los datos de las novelas favoritas desde el ViewModel
        novelViewModel.getAllNovels().observe(this, novels -> {
            List<Novel> favoriteNovels = new ArrayList<>();
            for (Novel novel : novels) {
                if (novel.isFavorite()) {
                    favoriteNovels.add(novel);
                }
            }
            favoriteAdapter.setNovels(favoriteNovels);
        });
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
            startActivityForResult(intent, ADD_NOVEL_REQUEST_CODE);
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
        db.collection("novelas").addSnapshotListener((snapshots, e) -> {
            if (e != null || snapshots == null) return;
            novelList.clear();
            for (QueryDocumentSnapshot document : snapshots) {
                Novel novel = document.toObject(Novel.class);
                novel.setId(document.getId());
                novelList.add(novel);
                sqliteHelper.addNovel(novel);
            }
            novelAdapter.setNovels(novelList);
        });
    }

    private void loadFavoriteNovelsFromFirebase() {
        db.collection("novelas").whereEqualTo("favorite", true).addSnapshotListener((snapshots, e) -> {
            if (e != null || snapshots == null) return;
            favoriteNovels.clear();
            for (QueryDocumentSnapshot document : snapshots) {
                Novel novel = document.toObject(Novel.class);
                novel.setId(document.getId());
                favoriteNovels.add(novel);
            }
            favoriteAdapter.setNovels(favoriteNovels);
        });
    }

    public void refreshFavoritesList() {
        loadFavoriteNovelsFromFirebase();
        updateWidget();
    }

    private void updateWidget() {
        Intent intent = new Intent(this, NovelWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = AppWidgetManager.getInstance(getApplication())
                .getAppWidgetIds(new ComponentName(getApplication(), NovelWidgetProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent);
    }

    @Override
    public void onNovelSelected(Novel novel) {
        NovelDetailFragment detailFragment = new NovelDetailFragment();

        // Pasar los datos de la novela seleccionada al fragmento
        Bundle args = new Bundle();
        args.putString("novelId", novel.getId());
        detailFragment.setArguments(args);

        // Realizar la transacción del fragmento
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
        novelList.clear();
        favoriteNovels.clear();
    }
}
