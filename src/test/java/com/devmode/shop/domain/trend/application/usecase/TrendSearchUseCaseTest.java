package com.devmode.shop.domain.trend.application.usecase;

import com.devmode.shop.domain.trend.application.dto.request.TrendSearchRequest;
import com.devmode.shop.domain.trend.application.dto.response.datalab.NaverDataLabResponse;
import com.devmode.shop.domain.trend.application.dto.response.datalab.DataLabResult;
import com.devmode.shop.domain.trend.application.dto.response.trend.TrendSearchResponse;
import com.devmode.shop.domain.trend.application.dto.response.trend.TrendDataPoint;
import com.devmode.shop.domain.trend.application.dto.response.trend.TrendSummary;
import com.devmode.shop.domain.trend.domain.service.NaverDataLabApiService;
import com.devmode.shop.domain.trend.domain.service.TrendCacheService;
import com.devmode.shop.domain.trend.domain.service.TrendTransformService;
import com.devmode.shop.global.exception.RestApiException;
import com.devmode.shop.global.exception.code.status.GlobalErrorStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class TrendSearchUseCaseTest {

    @Mock
    private NaverDataLabApiService naverDataLabApiService;

    @Mock
    private TrendCacheService trendCacheService;

    @Mock
    private TrendTransformService trendTransformService;

    @InjectMocks
    private TrendSearchUseCase trendSearchUseCase;

    private TrendSearchRequest request;
    private TrendSearchResponse mockResponse;
    private NaverDataLabResponse mockNaverResponse;

    @BeforeEach
    void setUp() {
        request = new TrendSearchRequest(
                "laptop",
                LocalDate.of(2025, 8, 1),
                LocalDate.of(2025, 8, 22),
                List.of("전자제품"),
                List.of("노트북", "컴퓨터"),
                "date",
                true,
                true,
                true
        );

        mockResponse = new TrendSearchResponse(
                "laptop",
                LocalDate.of(2025, 8, 1),
                LocalDate.of(2025, 8, 22),
                "date",
                "naver_datalab",
                10L,
                List.of(),
                new TrendSummary(
                        BigDecimal.valueOf(100.0),
                        BigDecimal.valueOf(150.0),
                        BigDecimal.valueOf(50.0),
                        1000L,
                        "RISING",
                        BigDecimal.valueOf(25.0)
                ),
                "fresh",
                100L,
                1L,
                "available"
        );

        mockNaverResponse = new NaverDataLabResponse(
                "2025-08-01",
                "2025-08-22",
                "date",
                List.of()
        );
    }

    @Test
    @DisplayName("캐시에서 트렌드 검색 결과를 가져올 수 있다")
    void searchTrendsFromCache() {
        // given
        when(trendCacheService.isCached(any())).thenReturn(true);
        when(trendCacheService.getCachedSearchResult(any())).thenReturn(Optional.of(mockResponse));

        // when
        TrendSearchResponse result = trendSearchUseCase.searchTrends(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.keyword()).isEqualTo("laptop");
        verify(trendCacheService).isCached(request);
        verify(trendCacheService).getCachedSearchResult(request);
        verify(naverDataLabApiService, never()).searchTrends(any());
    }

    @Test
    @DisplayName("캐시에 없을 때 네이버 데이터랩 API를 호출하여 트렌드를 검색할 수 있다")
    void searchTrendsFromNaverApi() {
        // given
        when(trendCacheService.isCached(any())).thenReturn(false);
        when(naverDataLabApiService.searchTrends(any())).thenReturn(mockNaverResponse);
        when(trendTransformService.transformToTrendSearchResponse(
                any(NaverDataLabResponse.class), 
                any(TrendSearchRequest.class), 
                anyString(), 
                anyLong(), 
                anyLong(), 
                anyString()
        )).thenReturn(mockResponse);

        // when
        TrendSearchResponse result = trendSearchUseCase.searchTrends(request);

        // then
        assertThat(result).isNotNull();
        verify(trendCacheService).isCached(request);
        verify(naverDataLabApiService).searchTrends(request);
        verify(trendTransformService).transformToTrendSearchResponse(
                eq(mockNaverResponse), eq(request), eq("fresh"), anyLong(), eq(1L), eq("available")
        );
        verify(trendCacheService).cacheSearchResult(request, mockResponse);
    }

    @Test
    @DisplayName("네이버 API 호출 실패 시 예외를 던진다")
    void searchTrendsThrowsExceptionWhenApiFails() {
        // given
        when(trendCacheService.isCached(any())).thenReturn(false);
        when(naverDataLabApiService.searchTrends(any())).thenThrow(new RestApiException(GlobalErrorStatus._INTERNAL_SERVER_ERROR));
        when(trendCacheService.getCachedSearchResult(any())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> trendSearchUseCase.searchTrends(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Trend search failed and no fallback available");
    }

    @Test
    @DisplayName("캐시 우선 검색을 성공적으로 처리할 수 있다")
    void searchTrendsWithCacheSuccess() {
        // given
        when(trendCacheService.getCachedSearchResult(any())).thenReturn(Optional.of(mockResponse));

        // when
        TrendSearchResponse result = trendSearchUseCase.searchTrendsWithCache(request);

        // then
        assertThat(result).isNotNull();
        verify(trendCacheService).getCachedSearchResult(request);
        verify(naverDataLabApiService, never()).searchTrends(any());
    }

    @Test
    @DisplayName("캐시 우선 검색 시 캐시에 없으면 API를 호출한다")
    void searchTrendsWithCacheFallsBackToApi() {
        // given
        when(trendCacheService.getCachedSearchResult(any())).thenReturn(Optional.empty());
        when(trendCacheService.isCached(any())).thenReturn(false);
        when(naverDataLabApiService.searchTrends(any())).thenReturn(mockNaverResponse);
        when(trendTransformService.transformToTrendSearchResponse(
                any(NaverDataLabResponse.class), 
                any(TrendSearchRequest.class), 
                anyString(), 
                anyLong(), 
                anyLong(), 
                anyString()
        )).thenReturn(mockResponse);

        // when
        TrendSearchResponse result = trendSearchUseCase.searchTrendsWithCache(request);

        // then
        assertThat(result).isNotNull();
        verify(trendCacheService).getCachedSearchResult(request);
        verify(naverDataLabApiService).searchTrends(request);
    }

    @Test
    @DisplayName("트렌드 캐시를 정리할 수 있다")
    void clearTrendCache() {
        // when
        trendSearchUseCase.clearTrendCache("laptop");

        // then
        verify(trendCacheService).clearCache("laptop");
        verify(trendCacheService).clearSearchCache("laptop");
    }

    @Test
    @DisplayName("키워드 필터가 올바르게 적용된다")
    void searchTrendsWithKeywordFilter() {
        // given
        TrendSearchRequest requestWithKeyword = new TrendSearchRequest(
                "smartphone",
                LocalDate.of(2025, 8, 1),
                LocalDate.of(2025, 8, 22),
                List.of(),
                List.of(),
                "date",
                true,
                true,
                true
        );

        when(trendCacheService.isCached(any())).thenReturn(false);
        when(naverDataLabApiService.searchTrends(any())).thenReturn(mockNaverResponse);
        when(trendTransformService.transformToTrendSearchResponse(
                any(NaverDataLabResponse.class), 
                any(TrendSearchRequest.class), 
                anyString(), 
                anyLong(), 
                anyLong(), 
                anyString()
        )).thenReturn(mockResponse);

        // when
        trendSearchUseCase.searchTrends(requestWithKeyword);

        // then
        verify(naverDataLabApiService).searchTrends(requestWithKeyword);
    }

    @Test
    @DisplayName("카테고리 필터가 올바르게 적용된다")
    void searchTrendsWithCategoryFilters() {
        // given
        TrendSearchRequest requestWithCategories = new TrendSearchRequest(
                "laptop",
                LocalDate.of(2025, 8, 1),
                LocalDate.of(2025, 8, 22),
                List.of("전자제품", "컴퓨터"),
                List.of("노트북"),
                "date",
                true,
                true,
                true
        );

        when(trendCacheService.isCached(any())).thenReturn(false);
        when(naverDataLabApiService.searchTrends(any())).thenReturn(mockNaverResponse);
        when(trendTransformService.transformToTrendSearchResponse(
                any(NaverDataLabResponse.class), 
                any(TrendSearchRequest.class), 
                anyString(), 
                anyLong(), 
                anyLong(), 
                anyString()
        )).thenReturn(mockResponse);

        // when
        trendSearchUseCase.searchTrends(requestWithCategories);

        // then
        verify(naverDataLabApiService).searchTrends(requestWithCategories);
    }

    @Test
    @DisplayName("디바이스 분포 포함 여부가 올바르게 적용된다")
    void searchTrendsWithDeviceDistribution() {
        // given
        TrendSearchRequest requestWithDevice = new TrendSearchRequest(
                "laptop",
                LocalDate.of(2025, 8, 1),
                LocalDate.of(2025, 8, 22),
                List.of(),
                List.of(),
                "date",
                true,  // includeDeviceDistribution
                false, // includeGenderDistribution
                false  // includeAgeDistribution
        );

        when(trendCacheService.isCached(any())).thenReturn(false);
        when(naverDataLabApiService.searchTrends(any())).thenReturn(mockNaverResponse);
        when(trendTransformService.transformToTrendSearchResponse(
                any(NaverDataLabResponse.class), 
                any(TrendSearchRequest.class), 
                anyString(), 
                anyLong(), 
                anyLong(), 
                anyString()
        )).thenReturn(mockResponse);

        // when
        trendSearchUseCase.searchTrends(requestWithDevice);

        // then
        verify(naverDataLabApiService).searchTrends(requestWithDevice);
    }

    @Test
    @DisplayName("시간 단위가 올바르게 적용된다")
    void searchTrendsWithTimeUnit() {
        // given
        TrendSearchRequest requestWithTimeUnit = new TrendSearchRequest(
                "laptop",
                LocalDate.of(2025, 8, 1),
                LocalDate.of(2025, 8, 22),
                List.of(),
                List.of(),
                "week", // timeUnit
                true,
                true,
                true
        );

        when(trendCacheService.isCached(any())).thenReturn(false);
        when(naverDataLabApiService.searchTrends(any())).thenReturn(mockNaverResponse);
        when(trendTransformService.transformToTrendSearchResponse(
                any(NaverDataLabResponse.class), 
                any(TrendSearchRequest.class), 
                anyString(), 
                anyLong(), 
                anyLong(), 
                anyString()
        )).thenReturn(mockResponse);

        // when
        trendSearchUseCase.searchTrends(requestWithTimeUnit);

        // then
        verify(naverDataLabApiService).searchTrends(requestWithTimeUnit);
    }
}
