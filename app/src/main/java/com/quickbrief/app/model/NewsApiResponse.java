package com.quickbrief.app.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class NewsApiResponse {
    @SerializedName("status")
    private String status;

    @SerializedName("totalResults")
    private int totalResults;

    @SerializedName("results")
    private List<Article> results;

    @SerializedName("nextPage")
    private String nextPage;

    public String getStatus() {
        return status;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public List<Article> getResults() {
        return results;
    }

    public String getNextPage() {
        return nextPage;
    }

    public static class Article {
        @SerializedName("source")
        private Source source;

        @SerializedName("author")
        private String author;

        @SerializedName("title")
        private String title;

        @SerializedName("description")
        private String description;

        @SerializedName("link")
        private String url;

        @SerializedName("url")
        private String urlFallback;

        @SerializedName("image_url")
        private String image_url;

        @SerializedName("pubDate")
        private String pubDate;

        @SerializedName("source_id")
        private String sourceId;

        @SerializedName("content")
        private String content;

        public Source getSource() {
            return source;
        }

        public String getAuthor() {
            return author;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public String getUrl() {
            return url != null ? url : urlFallback;
        }

        public String getImageUrl() {
            return image_url;
        }

        public String getPubDate() {
            return pubDate;
        }

        public String getSourceId() {
            return sourceId;
        }

        public String getContent() {
            return content;
        }
    }

    public static class Source {
        @SerializedName("id")
        private String id;

        @SerializedName("name")
        private String name;

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }
} 