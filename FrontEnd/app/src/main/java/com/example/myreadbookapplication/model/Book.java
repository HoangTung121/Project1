package com.example.myreadbookapplication.model;

import com.google.gson.annotations.SerializedName;

public class Book {
    @SerializedName("_id")
    private int _id;
    private String title;
    private String author;
    private int category; // id cua category
    private String category_name; // tên category
    private String description;
    @SerializedName("release_date")
    private String releaseDate;
    private String cover_url; // dung cho imageUrl
    private String txt_url;
    private String book_url;
    private String epub_url;
    private String[] keywords; // array of keywords
    private String status; //is active or not
    private String createdAt; // timestamp
    private String updatedAt; // timestamp
    private double avgRating;
    private int numberOfReviews;

    //Constructor
    public Book(){}
    public Book(String title, String cover_url){
        this.title=title;
        this.cover_url=cover_url;
    }
    //getters and setters
    public int getId() { return _id; }
    public void setId(int _id) { this._id = _id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public int getCategory() { return category; }
    public void setCategory(int category) { this.category = category; }
    public String getCategoryName() { return category_name; }
    public void setCategoryName(String category_name) { this.category_name = category_name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getReleaseDate() { return releaseDate; }
    public void setReleaseDate(String release_date) { this.releaseDate = releaseDate; }
    public String getCoverUrl() { return cover_url; }
    public void setCoverUrl(String cover_url) { this.cover_url = cover_url; }
    public String getTxtUrl() { return txt_url; }
    public void setTxtUrl(String txt_url) { this.txt_url = txt_url; }
    public String getBookUrl() { return book_url; }
    public void setBookUrl(String book_url) { this.book_url = book_url; }
    public String getEpubUrl() { return epub_url; }
    public void setEpubUrl(String epub_url) { this.epub_url = epub_url; }
    public String[] getKeywords() { return keywords; }
    public void setKeywords(String[] keywords) { this.keywords = keywords; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    public double getAvgRating() { return avgRating; }
    public void setAvgRating(double avgRating) { this.avgRating = avgRating; }
    public int getNumberOfReviews() { return numberOfReviews; }
    public void setNumberOfReviews(int numberOfReviews) { this.numberOfReviews = numberOfReviews; }
}
