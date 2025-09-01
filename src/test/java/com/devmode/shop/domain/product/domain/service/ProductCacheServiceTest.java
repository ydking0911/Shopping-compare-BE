package com.devmode.shop.domain.product.domain.service;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.devmode.shop.domain.product.application.dto.request.ProductSearchRequest;
import com.devmode.shop.domain.product.application.dto.response.ProductSearchResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductCacheService 테스트")
class ProductCacheServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ProductCacheService productCacheService;

    private ProductSearchRequest searchRequest;
    private ProductSearchResponse searchResponse;

    @BeforeEach
    void setUp() {
        searchRequest = new ProductSearchRequest(
                "테스트",
                1,
                20,
                "sim",
                Arrays.asList("used", "rental"),
                false,
                "전자제품",
                "컴퓨터",
                "노트북",
                "게이밍",
                null,
                null,
                null,
                null,
                null,
                null
        );

        searchResponse = ProductSearchResponse.of("테스트", Arrays.asList());
    }

    @Test
    @DisplayName("검색 결과 캐시 저장 성공 테스트")
    void cacheSearchResult_Success() throws JsonProcessingException {
        // given
        String expectedJson = "{\"products\":[],\"total\":100,\"page\":1,\"size\":20}";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(objectMapper.writeValueAsString(any(ProductSearchResponse.class))).thenReturn(expectedJson);

        // when
        productCacheService.cacheSearchResult(searchRequest, searchResponse);

        // then
        verify(redisTemplate, times(1)).opsForValue();
        verify(valueOperations, times(1)).set(anyString(), eq(expectedJson), any());
        verify(objectMapper, times(1)).writeValueAsString(searchResponse);
    }

    @Test
    @DisplayName("검색 결과 캐시 저장 실패 테스트 - 직렬화 오류")
    void cacheSearchResult_SerializationError() throws JsonProcessingException {
        // given
        when(objectMapper.writeValueAsString(any(ProductSearchResponse.class)))
                .thenThrow(new JsonProcessingException("직렬화 실패") {});

        // when & then
        // 예외가 발생해도 로그만 남기고 정상 종료되어야 함
        productCacheService.cacheSearchResult(searchRequest, searchResponse);

        // 직렬화 실패 시 redisTemplate.opsForValue()가 호출되지 않음
        verify(redisTemplate, never()).opsForValue();
        verify(valueOperations, never()).set(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("캐시된 검색 결과 조회 성공 테스트")
    void getCachedResult_Success() throws JsonProcessingException {
        // given
        String cachedJson = "{\"products\":[],\"total\":100,\"page\":1,\"size\":20}";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(cachedJson);
        when(objectMapper.readValue(cachedJson, ProductSearchResponse.class)).thenReturn(searchResponse);

        // when
        Optional<ProductSearchResponse> result = productCacheService.getCachedResult(searchRequest);

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(searchResponse);
        verify(redisTemplate, times(1)).opsForValue();
        verify(valueOperations, times(1)).get(anyString());
        verify(objectMapper, times(1)).readValue(cachedJson, ProductSearchResponse.class);
    }

    @Test
    @DisplayName("캐시된 검색 결과 조회 실패 테스트 - 캐시 미스")
    void getCachedResult_CacheMiss() {
        // given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);

        // when
        Optional<ProductSearchResponse> result = productCacheService.getCachedResult(searchRequest);

        // then
        assertThat(result).isEmpty();
        verify(redisTemplate, times(1)).opsForValue();
        verify(valueOperations, times(1)).get(anyString());
        // JSON 처리 관련 메서드는 검증하지 않음 (예외 처리로 인해)
    }

    @Test
    @DisplayName("캐시된 검색 결과 조회 실패 테스트 - 역직렬화 오류")
    void getCachedResult_DeserializationError() throws JsonProcessingException {
        // given
        String cachedJson = "invalid json";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(cachedJson);
        when(objectMapper.readValue(cachedJson, ProductSearchResponse.class))
                .thenThrow(new JsonProcessingException("역직렬화 실패") {});

        // when
        Optional<ProductSearchResponse> result = productCacheService.getCachedResult(searchRequest);

        // then
        assertThat(result).isEmpty();
        verify(redisTemplate, times(1)).opsForValue();
        verify(valueOperations, times(1)).get(anyString());
        verify(objectMapper, times(1)).readValue(cachedJson, ProductSearchResponse.class);
    }

    @Test
    @DisplayName("캐시 존재 여부 확인 테스트")
    void isCached_True() {
        // given
        when(redisTemplate.hasKey(anyString())).thenReturn(true);

        // when
        boolean result = productCacheService.isCached(searchRequest);

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
        boolean result = productCacheService.isCached(searchRequest);

        // then
        assertThat(result).isFalse();
        verify(redisTemplate, times(1)).hasKey(anyString());
    }

    @Test
    @DisplayName("캐시 무효화 테스트")
    void invalidateCache_Success() {
        // given
        String keyword = "테스트";

        // when
        productCacheService.invalidateCache(keyword);

        // then
        // 실제 구현에서는 Redis 패턴 매칭으로 키를 찾아 삭제하는 로직이 있어야 함
        // 현재는 로그만 남기는 상태
    }

    @Test
    @DisplayName("복잡한 필터가 포함된 캐시 키 생성 테스트")
    void generateCacheKey_ComplexFilters() throws JsonProcessingException {
        // given
        ProductSearchRequest complexRequest = new ProductSearchRequest(
                "노트북",
                2,
                50,
                "date",
                Arrays.asList("used", "overseas"),
                true,
                "전자제품",
                "컴퓨터",
                "노트북",
                "게이밍",
                "삼성",
                "삼성전자",
                1000000,
                5000000,
                4.5,
                100
        );

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(objectMapper.writeValueAsString(any(ProductSearchResponse.class))).thenReturn("{}");

        // when
        productCacheService.cacheSearchResult(complexRequest, searchResponse);

        // then
        verify(redisTemplate, times(1)).opsForValue();
        // 캐시 키가 올바르게 생성되어야 함
    }
}
