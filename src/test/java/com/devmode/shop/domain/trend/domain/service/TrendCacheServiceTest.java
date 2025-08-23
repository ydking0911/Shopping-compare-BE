package com.devmode.shop.domain.trend.domain.service;

import com.devmode.shop.domain.product.domain.repository.SearchHistoryRepository;
import com.devmode.shop.domain.trend.domain.repository.UserInterestKeywordsRepository;
import com.devmode.shop.global.config.properties.DataLabApiProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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

    @InjectMocks
    private TrendCacheService trendCacheService;

    private String testUserId;
    private String testKeyword;
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        testUserId = "test-user-123";
        testKeyword = "스마트폰";
        testDate = LocalDate.of(2024, 1, 15);
        
        // RedisTemplate의 기본 설정만 유지
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("트렌드 데이터 캐시 저장 성공")
    void cacheTrendData_Success() throws Exception {
        // given
        String testData = "{\"keyword\":\"스마트폰\",\"data\":\"test\"}";
        when(dataLabApiProperties.getCacheTtl()).thenReturn(3600);

        // when
        trendCacheService.cacheTrendData(testKeyword, testDate, testData);

        // then
        verify(valueOperations).set(
            eq("trend:스마트폰:2024-01-15"),
            eq(testData),
            any()
        );
    }

    @Test
    @DisplayName("사용자 검색 히스토리 캐시 저장 성공")
    void cacheUserSearchHistory_Success() throws Exception {
        // given
        List<String> searchHistory = List.of("스마트폰", "갤럭시", "아이폰");
        String jsonHistory = "[\"스마트폰\",\"갤럭시\",\"아이폰\"]";
        when(objectMapper.writeValueAsString(searchHistory)).thenReturn(jsonHistory);

        // when
        trendCacheService.cacheUserSearchHistory(testUserId, searchHistory);

        // then
        verify(valueOperations).set(
            eq("user_search_history:test-user-123"),
            eq(jsonHistory),
            any()
        );
    }

    @Test
    @DisplayName("사용자 검색 히스토리 캐시 조회 성공")
    void getUserSearchHistory_Success() throws Exception {
        // given
        String jsonHistory = "[\"스마트폰\",\"갤럭시\",\"아이폰\"]";
        List<String> expectedHistory = List.of("스마트폰", "갤럭시", "아이폰");
        
        when(valueOperations.get("user_search_history:test-user-123")).thenReturn(jsonHistory);
        when(objectMapper.readValue(jsonHistory, List.class)).thenReturn(expectedHistory);

        // when
        List<String> result = trendCacheService.getUserSearchHistory(testUserId);

        // then
        assertThat(result).isEqualTo(expectedHistory);
        verify(valueOperations).get("user_search_history:test-user-123");
    }

    @Test
    @DisplayName("사용자 검색 히스토리 데이터베이스 조회 성공")
    void getUserSearchHistoryFromDatabase_Success() {
        // given
        List<String> expectedHistory = List.of("스마트폰", "갤럭시", "아이폰");
        when(searchHistoryRepository.findRecentKeywordsByUserId(testUserId)).thenReturn(expectedHistory);

        // when
        List<String> result = trendCacheService.getUserSearchHistoryFromDatabase(testUserId);

        // then
        assertThat(result).isEqualTo(expectedHistory);
        verify(searchHistoryRepository).findRecentKeywordsByUserId(testUserId);
    }

    @Test
    @DisplayName("사용자 관심 키워드 데이터베이스 조회 성공")
    void getUserInterestKeywordsFromDatabase_Success() {
        // given
        List<String> expectedKeywords = List.of("스마트폰", "갤럭시");
        when(userInterestKeywordsRepository.findKeywordsByUserId(testUserId)).thenReturn(expectedKeywords);

        // when
        List<String> result = trendCacheService.getUserInterestKeywordsFromDatabase(testUserId);

        // then
        assertThat(result).isEqualTo(expectedKeywords);
        verify(userInterestKeywordsRepository).findKeywordsByUserId(testUserId);
    }

    @Test
    @DisplayName("사용자 관심 키워드 저장 성공")
    void saveUserInterestKeywords_Success() throws Exception {
        // given
        List<String> keywords = List.of("스마트폰", "갤럭시");
        String jsonKeywords = "[\"스마트폰\",\"갤럭시\"]";
        
        when(objectMapper.writeValueAsString(keywords)).thenReturn(jsonKeywords);
        doNothing().when(userInterestKeywordsRepository).deactivateAllByUserId(testUserId);

        // when
        trendCacheService.saveUserInterestKeywords(testUserId, keywords);

        // then
        verify(userInterestKeywordsRepository).deactivateAllByUserId(testUserId);
        verify(userInterestKeywordsRepository).saveAll(any());
        verify(valueOperations).set(
            eq("user_interest_keywords:test-user-123"),
            eq(jsonKeywords),
            any()
        );
    }
}
