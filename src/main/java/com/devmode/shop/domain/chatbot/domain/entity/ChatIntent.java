package com.devmode.shop.domain.chatbot.domain.entity;

public enum ChatIntent {
    SEARCH_PRODUCTS,      // 상품 검색
    GET_RECOMMENDATIONS,  // 상품 추천
    GET_TRENDS,          // 트렌드 조회
    COMPARE_PRICES,      // 가격 비교
    GET_FAVORITES,       // 즐겨찾기 조회
    GENERAL_QUESTION,    // 일반 질문
    UNKNOWN              // 의도 파악 불가
}
