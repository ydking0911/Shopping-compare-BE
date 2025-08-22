package com.devmode.shop.domain.trend.domain.service;

import com.devmode.shop.domain.trend.application.dto.request.TrendSearchRequest;
import com.devmode.shop.domain.trend.application.dto.response.trend.TrendSearchResponse;
import com.devmode.shop.global.config.properties.DataLabApiProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrendCacheService 테스트")
class TrendCacheServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private DataLabApiProperties dataLabApiProperties;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private TrendCacheService trendCacheService;

    private TrendSearchRequest searchRequest;
    private TrendSearchResponse searchResponse;
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        testDate = LocalDate.of(2024, 1, 15);
        searchRequest = new TrendSearchRequest(
                "노트북",
                testDate,
                testDate.plusDays(7),
                Arrays.asList(),
                Arrays.asList(),
                "date",
                true,
                true,
                true
        );
        searchResponse = new TrendSearchResponse(
                "노트북",
                testDate,
                testDate.plusDays(7),
                "date",
                "naver_datalab",
                10L,
                Arrays.asList(),
                null,
                "fresh",
                100L,
                1L,
                "available"
        );
    }

    @Test
    @DisplayName("트렌드 데이터 캐시 저장 성공 테스트")
    void cacheTrendData_Success() {
        // given
        String keyword = "노트북";
        String data = "{\"ratio\":1.5,\"clickCount\":1000}";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(dataLabApiProperties.getCacheTtl()).thenReturn(3600);

        // when
        trendCacheService.cacheTrendData(keyword, testDate, data);

        // then
        verify(redisTemplate, times(1)).opsForValue();
        verify(valueOperations, times(1)).set(anyString(), eq(data), any());
    }

    @Test
    @DisplayName("캐시된 트렌드 데이터 조회 성공 테스트")
    void getCachedTrendData_Success() {
        // given
        String keyword = "노트북";
        String cachedData = "{\"ratio\":1.5,\"clickCount\":1000}";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(cachedData);

        // when
        Optional<String> result = trendCacheService.getCachedTrendData(keyword, testDate);

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(cachedData);
        verify(redisTemplate, times(1)).opsForValue();
        verify(valueOperations, times(1)).get(anyString());
    }

    @Test
    @DisplayName("캐시된 트렌드 데이터 조회 실패 테스트 - 캐시 미스")
    void getCachedTrendData_CacheMiss() {
        // given
        String keyword = "노트북";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);

        // when
        Optional<String> result = trendCacheService.getCachedTrendData(keyword, testDate);

        // then
        assertThat(result).isEmpty();
        verify(redisTemplate, times(1)).opsForValue();
        verify(valueOperations, times(1)).get(anyString());
    }

    @Test
    @DisplayName("검색 결과 캐시 저장 성공 테스트")
    void cacheSearchResult_Success() throws JsonProcessingException {
        // given
        String expectedJson = "{\"keyword\":\"노트북\",\"startDate\":\"2024-01-15\"}";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(objectMapper.writeValueAsString(any(TrendSearchResponse.class))).thenReturn(expectedJson);
        when(dataLabApiProperties.getCacheTtl()).thenReturn(3600);

        // when
        trendCacheService.cacheSearchResult(searchRequest, searchResponse);

        // then
        verify(redisTemplate, times(1)).opsForValue();
        verify(valueOperations, times(1)).set(anyString(), eq(expectedJson), any());
        verify(objectMapper, times(1)).writeValueAsString(searchResponse);
    }

    @Test
    @DisplayName("검색 결과 캐시 저장 실패 테스트 - 직렬화 오류")
    void cacheSearchResult_SerializationError() throws JsonProcessingException {
        // given
        when(objectMapper.writeValueAsString(any(TrendSearchResponse.class)))
                .thenThrow(new JsonProcessingException("직렬화 실패") {});

        // when & then
        // 예외가 발생해도 로그만 남기고 정상 종료되어야 함
        trendCacheService.cacheSearchResult(searchRequest, searchResponse);

        // 직렬화 실패 시 redisTemplate.opsForValue()가 호출되지 않음
        verify(redisTemplate, never()).opsForValue();
        verify(valueOperations, never()).set(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("캐시된 검색 결과 조회 성공 테스트")
    void getCachedSearchResult_Success() throws JsonProcessingException {
        // given
        String cachedJson = "{\"keyword\":\"노트북\",\"startDate\":\"2024-01-15\"}";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(cachedJson);
        when(objectMapper.readValue(cachedJson, TrendSearchResponse.class)).thenReturn(searchResponse);

        // when
        Optional<TrendSearchResponse> result = trendCacheService.getCachedSearchResult(searchRequest);

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(searchResponse);
        verify(redisTemplate, times(1)).opsForValue();
        verify(valueOperations, times(1)).get(anyString());
        verify(objectMapper, times(1)).readValue(cachedJson, TrendSearchResponse.class);
    }

    @Test
    @DisplayName("캐시된 검색 결과 조회 실패 테스트 - 캐시 미스")
    void getCachedSearchResult_CacheMiss() {
        // given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);

        // when
        Optional<TrendSearchResponse> result = trendCacheService.getCachedSearchResult(searchRequest);

        // then
        assertThat(result).isEmpty();
        verify(redisTemplate, times(1)).opsForValue();
        verify(valueOperations, times(1)).get(anyString());
        // JSON 처리 관련 메서드는 검증하지 않음 (예외 처리로 인해)
    }

    @Test
    @DisplayName("캐시된 검색 결과 조회 실패 테스트 - 역직렬화 오류")
    void getCachedSearchResult_DeserializationError() throws JsonProcessingException {
        // given
        String cachedJson = "invalid json";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(cachedJson);
        when(objectMapper.readValue(cachedJson, TrendSearchResponse.class))
                .thenThrow(new RuntimeException("역직렬화 실패"));

        // when
        Optional<TrendSearchResponse> result = trendCacheService.getCachedSearchResult(searchRequest);

        // then
        assertThat(result).isEmpty();
        verify(redisTemplate, times(1)).opsForValue();
        verify(valueOperations, times(1)).get(anyString());
        verify(objectMapper, times(1)).readValue(cachedJson, TrendSearchResponse.class);
    }

    @Test
    @DisplayName("캐시 존재 여부 확인 테스트")
    void isCached_True() {
        // given
        when(redisTemplate.hasKey(anyString())).thenReturn(true);

        // when
        boolean result = trendCacheService.isCached(searchRequest);

        // then
        assertThat(result).isTrue();
        verify(redisTemplate, times(1)).hasKey(anyString());
    }

    @Test
    @DisplayName("캐시 존재 여부 확인 테스트 - 캐시 없음")
    void isCached_False() {
        // given
        when(redisTemplate.hasKey(anyString())).thenReturn(false);

        // when
        boolean result = trendCacheService.isCached(searchRequest);

        // then
        assertThat(result).isFalse();
        verify(redisTemplate, times(1)).hasKey(anyString());
    }

    @Test
    @DisplayName("트렌드 캐시 무효화 테스트")
    void clearCache_Success() {
        // given
        String keyword = "노트북";
        Set<String> keys = Set.of("trend:노트북:2024-01-15", "trend:노트북:2024-01-16");
        when(redisTemplate.keys(anyString())).thenReturn(keys);

        // when
        trendCacheService.clearCache(keyword);

        // then
        verify(redisTemplate, times(1)).keys(anyString());
        verify(redisTemplate, times(1)).delete(keys);
    }

    @Test
    @DisplayName("검색 캐시 무효화 테스트")
    void clearSearchCache_Success() {
        // given
        String keyword = "노트북";
        Set<String> keys = Set.of("trend_search:노트북:2024-01-15:2024-01-22:date");
        when(redisTemplate.keys(anyString())).thenReturn(keys);

        // when
        trendCacheService.clearSearchCache(keyword);

        // then
        verify(redisTemplate, times(1)).keys(anyString());
        verify(redisTemplate, times(1)).delete(keys);
    }

    @Test
    @DisplayName("복잡한 검색 요청에 대한 캐시 키 생성 테스트")
    void buildCacheKey_ComplexRequest() throws JsonProcessingException {
        // given
        TrendSearchRequest complexRequest = new TrendSearchRequest(
                "노트북",
                testDate,
                testDate.plusDays(30),
                Arrays.asList(),
                Arrays.asList(),
                "week",
                true,
                true,
                true
        );

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(objectMapper.writeValueAsString(any(TrendSearchResponse.class))).thenReturn("{}");
        when(dataLabApiProperties.getCacheTtl()).thenReturn(3600);

        // when
        trendCacheService.cacheSearchResult(complexRequest, searchResponse);

        // then
        verify(redisTemplate, times(1)).opsForValue();
        // 캐시 키가 올바르게 생성되어야 함
    }
}
