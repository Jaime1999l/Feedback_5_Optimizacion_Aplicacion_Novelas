package com.example.feedback_5_optimizacion_aplicacion_novelas;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.example.feedback_5_optimizacion_aplicacion_novelas.domain.Novel;
import com.example.feedback_5_optimizacion_aplicacion_novelas.ui.fragments.NovelDetailFragment;
import com.example.feedback_5_optimizacion_aplicacion_novelas.ui.fragments.NovelListFragment;
import com.example.feedback_5_optimizacion_aplicacion_novelas.ui.mainNovel.NovelAdapter;
import com.example.feedback_5_optimizacion_aplicacion_novelas.ui.mainNovel.NovelViewModel;
import com.example.feedback_5_optimizacion_aplicacion_novelas.widget.NovelWidgetProvider;

import java.util.ArrayList;
import java.util.List;

public class PantallaPrincipalActivity extends AppCompatActivity implements NovelListFragment.OnNovelSelectedListener {

    private DrawerLayout drawerLayout;
    private RecyclerView recyclerViewNovels;
    private RecyclerView recyclerViewFavorites;
    private NovelAdapter novelAdapter;
    private NovelAdapter favoriteAdapter;
    private NovelViewModel novelViewModel;

    private boolean isLowBattery = false;
    private BroadcastReceiver batteryReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadThemePreference();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        novelViewModel = new ViewModelProvider(this).get(NovelViewModel.class);

        // Configuración inicial
        drawerLayout = findViewById(R.id.drawer_layout);
        recyclerViewNovels = findViewById(R.id.recyclerViewNovels);
        recyclerViewFavorites = findViewById(R.id.recyclerViewFavorites);

        // Configuración de la imagen de la pantalla principal
        ImageView imageView = findViewById(R.id.home_image);
        imageView.setImageResource(R.drawable.libros);

        // Botón para abrir el menú lateral
        Button openMenuButton = findViewById(R.id.open_menu_button);
        openMenuButton.setOnClickListener(v -> drawerLayout.openDrawer(findViewById(R.id.menu_layout)));

        setupNavigation();

        // Configurar RecyclerView y Adaptadores
        setupRecyclerViews();

        // Monitorear el estado de la batería
        monitorBatteryState();
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
        }, this);

        recyclerViewNovels.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewNovels.setAdapter(novelAdapter);

        // Observa los datos de las novelas desde el ViewModel
        novelViewModel.getAllNovels().observe(this, novels -> novelAdapter.setNovels(novels));

        // Configuración del RecyclerView para favoritos
        favoriteAdapter = new NovelAdapter(new NovelAdapter.OnNovelClickListener() {
            @Override
            public void onNovelClick(Novel novel) {
                onNovelSelected(novel);
            }

            @Override
            public void onFavoriteClick(Novel novel) {
                novel.setFavorite(!novel.isFavorite());
                novelViewModel.updateFavoriteStatus(novel);
                refreshFavoritesList();
            }

            @Override
            public void onReviewClick(Novel novel) {
                Intent intent = new Intent(PantallaPrincipalActivity.this, ReviewActivity.class);
                intent.putExtra("EXTRA_NOVEL_ID", novel.getId());
                startActivity(intent);
            }
        }, this);

        recyclerViewFavorites.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewFavorites.setAdapter(favoriteAdapter);

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
            startActivity(intent);
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

    private void monitorBatteryState() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        batteryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                float batteryPct = level * 100 / (float) scale;
                isLowBattery = batteryPct < 20; // Consideramos batería baja si está por debajo del 20%
            }
        };
        registerReceiver(batteryReceiver, filter);
    }

    public void refreshFavoritesList() {
        novelViewModel.getAllNovels().observe(this, novels -> {
            List<Novel> favoriteNovels = new ArrayList<>();
            for (Novel novel : novels) {
                if (novel.isFavorite()) {
                    favoriteNovels.add(novel);
                }
            }
            favoriteAdapter.setNovels(favoriteNovels);
            updateWidget();
        });
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
        if (batteryReceiver != null) {
            unregisterReceiver(batteryReceiver);
        }
    }
}
