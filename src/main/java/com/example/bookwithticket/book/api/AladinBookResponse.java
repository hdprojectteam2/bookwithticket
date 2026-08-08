package com.example.bookwithticket.book.api;


import java.util.List;


public class AladinBookResponse {


    private List<Item> item;


    public List<Item> getItem() {
        return item;
    }


    public void setItem(List<Item> item) {
        this.item = item;
    }



    public static class Item {


        private String isbn13;

        private String title;

        private String author;

        private String publisher;

        private int priceStandard;

        private String cover;

        private String description;



        public String getIsbn13() {
            return isbn13;
        }


        public void setIsbn13(String isbn13) {
            this.isbn13 = isbn13;
        }


        public String getTitle() {
            return title;
        }


        public void setTitle(String title) {
            this.title = title;
        }


        public String getAuthor() {
            return author;
        }


        public void setAuthor(String author) {
            this.author = author;
        }


        public String getPublisher() {
            return publisher;
        }


        public void setPublisher(String publisher) {
            this.publisher = publisher;
        }


        public int getPriceStandard() {
            return priceStandard;
        }


        public void setPriceStandard(int priceStandard) {
            this.priceStandard = priceStandard;
        }


        public String getCover() {
            return cover;
        }


        public void setCover(String cover) {
            this.cover = cover;
        }


        public String getDescription() {
            return description;
        }


        public void setDescription(String description) {
            this.description = description;
        }
    }
}