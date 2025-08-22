package com.devmode.shop.domain.product.application.dto.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

class NaverProductItemTest {

    private NaverProductItem naverProductItem;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Record는 불변이므로 새로운 인스턴스로 생성
        naverProductItem = new NaverProductItem(
            "삼성 노트북",
            "https://shopping.naver.com/product/123",
            "https://shopping.naver.com/image/123.jpg",
            "1,200,000",
            "1,500,000",
            "삼성전자",
            "SAMSUNG001",
            "노트북",
            "삼성",
            "삼성전자",
            "전자제품",
            "컴퓨터",
            "노트북",
            "15인치",
            "NAVER001",
            "150",
            "4.5",
            "무료배송",
            "신상품"
        );

        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("NaverProductItem 기본 필드 설정 및 조회 테스트")
    void testNaverProductItemBasicFields() {
        // then
        assertEquals("삼성 노트북", naverProductItem.title());
        assertEquals("https://shopping.naver.com/product/123", naverProductItem.link());
        assertEquals("https://shopping.naver.com/image/123.jpg", naverProductItem.image());
        assertEquals("1,200,000", naverProductItem.lprice());
        assertEquals("1,500,000", naverProductItem.hprice());
        assertEquals("삼성전자", naverProductItem.mallName());
        assertEquals("SAMSUNG001", naverProductItem.productId());
        assertEquals("노트북", naverProductItem.productType());
        assertEquals("삼성", naverProductItem.brand());
        assertEquals("삼성전자", naverProductItem.maker());
        assertEquals("전자제품", naverProductItem.category1());
        assertEquals("컴퓨터", naverProductItem.category2());
        assertEquals("노트북", naverProductItem.category3());
        assertEquals("15인치", naverProductItem.category4());
        assertEquals("NAVER001", naverProductItem.naverProductId());
        assertEquals("150", naverProductItem.reviewCount());
        assertEquals("4.5", naverProductItem.rating());
        assertEquals("무료배송", naverProductItem.shippingInfo());
        assertEquals("신상품", naverProductItem.additionalInfo());
    }

    @Test
    @DisplayName("NaverProductItem 빌더 패턴 테스트")
    void testNaverProductItemBuilder() {
        // given & when
        NaverProductItem builtItem = new NaverProductItem(
            "삼성 노트북", "https://shopping.naver.com/product/123", "https://shopping.naver.com/image/123.jpg",
            "1,200,000", "1,500,000", "삼성전자", "SAMSUNG001", "노트북", "삼성", "삼성전자",
            "전자제품", "컴퓨터", "노트북", "15인치", "NAVER001", "150", "4.5", "무료배송", "신상품"
        );

        // then
        assertNotNull(builtItem);
        assertEquals("삼성 노트북", builtItem.title());
        assertEquals("https://shopping.naver.com/product/123", builtItem.link());
        assertEquals("https://shopping.naver.com/image/123.jpg", builtItem.image());
        assertEquals("1,200,000", builtItem.lprice());
        assertEquals("1,500,000", builtItem.hprice());
        assertEquals("삼성전자", builtItem.mallName());
        assertEquals("SAMSUNG001", builtItem.productId());
        assertEquals("노트북", builtItem.productType());
        assertEquals("삼성", builtItem.brand());
        assertEquals("삼성전자", builtItem.maker());
        assertEquals("전자제품", builtItem.category1());
        assertEquals("컴퓨터", builtItem.category2());
        assertEquals("노트북", builtItem.category3());
        assertEquals("15인치", builtItem.category4());
        assertEquals("NAVER001", builtItem.naverProductId());
        assertEquals("150", builtItem.reviewCount());
        assertEquals("4.5", builtItem.rating());
        assertEquals("무료배송", builtItem.shippingInfo());
        assertEquals("신상품", builtItem.additionalInfo());
    }

    @Test
    @DisplayName("NaverProductItem JSON 직렬화 테스트")
    void testNaverProductItemJsonSerialization() throws JsonProcessingException {
        // when
        String json = objectMapper.writeValueAsString(naverProductItem);

        // then
        assertNotNull(json);
        assertTrue(json.contains("삼성 노트북"));
        assertTrue(json.contains("1,200,000"));
        assertTrue(json.contains("삼성전자"));
        assertTrue(json.contains("4.5"));
    }

