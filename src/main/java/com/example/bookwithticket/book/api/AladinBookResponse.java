package com.example.bookwithticket.book.api;

import java.util.ArrayList;
import java.util.List;

public class AladinBookResponse {

    private int totalResults;
    private int startIndex;
    private int itemsPerPage;
    private List<Item> item = new ArrayList<>();

    public int getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getItemsPerPage() {
        return itemsPerPage;
    }

    public void setItemsPerPage(int itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
    }

    public List<Item> getItem() {
        return item;
    }

    public void setItem(List<Item> item) {
        this.item = item != null ? item : new ArrayList<>();
    }

    public static class Item {

        private String isbn13;
        private String title;
        private String author;
        private String publisher;
        private int priceStandard;
        private int priceSales;
        private String cover;
        private String description;
        private String pubDate;
        private Integer categoryId;
        private String categoryName;

        public String getIsbn13() { return isbn13; }
        public void setIsbn13(String isbn13) { this.isbn13 = isbn13; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }

        public String getPublisher() { return publisher; }
        public void setPublisher(String publisher) { this.publisher = publisher; }

        public int getPriceStandard() { return priceStandard; }
        public void setPriceStandard(int priceStandard) { this.priceStandard = priceStandard; }

        public int getPriceSales() { return priceSales; }
        public void setPriceSales(int priceSales) { this.priceSales = priceSales; }

        public String getCover() { return cover; }
        public void setCover(String cover) { this.cover = cover; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getPubDate() { return pubDate; }
        public void setPubDate(String pubDate) { this.pubDate = pubDate; }

        public Integer getCategoryId() { return categoryId; }
        public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    }
}
