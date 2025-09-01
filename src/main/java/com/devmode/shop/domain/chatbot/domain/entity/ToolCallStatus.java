package com.devmode.shop.domain.chatbot.domain.entity;

public enum ToolCallStatus {
    PENDING,    // 대기 중
    SUCCESS,    // 성공
    FAILED,     // 실패
    TIMEOUT     // 시간 초과
}