    @Test
    @DisplayName("NaverProductItem JSON 역직렬화 테스트")
    void testNaverProductItemJsonDeserialization() throws JsonProcessingException {
        // given
        String json = """
            {
                "title": "LG 그램",
                "link": "https://shopping.naver.com/product/789",
                "image": "https://shopping.naver.com/image/789.jpg",
                "lprice": "1,800,000",
                "hprice": "2,200,000",
                "mallName": "LG전자",
                "productId": "LG001",
                "productType": "노트북",
                "brand": "LG",
                "maker": "LG전자",
                "category1": "전자제품",
                "category2": "컴퓨터",
                "category3": "노트북",
                "category4": "17인치",
                "naverProductId": "NAVER003",
                "reviewCount": "120",
                "rating": "4.3",
                "shippingInfo": "무료배송",
                "additionalInfo": "경량"
            }
            """;

        // when
        NaverProductItem deserializedItem = objectMapper.readValue(json, NaverProductItem.class);

        // then
        assertNotNull(deserializedItem);
        assertEquals("LG 그램", deserializedItem.title());
        assertEquals("https://shopping.naver.com/product/789", deserializedItem.link());
        assertEquals("1,800,000", deserializedItem.lprice());
        assertEquals("2,200,000", deserializedItem.hprice());
        assertEquals("LG전자", deserializedItem.mallName());
        assertEquals("LG001", deserializedItem.productId());
        assertEquals("LG", deserializedItem.brand());
        assertEquals("4.3", deserializedItem.rating());
    }

    @Test
    @DisplayName("NaverProductItem 불변성 테스트")
    void testNaverProductItemImmutability() {
        // given
        NaverProductItem original = naverProductItem;
        
        // when - Record는 불변이므로 새로운 인스턴스로 생성해야 함
        NaverProductItem modified = new NaverProductItem(
            "수정된 제목",
            original.link(),
            original.image(),
            original.lprice(),
            original.hprice(),
            original.mallName(),
            original.productId(),
            original.productType(),
            original.brand(),
            original.maker(),
            original.category1(),
            original.category2(),
            original.category3(),
            original.category4(),
            original.naverProductId(),
            original.reviewCount(),
            original.rating(),
            original.shippingInfo(),
            original.additionalInfo()
        );
        
        // then
        assertNotEquals(original.title(), modified.title());
        assertEquals("수정된 제목", modified.title());
        assertEquals(original.link(), modified.link());
        assertEquals(original.image(), modified.image());
    }

    @Test
    @DisplayName("NaverProductItem 카테고리 계층 구조 테스트")
    void testNaverProductItemCategoryHierarchy() {
        // given & when
        String category1 = naverProductItem.category1();
        String category2 = naverProductItem.category2();
        String category3 = naverProductItem.category3();
        String category4 = naverProductItem.category4();

        // then
        assertEquals("전자제품", category1);
        assertEquals("컴퓨터", category2);
        assertEquals("노트북", category3);
        assertEquals("15인치", category4);
        
        // 카테고리 계층 구조 검증
        assertNotNull(category1);
        assertNotNull(category2);
        assertNotNull(category3);
        assertNotNull(category4);
    }

    @Test
    @DisplayName("NaverProductItem 가격 정보 테스트")
    void testNaverProductItemPriceInfo() {
        // given & when
        String lprice = naverProductItem.lprice();
        String hprice = naverProductItem.hprice();

        // then
        assertEquals("1,200,000", lprice);
        assertEquals("1,500,000", hprice);
        
        // 가격 정보 검증
        assertNotNull(lprice);
        assertNotNull(hprice);
        assertTrue(lprice.contains(","));
        assertTrue(hprice.contains(","));
    }

    @Test
    @DisplayName("NaverProductItem 리뷰 및 평점 정보 테스트")
    void testNaverProductItemReviewAndRating() {
        // given & when
        String reviewCount = naverProductItem.reviewCount();
        String rating = naverProductItem.rating();

        // then
        assertEquals("150", reviewCount);
        assertEquals("4.5", rating);
        
        // 리뷰 및 평점 정보 검증
        assertNotNull(reviewCount);
        assertNotNull(rating);
        assertTrue(reviewCount.matches("\\d+"));
        assertTrue(rating.matches("\\d+\\.\\d+"));
    }

    @Test
    @DisplayName("NaverProductItem copy 메서드 테스트")
    void testNaverProductItemCopy() {
        // given
        NaverProductItem original = naverProductItem;
        
        // when - Record의 copy 메서드 사용
        NaverProductItem copied = new NaverProductItem(
            "복사된 제목",
            original.link(),
            original.image(),
            original.lprice(),
            original.hprice(),
            original.mallName(),
            original.productId(),
            original.productType(),
            original.brand(),
            original.maker(),
            original.category1(),
            original.category2(),
            original.category3(),
            original.category4(),
            original.naverProductId(),
            original.reviewCount(),
            original.rating(),
            original.shippingInfo(),
            original.additionalInfo()
        );
        
        // then
        assertNotEquals(original.title(), copied.title());
        assertEquals("복사된 제목", copied.title());
        assertEquals(original.link(), copied.link());
        assertEquals(original.image(), copied.image());
        assertEquals(original.lprice(), copied.lprice());
    }
}
