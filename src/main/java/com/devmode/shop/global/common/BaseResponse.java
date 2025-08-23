package com.devmode.shop.global.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.LocalDateTime;

@JsonPropertyOrder({"timestamp", "code", "message", "result"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public record BaseResponse<T>(
    LocalDateTime timestamp,
    String code,
    String message,
    T result
) {
    
    // 기본 생성자 - timestamp 자동 설정
    public BaseResponse(String code, String message, T result) {
        this(LocalDateTime.now(), code, message, result);
    }
    
    public static <T> BaseResponse<T> onSuccess(T result) {
        return new BaseResponse<>("COMMON200", "요청에 성공하였습니다.", result);
    }
    
    public static BaseResponse<Void> onSuccess() {
        return new BaseResponse<>("COMMON200", "요청에 성공하였습니다.", null);
    }
    
    public static <T> BaseResponse<T> onFailure(String code, String message, T data) {
        return new BaseResponse<>(code, message, data);
    }
}