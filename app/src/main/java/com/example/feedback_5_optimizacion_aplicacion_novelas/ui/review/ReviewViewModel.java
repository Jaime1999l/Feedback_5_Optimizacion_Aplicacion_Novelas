package com.example.feedback_5_optimizacion_aplicacion_novelas.ui.review;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.feedback_5_optimizacion_aplicacion_novelas.domain.Novel;
import com.example.feedback_5_optimizacion_aplicacion_novelas.domain.Review;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ReviewViewModel extends AndroidViewModel {

    private final FirebaseFirestore db;
    private final MutableLiveData<List<Review>> reviewsLiveData;

    public ReviewViewModel(@NonNull Application application) {
        super(application);
        db = FirebaseFirestore.getInstance();
        reviewsLiveData = new MutableLiveData<>();
    }

    // Obtener todas las reseñas
    public LiveData<List<Review>> getAllReviews() {
        MutableLiveData<List<Review>> liveData = new MutableLiveData<>();
        db.collection("reviews").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Review> reviewList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Review review = document.toObject(Review.class);
                            review.setId(document.getId());
                            reviewList.add(review);
                        }
                        liveData.setValue(reviewList);
                    }
                });
        return liveData;
    }

    public LiveData<Novel> getNovelById(String novelId) {
        MutableLiveData<Novel> liveData = new MutableLiveData<>();
        db.collection("novelas")
                .document(novelId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Novel novel = task.getResult().toObject(Novel.class);
                        liveData.setValue(novel);
                    }
                });
        return liveData;
    }



    public void addReview(Review review) {
        db.collection("reviews").add(review)
                .addOnSuccessListener(documentReference -> {
                    review.setId(documentReference.getId()); // Aquí se asigna el ID del documento en Firebase
                    db.collection("reviews").document(review.getId()).set(review);
                })
                .addOnFailureListener(e -> {
                });
    }

}



