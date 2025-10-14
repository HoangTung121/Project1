package com.example.myreadbookapplication.model;

import java.util.List;

public class FavoritesResponse {
    private List<Book> favoriteBooks;
    private List<String> favoriteBookIds;

    public List<Book> getFavoriteBooks() {
        return favoriteBooks;
    }

    public void setFavoriteBooks(List<Book> favoriteBooks) {
        this.favoriteBooks = favoriteBooks;
    }

    public List<String> getFavoriteBookIds() {
        return favoriteBookIds;
    }

    public void setFavoriteBookIds(List<String> favoriteBookIds) {
        this.favoriteBookIds = favoriteBookIds;
    }
}


