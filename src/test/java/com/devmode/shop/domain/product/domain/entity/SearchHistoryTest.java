package com.devmode.shop.domain.product.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.time.LocalDateTime;

class SearchHistoryTest {

    private SearchHistory searchHistory;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusHours(1);
        LocalDateTime lastApiCall = now.minusMinutes(5);

        searchHistory = SearchHistory.builder()
                .userId("user123")
                .keyword("노트북")
                .filters("brand:삼성,price:100000-500000")
                .page(1)
                .size(20)
                .sort("sim")
                .totalResults(150)
                .lastSearchedAt(now)
                .cacheKey("search:노트북:1:20:sim")
                .cacheExpiresAt(expiresAt)
                .apiCallCount(3)
                .lastApiCallAt(lastApiCall)
                .build();
    }

    @Test
    @DisplayName("SearchHistory 빌더를 통한 정상 생성 테스트")
    void testSearchHistoryBuilder() {
        // given & when
        LocalDateTime now = LocalDateTime.now();
        SearchHistory newHistory = SearchHistory.builder()
                .userId("user456")
                .keyword("스마트폰")
                .filters("brand:애플,price:500000-1000000")
                .page(2)
                .size(10)
                .sort("date")
                .totalResults(75)
                .lastSearchedAt(now)
                .cacheKey("search:스마트폰:2:10:date")
                .cacheExpiresAt(now.plusHours(2))
                .apiCallCount(1)
                .lastApiCallAt(now.minusMinutes(10))
                .build();

        // then
        assertNotNull(newHistory);
        assertEquals("user456", newHistory.getUserId());
        assertEquals("스마트폰", newHistory.getKeyword());
        assertEquals("brand:애플,price:500000-1000000", newHistory.getFilters());
        assertEquals(2, newHistory.getPage());
        assertEquals(10, newHistory.getSize());
        assertEquals("date", newHistory.getSort());
        assertEquals(75, newHistory.getTotalResults());
        assertEquals(now, newHistory.getLastSearchedAt());
        assertEquals("search:스마트폰:2:10:date", newHistory.getCacheKey());
        assertEquals(1, newHistory.getApiCallCount());
    }

    @Test
    @DisplayName("SearchHistory 기본값 테스트")
    void testSearchHistoryDefaultValues() {
        // given & when
        SearchHistory defaultHistory = SearchHistory.builder()
                .userId("user789")
                .keyword("기본키워드")
                .filters("")
                .page(1)
                .size(20)
                .sort("sim")
                .totalResults(0)
                .lastSearchedAt(LocalDateTime.now())
                .cacheKey("default")
                .cacheExpiresAt(LocalDateTime.now().plusHours(1))
                .apiCallCount(0)
                .lastApiCallAt(LocalDateTime.now())
                .build();

        // then
        assertNotNull(defaultHistory);
        assertEquals("user789", defaultHistory.getUserId());
        assertEquals("기본키워드", defaultHistory.getKeyword());
        assertEquals("", defaultHistory.getFilters());
        assertEquals(1, defaultHistory.getPage());
        assertEquals(20, defaultHistory.getSize());
        assertEquals("sim", defaultHistory.getSort());
        assertEquals(0, defaultHistory.getTotalResults());
        assertEquals(0, defaultHistory.getApiCallCount());
    }

    @Test
    @DisplayName("SearchHistory 검색 파라미터 테스트")
    void testSearchHistorySearchParameters() {
        // given & when
        String keyword = searchHistory.getKeyword();
        Integer page = searchHistory.getPage();
        Integer size = searchHistory.getSize();
        String sort = searchHistory.getSort();

        // then
        assertEquals("노트북", keyword);
        assertEquals(1, page);
        assertEquals(20, size);
        assertEquals("sim", sort);
        
        assertTrue(page > 0);
        assertTrue(size > 0);
        assertNotNull(sort);
        assertTrue(sort.length() > 0);
    }

    @Test
    @DisplayName("SearchHistory 필터 정보 테스트")
    void testSearchHistoryFilters() {
        // given & when
        String filters = searchHistory.getFilters();

        // then
        assertEquals("brand:삼성,price:100000-500000", filters);
        assertTrue(filters.contains("brand:삼성"));
        assertTrue(filters.contains("price:100000-500000"));
        assertNotNull(filters);
    }

    @Test
    @DisplayName("SearchHistory 검색 결과 정보 테스트")
    void testSearchHistorySearchResults() {
        // given & when
        Integer totalResults = searchHistory.getTotalResults();

        // then
        assertEquals(150, totalResults);
        assertTrue(totalResults >= 0);
    }

    @Test
    @DisplayName("SearchHistory 캐시 정보 테스트")
    void testSearchHistoryCacheInfo() {
        // given & when
        String cacheKey = searchHistory.getCacheKey();
        LocalDateTime cacheExpiresAt = searchHistory.getCacheExpiresAt();
        LocalDateTime lastSearchedAt = searchHistory.getLastSearchedAt();

        // then
        assertEquals("search:노트북:1:20:sim", cacheKey);
        assertNotNull(cacheExpiresAt);
        assertNotNull(lastSearchedAt);
        assertTrue(cacheExpiresAt.isAfter(lastSearchedAt));
    }

    @Test
    @DisplayName("SearchHistory API 호출 정보 테스트")
    void testSearchHistoryApiCallInfo() {
        // given & when
        Integer apiCallCount = searchHistory.getApiCallCount();
        LocalDateTime lastApiCallAt = searchHistory.getLastApiCallAt();

        // then
        assertEquals(3, apiCallCount);
        assertNotNull(lastApiCallAt);
        assertTrue(apiCallCount >= 0);
    }

    @Test
    @DisplayName("SearchHistory 시간 정보 테스트")
    void testSearchHistoryTimeInfo() {
        // given & when
        LocalDateTime lastSearchedAt = searchHistory.getLastSearchedAt();
        LocalDateTime cacheExpiresAt = searchHistory.getCacheExpiresAt();
        LocalDateTime lastApiCallAt = searchHistory.getLastApiCallAt();

        // then
        assertNotNull(lastSearchedAt);
        assertNotNull(cacheExpiresAt);
        assertNotNull(lastApiCallAt);
        
        // 캐시 만료 시간은 마지막 검색 시간보다 이후여야 함
        assertTrue(cacheExpiresAt.isAfter(lastSearchedAt));
        
        // 마지막 API 호출 시간은 마지막 검색 시간보다 이전이거나 같아야 함
        assertTrue(lastApiCallAt.isBefore(lastSearchedAt) || lastApiCallAt.isEqual(lastSearchedAt));
    }

    @Test
    @DisplayName("SearchHistory 사용자별 구분 테스트")
    void testSearchHistoryUserSeparation() {
        // given
        SearchHistory user1History = SearchHistory.builder()
                .userId("user1")
                .keyword("키워드1")
                .build();

        SearchHistory user2History = SearchHistory.builder()
                .userId("user2")
                .keyword("키워드2")
                .build();

        // when & then
        assertNotEquals(user1History.getUserId(), user2History.getUserId());
        assertNotEquals(user1History.getKeyword(), user2History.getKeyword());
    }

    @Test
    @DisplayName("SearchHistory 정렬 옵션 테스트")
    void testSearchHistorySortOptions() {
        // given
        String[] validSortOptions = {"sim", "date", "asc", "dsc"};
        String currentSort = searchHistory.getSort();

        // when & then
        assertTrue(java.util.Arrays.asList(validSortOptions).contains(currentSort));
    }

    @Test
    @DisplayName("SearchHistory 페이지네이션 테스트")
    void testSearchHistoryPagination() {
        // given & when
        Integer page = searchHistory.getPage();
        Integer size = searchHistory.getSize();
        Integer totalResults = searchHistory.getTotalResults();

        // then
        assertTrue(page > 0);
        assertTrue(size > 0);
        assertTrue(totalResults >= 0);
        
        // 페이지 크기가 총 결과 수보다 클 수 있음 (마지막 페이지의 경우)
        assertTrue(size <= totalResults || page == 1);
    }
}
