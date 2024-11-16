package com.example.feedback_5_optimizacion_aplicacion_novelas.ui.mainNovel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.feedback_5_optimizacion_aplicacion_novelas.domain.Novel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class NovelViewModel extends AndroidViewModel {

    private final MutableLiveData<List<Novel>> novelListLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>();
    private final FirebaseFirestore db;
    private ListenerRegistration registration;

    private DocumentSnapshot lastVisible; // Último documento visible para paginación
    private boolean isLastPage = false; // Verifica si se cargó la última página

    public NovelViewModel(@NonNull Application application) {
        super(application);
        db = FirebaseFirestore.getInstance();
        loadNovels(); // Cargar los datos iniciales
    }

    // Método para obtener todas las novelas
    public LiveData<List<Novel>> getAllNovels() {
        return novelListLiveData;
    }

    // Método para verificar si los datos están cargando
    public LiveData<Boolean> isLoading() {
        return isLoadingLiveData;
    }

    // Método para cargar novelas con una escucha en tiempo real y optimización
    private void loadNovels() {
        isLoadingLiveData.setValue(true);
        registration = db.collection("novelas")
                .orderBy("title") // Ordena por título
                .limit(20) // Solo obtén las primeras 20 novelas
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        isLoadingLiveData.setValue(false);
                        return;
                    }

                    if (snapshots != null) {
                        List<Novel> novelList = new ArrayList<>();
                        for (DocumentSnapshot document : snapshots) {
                            Novel novel = document.toObject(Novel.class);
                            if (novel != null) {
                                novel.setId(document.getId());
                                novelList.add(novel);
                            }
                        }

                        // Almacena el último documento visible para paginación
                        if (!snapshots.isEmpty()) {
                            lastVisible = snapshots.getDocuments().get(snapshots.size() - 1);
                            isLastPage = snapshots.size() < 20; // Si se cargaron menos de 20, es la última página
                        }

                        novelListLiveData.setValue(novelList);
                        isLoadingLiveData.setValue(false);
                    }
                });
    }

    // Método para cargar más novelas (paginación)
    public void loadMoreNovels() {
        if (isLastPage || isLoadingLiveData.getValue() != null && isLoadingLiveData.getValue()) {
            return; // No cargues más si ya estás en la última página o si ya estás cargando
        }

        isLoadingLiveData.setValue(true);
        db.collection("novelas")
                .orderBy("title")
                .startAfter(lastVisible) // Comienza después del último documento visible
                .limit(20) // Obtén las siguientes 20 novelas
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Novel> currentList = novelListLiveData.getValue() != null ? novelListLiveData.getValue() : new ArrayList<>();
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        Novel novel = document.toObject(Novel.class);
                        if (novel != null) {
                            novel.setId(document.getId());
                            currentList.add(novel);
                        }
                    }

                    // Actualiza el último documento visible para la próxima carga
                    if (!querySnapshot.isEmpty()) {
                        lastVisible = querySnapshot.getDocuments().get(querySnapshot.size() - 1);
                        isLastPage = querySnapshot.size() < 20;
                    }

                    novelListLiveData.setValue(currentList);
                    isLoadingLiveData.setValue(false);
                });
    }

    // Método para obtener una novela por su ID
    public LiveData<Novel> getNovelById(String novelId) {
        MutableLiveData<Novel> novelLiveData = new MutableLiveData<>();
        db.collection("novelas").document(novelId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Novel novel = task.getResult().toObject(Novel.class);
                if (novel != null) {
                    novel.setId(task.getResult().getId());
                    novelLiveData.setValue(novel);
                }
            }
        });
        return novelLiveData;
    }

    // Método para actualizar el estado de favorito de una novela en Firestore
    public void updateFavoriteStatus(Novel novel) {
        db.collection("novelas").document(novel.getId()).update("favorite", novel.isFavorite())
                .addOnSuccessListener(aVoid -> {
                    // Opcional: Actualiza la lista en tiempo real si es necesario
                })
                .addOnFailureListener(e -> {
                });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (registration != null) {
            registration.remove();
        }
    }
}
