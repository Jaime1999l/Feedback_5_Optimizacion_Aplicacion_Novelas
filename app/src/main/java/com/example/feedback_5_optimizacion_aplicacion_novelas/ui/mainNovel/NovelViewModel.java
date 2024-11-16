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
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class NovelViewModel extends AndroidViewModel {

    private final MutableLiveData<List<Novel>> novelListLiveData = new MutableLiveData<>();
    private final FirebaseFirestore db;
    private ListenerRegistration registration;

    public NovelViewModel(@NonNull Application application) {
        super(application);
        db = FirebaseFirestore.getInstance();
        loadNovels();
    }

    // Método para obtener todas las novelas
    public LiveData<List<Novel>> getAllNovels() {
        return novelListLiveData;
    }

    // Método para cargar novelas con una escucha en tiempo real
    private void loadNovels() {
        registration = db.collection("novelas").addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                return;
            }
            if (snapshots != null) {
                List<Novel> novelList = new ArrayList<>();
                for (QueryDocumentSnapshot document : snapshots) {
                    Novel novel = document.toObject(Novel.class);
                    if (novel != null) {
                        novel.setId(document.getId());
                        novelList.add(novel);
                    }
                }
                novelListLiveData.setValue(novelList);
            }
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
