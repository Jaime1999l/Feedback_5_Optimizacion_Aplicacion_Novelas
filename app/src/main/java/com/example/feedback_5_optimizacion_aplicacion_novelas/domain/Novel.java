package com.example.feedback_5_optimizacion_aplicacion_novelas.domain;

public class Novel {
    private String id;
    private String title;
    private String author;
    private int year;
    private String synopsis;
    private String imageUri;
    private boolean favorite;

    public Novel() {
    }

    public Novel(String title, String author, int year, String synopsis, String imageUri) {
        this.title = title;
        this.author = author;
        this.year = year;
        this.synopsis = synopsis;
        this.imageUri = imageUri;
        this.favorite = false;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }
}

