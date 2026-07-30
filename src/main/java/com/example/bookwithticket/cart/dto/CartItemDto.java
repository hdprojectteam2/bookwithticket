package com.example.bookwithticket.cart.dto;

import com.example.bookwithticket.book.entity.BookEntity;
import com.example.bookwithticket.cart.entity.CartItemEntity;

public class CartItemDto {

    private final Long cartItemId;
    private final Long bookId;

    private final String bookTitle;
    private final String author;
    private final String publisher;
    private final String thumbnailUrl;

    private final int price;
    private final int quantity;
    private final int stock;
    private final int maxPurchaseQty;
    private final int totalPrice;

    private final boolean purchasable;
    private final String unavailableReason;

    public CartItemDto(CartItemEntity cartItem) {
        BookEntity book = cartItem.getBook();

        this.cartItemId = cartItem.getId();
        this.bookId = book.getId();

        this.bookTitle = book.getTitle();
        this.author = book.getAuthor();
        this.publisher = book.getPublisher();
        this.thumbnailUrl = book.getThumbnailUrl();

        this.price = book.getSalePrice();
        this.quantity = cartItem.getQuantity();
        this.stock = book.getStock();
        this.maxPurchaseQty = book.getMaxPurchaseQty();
        this.totalPrice = price * quantity;

        if (book.isDeleted()) {
            this.purchasable = false;
            this.unavailableReason =
                    "삭제된 상품입니다.";

        } else if (!book.isActive()) {
            this.purchasable = false;
            this.unavailableReason =
                    "판매가 중지된 상품입니다.";

        } else if (!"ON_SALE".equals(book.getSaleStatus()) || book.getStock() <= 0) {
            this.purchasable = false;
            this.unavailableReason =
                    "품절된 상품입니다.";

        } else if (quantity > book.getStock()) {
            this.purchasable = false;
            this.unavailableReason = "재고가 부족합니다. 현재 재고는 " + book.getStock() + "개입니다.";

        } else {
            this.purchasable = true;
            this.unavailableReason = null;
        }

    }

    public Long getCartItemId() {
        return cartItemId;
    }

    public Long getBookId() {
        return bookId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public String getAuthor() {
        return author;
    }

    public String getPublisher() {
        return publisher;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public int getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getStock() {
        return stock;
    }

    public int getMaxPurchaseQty() {
        return maxPurchaseQty;
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public boolean isPurchasable() {
        return purchasable;
    }

    public String getUnavailableReason() {
        return unavailableReason;
    }


}