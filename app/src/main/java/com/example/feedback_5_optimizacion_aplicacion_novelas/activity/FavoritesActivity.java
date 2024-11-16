package com.example.feedback_5_optimizacion_aplicacion_novelas.activity;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.feedback_5_optimizacion_aplicacion_novelas.R;
import com.example.feedback_5_optimizacion_aplicacion_novelas.domain.Novel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FavoritesActivity extends AppCompatActivity {

    private LinearLayout favoritesLayout;
    private FirebaseFirestore firebaseFirestore;
    private ScheduledExecutorService scheduler;
    private BroadcastReceiver batteryReceiver;
    private boolean isLowBattery = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favorites_activity);

        favoritesLayout = findViewById(R.id.favorites_layout);
        firebaseFirestore = FirebaseFirestore.getInstance();

        // Monitorear el estado de la batería para ajustar la frecuencia de actualizaciones
        monitorBatteryState();

        // Cargar las novelas favoritas periódicamente
        schedulePeriodicFavoriteUpdates();

        // Realizar la carga inicial
        loadFavoriteNovelsFromFirebase();
    }

    private void loadFavoriteNovelsFromFirebase() {
        firebaseFirestore.collection("novelas")
                .whereEqualTo("favorite", true)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Novel> favoriteNovels = new ArrayList<>();
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        Novel novel = document.toObject(Novel.class);
                        if (novel != null) {
                            novel.setId(document.getId());
                            favoriteNovels.add(novel);
                        }
                    }
                    displayFavoriteNovels(favoriteNovels);
                })
                .addOnFailureListener(e -> {
                    // Manejar el error
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

    private void schedulePeriodicFavoriteUpdates() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        int updateInterval = isLowBattery ? 30 : 15; // Ajustar la frecuencia de actualización en minutos
        scheduler.scheduleAtFixedRate(this::loadFavoriteNovelsFromFirebase, 0, updateInterval, TimeUnit.MINUTES);
    }

    private void monitorBatteryState() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        batteryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                float batteryPct = level * 100 / (float) scale;

                boolean wasLowBattery = isLowBattery;
                isLowBattery = batteryPct < 20;

                if (wasLowBattery != isLowBattery && scheduler != null && !scheduler.isShutdown()) {
                    scheduler.shutdownNow();
                    schedulePeriodicFavoriteUpdates(); // Ajustar la frecuencia si el estado de la batería cambia
                }
            }
        };
        registerReceiver(batteryReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
        if (batteryReceiver != null) {
            unregisterReceiver(batteryReceiver);
        }
    }
}
