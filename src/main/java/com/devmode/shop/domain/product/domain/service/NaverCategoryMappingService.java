package com.devmode.shop.domain.product.domain.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 네이버 쇼핑 카테고리 매핑 서비스
 * 자연어로 표현된 카테고리를 네이버 쇼핑 API의 카테고리 코드로 변환
 */
@Service
public class NaverCategoryMappingService {
    
    private static final Logger log = LoggerFactory.getLogger(NaverCategoryMappingService.class);
    
    // 대분류 카테고리 매핑 (자연어 -> 네이버 카테고리 코드)
    private static final Map<String, String> CATEGORY_MAPPING = new HashMap<>();
    
    static {
        // 전자제품 관련
        CATEGORY_MAPPING.put("전자제품", "50000000");
        CATEGORY_MAPPING.put("전자기기", "50000000");
        CATEGORY_MAPPING.put("가전제품", "50000000");
        CATEGORY_MAPPING.put("컴퓨터", "50000000");
        CATEGORY_MAPPING.put("노트북", "50000000");
        CATEGORY_MAPPING.put("스마트폰", "50000000");
        CATEGORY_MAPPING.put("태블릿", "50000000");
        CATEGORY_MAPPING.put("가전", "50000000");
        
        // 패션의류 관련
        CATEGORY_MAPPING.put("패션의류", "50000001");
        CATEGORY_MAPPING.put("의류", "50000001");
        CATEGORY_MAPPING.put("옷", "50000001");
        CATEGORY_MAPPING.put("패션", "50000001");
        CATEGORY_MAPPING.put("남성의류", "50000001");
        CATEGORY_MAPPING.put("여성의류", "50000001");
        CATEGORY_MAPPING.put("아동의류", "50000001");
        
        // 뷰티 관련
        CATEGORY_MAPPING.put("뷰티", "50000002");
        CATEGORY_MAPPING.put("화장품", "50000002");
        CATEGORY_MAPPING.put("미용", "50000002");
        CATEGORY_MAPPING.put("스킨케어", "50000002");
        CATEGORY_MAPPING.put("메이크업", "50000002");
        
        // 생활용품 관련
        CATEGORY_MAPPING.put("생활용품", "50000003");
        CATEGORY_MAPPING.put("생활", "50000003");
        CATEGORY_MAPPING.put("생활잡화", "50000003");
        CATEGORY_MAPPING.put("잡화", "50000003");
        CATEGORY_MAPPING.put("주방용품", "50000003");
        CATEGORY_MAPPING.put("욕실용품", "50000003");
        CATEGORY_MAPPING.put("청소용품", "50000003");
        CATEGORY_MAPPING.put("문구", "50000003");
        CATEGORY_MAPPING.put("사무용품", "50000003");
        
        // 식품 관련
        CATEGORY_MAPPING.put("식품", "50000004");
        CATEGORY_MAPPING.put("음식", "50000004");
        CATEGORY_MAPPING.put("먹거리", "50000004");
        CATEGORY_MAPPING.put("음료", "50000004");
        CATEGORY_MAPPING.put("간식", "50000004");
        CATEGORY_MAPPING.put("건강식품", "50000004");
        CATEGORY_MAPPING.put("신선식품", "50000004");
        CATEGORY_MAPPING.put("가공식품", "50000004");
        CATEGORY_MAPPING.put("베이커리", "50000004");
        
        // 도서 관련
        CATEGORY_MAPPING.put("도서", "50000005");
        CATEGORY_MAPPING.put("책", "50000005");
        CATEGORY_MAPPING.put("서적", "50000005");
        CATEGORY_MAPPING.put("음반", "50000005");
        CATEGORY_MAPPING.put("DVD", "50000005");
        
        // 스포츠/레저 관련
        CATEGORY_MAPPING.put("스포츠", "50000006");
        CATEGORY_MAPPING.put("레저", "50000006");
        CATEGORY_MAPPING.put("운동", "50000006");
        CATEGORY_MAPPING.put("스포츠레저", "50000006");
        CATEGORY_MAPPING.put("아웃도어", "50000006");
        CATEGORY_MAPPING.put("캠핑", "50000006");
        CATEGORY_MAPPING.put("낚시", "50000006");
        CATEGORY_MAPPING.put("골프", "50000006");
        CATEGORY_MAPPING.put("자전거", "50000006");
        
        // 홈인테리어 관련
        CATEGORY_MAPPING.put("홈인테리어", "50000007");
        CATEGORY_MAPPING.put("인테리어", "50000007");
        CATEGORY_MAPPING.put("가구", "50000007");
        CATEGORY_MAPPING.put("홈", "50000007");
        CATEGORY_MAPPING.put("인테리어소품", "50000007");
        CATEGORY_MAPPING.put("침구", "50000007");
        CATEGORY_MAPPING.put("조명", "50000007");
        CATEGORY_MAPPING.put("커튼", "50000007");
        CATEGORY_MAPPING.put("블라인드", "50000007");
        CATEGORY_MAPPING.put("데코", "50000007");
        
        // 자동차 관련
        CATEGORY_MAPPING.put("자동차", "50000008");
        CATEGORY_MAPPING.put("차", "50000008");
        CATEGORY_MAPPING.put("자동차용품", "50000008");
        CATEGORY_MAPPING.put("오토바이", "50000008");
        
        // 유아동 관련
        CATEGORY_MAPPING.put("유아동", "50000009");
        CATEGORY_MAPPING.put("아기", "50000009");
        CATEGORY_MAPPING.put("유아", "50000009");
        CATEGORY_MAPPING.put("아동", "50000009");
        CATEGORY_MAPPING.put("육아", "50000009");
        CATEGORY_MAPPING.put("장난감", "50000009");
        CATEGORY_MAPPING.put("유모차", "50000009");
        CATEGORY_MAPPING.put("카시트", "50000009");
        CATEGORY_MAPPING.put("아동의류", "50000009");
        
        // 반려동물 관련
        CATEGORY_MAPPING.put("반려동물", "50000010");
        CATEGORY_MAPPING.put("펫", "50000010");
        CATEGORY_MAPPING.put("애완동물", "50000010");
        CATEGORY_MAPPING.put("강아지", "50000010");
        CATEGORY_MAPPING.put("고양이", "50000010");
        CATEGORY_MAPPING.put("반려동물용품", "50000010");
        CATEGORY_MAPPING.put("사료", "50000010");
        CATEGORY_MAPPING.put("간식", "50000010");
    }
    
