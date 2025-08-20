package com.devmode.shop.domain.product.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record NaverShoppingResponse(
    @JsonProperty("lastBuildDate")
    String lastBuildDate, // API 응답이 마지막으로 생성된 시간
    @JsonProperty("total")
    Integer total,
    @JsonProperty("start")
    Integer start,
    @JsonProperty("display")
    Integer display,
    @JsonProperty("items")
    List<NaverProductItem> items
) {
    public NaverShoppingResponse {
        // 기본값 설정
        total = (total != null && total >= 0) ? total : 0;
        start = (start != null && start >= 1) ? start : 1;
        display = (display != null && display > 0) ? display : 20;
        
        // null 리스트를 빈 리스트로 변환
        items = (items != null) ? items : List.of();
    }
    

}
