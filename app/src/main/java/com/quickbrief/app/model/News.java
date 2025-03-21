package com.quickbrief.app.model;

public class News {
    private String title;
    private String description;
    private String imageUrl;
    private String source;
    private String url;

    public News(String title, String description, String imageUrl, String source, String url) {
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.source = source;
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getSource() {
        return source;
    }

    public String getUrl() {
        return url;
    }
} 