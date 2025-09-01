package com.devmode.shop.domain.chatbot.domain.entity;

public enum ChatSessionStatus {
    ACTIVE,     // 활성 상태
    CLOSED,     // 사용자가 종료
    EXPIRED,    // 시간 만료
    SUSPENDED   // 일시 중단
}
