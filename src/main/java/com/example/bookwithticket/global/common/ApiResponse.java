package com.example.bookwithticket.global.common;

// 자동으로 생성자 getter 만듬 
public record ApiResponse<T>(boolean success, String message, T data) {
	 
	//static이라 앞쪽에 미리 <T> 선언해야함 
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "성공", data);
    }
    //메세지 필요하면
    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }
    //데이터 없이
    public static <T> ApiResponse<T> ok() {
        return new ApiResponse<>(true, "성공", null);
    }
    //실패시 
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
}
