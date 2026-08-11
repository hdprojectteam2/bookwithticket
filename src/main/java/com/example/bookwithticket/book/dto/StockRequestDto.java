package com.example.bookwithticket.book.dto;

import jakarta.validation.constraints.Min;

public class StockRequestDto {
    @Min(value = 0, message = "재고는 0 이상이어야 합니다.")
    private int stock;

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
}