    /**
     * 자연어 카테고리를 네이버 쇼핑 카테고리 코드로 변환
     * @param naturalCategory 자연어로 표현된 카테고리
     * @return 네이버 쇼핑 카테고리 코드, 매핑되지 않으면 null
     */
    public String mapToNaverCategory(String naturalCategory) {
        if (naturalCategory == null || naturalCategory.trim().isEmpty()) {
            return null;
        }
        
        String trimmedCategory = naturalCategory.trim();
        
        // 정확한 매칭 시도
        String mappedCategory = CATEGORY_MAPPING.get(trimmedCategory);
        if (mappedCategory != null) {
            log.debug("[카테고리 매핑] 정확 매칭: {} -> {}", trimmedCategory, mappedCategory);
            return mappedCategory;
        }
        
        // 부분 매칭 시도 (대소문자 무시)
        for (Map.Entry<String, String> entry : CATEGORY_MAPPING.entrySet()) {
            if (trimmedCategory.toLowerCase().contains(entry.getKey().toLowerCase()) ||
                entry.getKey().toLowerCase().contains(trimmedCategory.toLowerCase())) {
                log.debug("[카테고리 매핑] 부분 매칭: {} -> {} (키워드: {})", 
                         trimmedCategory, entry.getValue(), entry.getKey());
                return entry.getValue();
            }
        }
        
        log.debug("[카테고리 매핑] 매칭 실패: {}", trimmedCategory);
        return null;
    }
    
    /**
     * 카테고리가 유효한지 확인
     * @param category 카테고리 코드
     * @return 유효한 카테고리인지 여부
     */
    public boolean isValidCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return false;
        }
        
        return CATEGORY_MAPPING.containsValue(category.trim());
    }
    
    /**
     * 사용 가능한 모든 카테고리 코드 반환
     * @return 카테고리 코드 Set
     */
    public Set<String> getAllValidCategories() {
        return Set.copyOf(CATEGORY_MAPPING.values());
    }
    
    /**
     * 자연어 카테고리 목록 반환
     * @return 자연어 카테고리 Set
     */
    public Set<String> getAllNaturalCategories() {
        return CATEGORY_MAPPING.keySet();
    }
    
    /**
     * 카테고리 매핑 통계 정보 반환
     * @return 매핑 통계 정보
     */
    public CategoryMappingStats getMappingStats() {
        return new CategoryMappingStats(
            CATEGORY_MAPPING.size(),
            CATEGORY_MAPPING.values().stream().distinct().count()
        );
    }
    
    /**
     * 카테고리 매핑 통계
     */
    public record CategoryMappingStats(
        int totalMappings,
        long uniqueCategories
    ) {}
}
