package com.devmode.shop.domain.trend.domain.service;

import com.devmode.shop.domain.trend.domain.entity.UserInterestKeywords;
import com.devmode.shop.domain.trend.domain.repository.UserInterestKeywordsRepository;
import com.devmode.shop.domain.product.domain.repository.SearchHistoryRepository;
import com.devmode.shop.global.config.properties.DataLabApiProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("TrendCacheService 테스트")
class TrendCacheServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private DataLabApiProperties dataLabApiProperties;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private SearchHistoryRepository searchHistoryRepository;

    @Mock
    private UserInterestKeywordsRepository userInterestKeywordsRepository;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private TypeFactory typeFactory;

    @Mock
    private CollectionType collectionType;

    @InjectMocks
    private TrendCacheService trendCacheService;

    private static final String TEST_KEYWORD = "노트북";
    private static final String TEST_USER_ID = "user123";
    private static final LocalDate TEST_DATE = LocalDate.of(2024, 1, 1);

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(dataLabApiProperties.getCacheTtl()).thenReturn(3600);
        
        // TypeFactory 관련 Mock 설정
        when(objectMapper.getTypeFactory()).thenReturn(typeFactory);
        when(typeFactory.constructCollectionType(List.class, String.class)).thenReturn(collectionType);
    }

    @Test
    @DisplayName("트렌드 데이터 캐싱 테스트")
    void testCacheTrendData() {
        // given
        String data = "{\"keyword\":\"노트북\",\"trend\":\"up\"}";
        String expectedKey = "trend:노트북:2024-01-01";

        // when
        trendCacheService.cacheTrendData(TEST_KEYWORD, TEST_DATE, data);

        // then
        verify(valueOperations).set(eq(expectedKey), eq(data), eq(Duration.ofSeconds(3600)));
    }

    @Test
    @DisplayName("캐시된 트렌드 데이터 조회 테스트 - 성공")
    void testGetCachedTrendData_Success() {
        // given
        String cachedData = "{\"keyword\":\"노트북\",\"trend\":\"up\"}";
        String key = "trend:노트북:2024-01-01";
        when(valueOperations.get(key)).thenReturn(cachedData);

        // when
        var result = trendCacheService.getCachedTrendData(TEST_KEYWORD, TEST_DATE);

        // then
        assertTrue(result.isPresent());
        assertEquals(cachedData, result.get());
    }

    @Test
    @DisplayName("캐시된 트렌드 데이터 조회 테스트 - 실패")
    void testGetCachedTrendData_NotFound() {
        // given
        String key = "trend:노트북:2024-01-01";
        when(valueOperations.get(key)).thenReturn(null);

        // when
        var result = trendCacheService.getCachedTrendData(TEST_KEYWORD, TEST_DATE);

        // then
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("캐시 무효화 테스트")
    void testClearCache() {
        // given
        Set<String> keys = Set.of("trend:노트북:2024-01-01", "trend:노트북:2024-01-02");
        when(redisTemplate.keys("trend:노트북*")).thenReturn(keys);

        // when
        trendCacheService.clearCache(TEST_KEYWORD);

        // then
        verify(redisTemplate).delete(keys);
    }

    @Test
    @DisplayName("사용자 검색 히스토리 캐시 조회 테스트 - 성공")
    void testGetUserSearchHistory_Success() throws Exception {
        // given
        String cachedJson = "[\"노트북\", \"키보드\"]";
        List<String> expectedHistory = List.of("노트북", "키보드");
        
        when(valueOperations.get("user_search_history:user123")).thenReturn(cachedJson);
        when(objectMapper.getTypeFactory()).thenReturn(typeFactory);
        when(typeFactory.constructCollectionType(List.class, String.class)).thenReturn(collectionType);
        when(objectMapper.readValue(cachedJson, collectionType)).thenReturn(expectedHistory);

        // when
        List<String> result = trendCacheService.getUserSearchHistory(TEST_USER_ID);

        // then
        assertEquals(expectedHistory, result);
    }

    @Test
    @DisplayName("사용자 검색 히스토리 캐시 조회 테스트 - 캐시 미스")
    void testGetUserSearchHistory_CacheMiss() {
        // given
        when(valueOperations.get("user_search_history:user123")).thenReturn(null);

        // when
        List<String> result = trendCacheService.getUserSearchHistory(TEST_USER_ID);

        // then
        assertNull(result);
    }

    @Test
    @DisplayName("사용자 검색 히스토리 데이터베이스 조회 테스트 - 성공")
    void testGetUserSearchHistoryFromDatabase_Success() {
        // given
        List<String> expectedHistory = List.of("노트북", "키보드");
        when(searchHistoryRepository.findRecentKeywordsByUserId(TEST_USER_ID)).thenReturn(expectedHistory);

        // when
        List<String> result = trendCacheService.getUserSearchHistoryFromDatabase(TEST_USER_ID);

        // then
        assertEquals(expectedHistory, result);
    }

    @Test
    @DisplayName("사용자 검색 히스토리 데이터베이스 조회 테스트 - 예외 발생")
    void testGetUserSearchHistoryFromDatabase_Exception() {
        // given
        when(searchHistoryRepository.findRecentKeywordsByUserId(TEST_USER_ID))
            .thenThrow(new RuntimeException("데이터베이스 오류"));

        // when
        List<String> result = trendCacheService.getUserSearchHistoryFromDatabase(TEST_USER_ID);

        // then
        assertEquals(List.of(), result);
    }

    @Test
    @DisplayName("사용자 검색 히스토리 캐싱 테스트")
    void testCacheUserSearchHistory() throws JsonProcessingException {
        // given
        List<String> searchHistory = List.of("노트북", "키보드");
        String jsonHistory = "[\"노트북\", \"키보드\"]";
        when(objectMapper.writeValueAsString(searchHistory)).thenReturn(jsonHistory);

        // when
        trendCacheService.cacheUserSearchHistory(TEST_USER_ID, searchHistory);

        // then
        verify(valueOperations).set("user_search_history:user123", jsonHistory, Duration.ofHours(24));
    }

    @Test
    @DisplayName("사용자 관심 키워드 캐시 조회 테스트 - 성공")
    void testGetUserInterestKeywords_Success() throws Exception {
        // given
        String cachedJson = "[\"노트북\", \"키보드\"]";
        List<String> expectedKeywords = List.of("노트북", "키보드");
        
        when(valueOperations.get("user_interest_keywords:user123")).thenReturn(cachedJson);
        when(objectMapper.getTypeFactory()).thenReturn(typeFactory);
        when(typeFactory.constructCollectionType(List.class, String.class)).thenReturn(collectionType);
        when(objectMapper.readValue(cachedJson, collectionType)).thenReturn(expectedKeywords);

        // when
        List<String> result = trendCacheService.getUserInterestKeywords(TEST_USER_ID);

        // then
        assertEquals(expectedKeywords, result);
    }

    @Test
    @DisplayName("사용자 관심 키워드 캐시 조회 테스트 - 캐시 미스")
    void testGetUserInterestKeywords_CacheMiss() {
        // given
        when(valueOperations.get("user_interest_keywords:user123")).thenReturn(null);

        // when
        List<String> result = trendCacheService.getUserInterestKeywords(TEST_USER_ID);

        // then
        assertEquals(List.of(), result);
    }

    @Test
    @DisplayName("프리페치 시드 키워드 캐싱 테스트")
    void testCacheTrendKeywords() throws JsonProcessingException {
        // given
        List<String> keywords = List.of("노트북", "키보드");
        String jsonKeywords = "[\"노트북\", \"키보드\"]";
        Duration ttl = Duration.ofHours(1);
        when(objectMapper.writeValueAsString(keywords)).thenReturn(jsonKeywords);

        // when
        trendCacheService.cacheTrendKeywords("test_key", keywords, ttl);

        // then
        verify(valueOperations).set("test_key", jsonKeywords, ttl);
    }

    @Test
    @DisplayName("캐시된 프리페치 시드 키워드 조회 테스트 - 성공")
    void testGetCachedTrendKeywords_Success() throws Exception {
        // given
        String cachedJson = "[\"노트북\", \"키보드\"]";
        List<String> expectedKeywords = List.of("노트북", "키보드");
        
        when(valueOperations.get("test_key")).thenReturn(cachedJson);
        when(objectMapper.getTypeFactory()).thenReturn(typeFactory);
        when(typeFactory.constructCollectionType(List.class, String.class)).thenReturn(collectionType);
        when(objectMapper.readValue(cachedJson, collectionType)).thenReturn(expectedKeywords);

        // when
        List<String> result = trendCacheService.getCachedTrendKeywords("test_key");

        // then
        assertEquals(expectedKeywords, result);
    }

    @Test
    @DisplayName("프리페치 트렌드 데이터 캐싱 테스트")
    void testCacheTrendData_WithTtl() throws JsonProcessingException {
        // given
        Object trendData = new Object();
        String jsonData = "{\"data\":\"test\"}";
        Duration ttl = Duration.ofHours(1);
        when(objectMapper.writeValueAsString(trendData)).thenReturn(jsonData);

        // when
        trendCacheService.cacheTrendData("test_key", trendData, ttl);

        // then
        verify(valueOperations).set("test_key", jsonData, ttl);
    }

    @Test
    @DisplayName("캐시된 프리페치 트렌드 데이터 조회 테스트 - 성공")
    void testGetCachedTrendData_Generic_Success() throws Exception {
        // given
        String cachedJson = "{\"data\":\"test\"}";
        TestData expectedData = new TestData("test");
        
        when(valueOperations.get("test_key")).thenReturn(cachedJson);
        when(objectMapper.readValue(cachedJson, TestData.class)).thenReturn(expectedData);

        // when
        TestData result = trendCacheService.getCachedTrendData("test_key", TestData.class);

        // then
        assertEquals(expectedData, result);
    }

    @Test
    @DisplayName("캐시된 프리페치 트렌드 데이터 조회 테스트 - 캐시 미스")
    void testGetCachedTrendData_Generic_CacheMiss() {
        // given
        when(valueOperations.get("test_key")).thenReturn(null);

        // when
        TestData result = trendCacheService.getCachedTrendData("test_key", TestData.class);

        // then
        assertNull(result);
    }

    @Test
    @DisplayName("패턴으로 캐시 무효화 테스트")
    void testInvalidateCacheByPattern() {
        // given
        Set<String> keys = Set.of("trend:test1", "trend:test2");
        when(redisTemplate.keys("trend:*")).thenReturn(keys);

        // when
        trendCacheService.invalidateCacheByPattern("trend:*");

        // then
        verify(redisTemplate).delete(keys);
    }

    @Test
    @DisplayName("패턴으로 캐시 무효화 테스트 - 키가 없는 경우")
    void testInvalidateCacheByPattern_NoKeys() {
        // given
        when(redisTemplate.keys("trend:*")).thenReturn(Set.of());

        // when
        trendCacheService.invalidateCacheByPattern("trend:*");

        // then
        verify(redisTemplate, never()).delete(any(Set.class));
    }

    @Test
    @DisplayName("사용자 관심 키워드 캐시 저장 테스트")
    void testCacheUserInterestKeywords() throws JsonProcessingException {
        // given
        List<String> keywords = List.of("노트북", "키보드");
        String jsonKeywords = "[\"노트북\", \"키보드\"]";
        when(objectMapper.writeValueAsString(keywords)).thenReturn(jsonKeywords);

        // when
        trendCacheService.cacheUserInterestKeywords(TEST_USER_ID, keywords);

        // then
        verify(valueOperations).set("user_interest_keywords:user123", jsonKeywords, Duration.ofDays(7));
    }

    @Test
    @DisplayName("사용자 관심 키워드 데이터베이스 저장 테스트 - 성공")
    void testSaveUserInterestKeywords_Success() {
        // given
        List<String> keywords = List.of("노트북", "키보드");

        // when
        trendCacheService.saveUserInterestKeywords(TEST_USER_ID, keywords);

        // then
        verify(userInterestKeywordsRepository).deactivateAllByUserId(TEST_USER_ID);
        verify(userInterestKeywordsRepository).saveAll(any());
    }

    @Test
    @DisplayName("사용자 관심 키워드 데이터베이스 저장 테스트 - 예외 발생")
    void testSaveUserInterestKeywords_Exception() {
        // given
        List<String> keywords = List.of("노트북", "키보드");
        doThrow(new RuntimeException("데이터베이스 오류"))
            .when(userInterestKeywordsRepository).deactivateAllByUserId(TEST_USER_ID);

        // when
        trendCacheService.saveUserInterestKeywords(TEST_USER_ID, keywords);

        // then
        verify(userInterestKeywordsRepository).deactivateAllByUserId(TEST_USER_ID);
        verify(userInterestKeywordsRepository, never()).saveAll(any());
    }

    // 테스트용 내부 클래스
    private static class TestData {
        private String data;

        public TestData() {}

        public TestData(String data) {
            this.data = data;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            TestData testData = (TestData) obj;
            return data.equals(testData.data);
        }

        @Override
        public int hashCode() {
            return data.hashCode();
        }
    }
}
