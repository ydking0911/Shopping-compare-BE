package com.devmode.shop.domain.clickout.application.usecase;

import com.devmode.shop.domain.clickout.application.dto.response.ClickoutAnalyticsResponse;
import com.devmode.shop.domain.clickout.domain.repository.ProductClickLogRepository;
import com.devmode.shop.domain.clickout.domain.repository.PriceHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClickoutAnalyticsUseCase 테스트")
class ClickoutAnalyticsUseCaseTest {

    @Mock
    private ProductClickLogRepository productClickLogRepository;

    @Mock
    private PriceHistoryRepository priceHistoryRepository;

    @InjectMocks
    private ClickoutAnalyticsUseCase clickoutAnalyticsUseCase;

    private String keyword;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        keyword = "테스트";
        startDate = LocalDate.of(2024, 1, 1);
        endDate = LocalDate.of(2024, 1, 31);
        testDate = LocalDate.of(2024, 1, 15);
    }

    @Test
    @DisplayName("클릭아웃 통계 조회 성공 테스트")
    void getClickoutStatistics_Success() {
        // given
        List<Object[]> clickoutStats = Arrays.asList(
                new Object[]{"전자제품", 100L, 50L, 150000.0},
                new Object[]{"의류", 80L, 40L, 50000.0}
        );

        List<Object[]> popularProducts = Arrays.asList(
                new Object[]{"product1", "상품1", 50L, "전자제품", "브랜드1"},
                new Object[]{"product2", "상품2", 30L, "의류", "브랜드2"}
        );

        List<Object[]> priceTrends = Arrays.asList(
                new Object[]{"product1", "상품1", "UP", 5.5, testDate},
                new Object[]{"product2", "상품2", "DOWN", -3.2, testDate}
        );

        when(productClickLogRepository.findClickoutStatisticsByKeywordAndDateRange(eq(keyword), eq(startDate), eq(endDate)))
                .thenReturn(clickoutStats);
        when(productClickLogRepository.findPopularProductsByKeywordAndDateRange(eq(keyword), eq(startDate), eq(endDate)))
                .thenReturn(popularProducts);
        when(priceHistoryRepository.findPriceTrendsByKeywordAndDateRange(eq(keyword), eq(startDate), eq(endDate)))
                .thenReturn(priceTrends);

        // when
        ClickoutAnalyticsResponse response = clickoutAnalyticsUseCase.getClickoutStatistics(keyword, startDate, endDate);

        // then
        assertThat(response.keyword()).isEqualTo(keyword);
        assertThat(response.startDate()).isEqualTo(startDate);
        assertThat(response.endDate()).isEqualTo(endDate);
        assertThat(response.clickStatistics()).hasSize(2);
        assertThat(response.popularProducts()).hasSize(2);
        assertThat(response.priceTrends()).hasSize(2);

        // 클릭 통계 검증
        ClickoutAnalyticsResponse.ClickoutStatistic firstStat = response.clickStatistics().get(0);
        assertThat(firstStat.category()).isEqualTo("전자제품");
        assertThat(firstStat.clickCount()).isEqualTo(100L);
        assertThat(firstStat.uniqueUsers()).isEqualTo(50L);
        assertThat(firstStat.averagePrice()).isEqualTo(150000.0);

        // 인기 상품 검증
        ClickoutAnalyticsResponse.PopularProduct firstProduct = response.popularProducts().get(0);
        assertThat(firstProduct.productId()).isEqualTo("product1");
        assertThat(firstProduct.productTitle()).isEqualTo("상품1");
        assertThat(firstProduct.clickCount()).isEqualTo(50L);
        assertThat(firstProduct.category()).isEqualTo("전자제품");
        assertThat(firstProduct.brand()).isEqualTo("브랜드1");

        // 가격 트렌드 검증
        ClickoutAnalyticsResponse.PriceTrend firstTrend = response.priceTrends().get(0);
        assertThat(firstTrend.productId()).isEqualTo("product1");
        assertThat(firstTrend.productTitle()).isEqualTo("상품1");
        assertThat(firstTrend.priceChange()).isEqualTo("UP");
        assertThat(firstTrend.priceChangePercentage()).isEqualTo(5.5);
        assertThat(firstTrend.recordedDate()).isEqualTo(testDate);
    }

    @Test
    @DisplayName("빈 결과에 대한 클릭아웃 통계 조회 테스트")
    void getClickoutStatistics_EmptyResult() {
        // given
        when(productClickLogRepository.findClickoutStatisticsByKeywordAndDateRange(eq(keyword), eq(startDate), eq(endDate)))
                .thenReturn(Arrays.asList());
        when(productClickLogRepository.findPopularProductsByKeywordAndDateRange(eq(keyword), eq(startDate), eq(endDate)))
                .thenReturn(Arrays.asList());
        when(priceHistoryRepository.findPriceTrendsByKeywordAndDateRange(eq(keyword), eq(startDate), eq(endDate)))
                .thenReturn(Arrays.asList());

        // when
        ClickoutAnalyticsResponse response = clickoutAnalyticsUseCase.getClickoutStatistics(keyword, startDate, endDate);

        // then
        assertThat(response.keyword()).isEqualTo(keyword);
        assertThat(response.startDate()).isEqualTo(startDate);
        assertThat(response.endDate()).isEqualTo(endDate);
        assertThat(response.clickStatistics()).isEmpty();
        assertThat(response.popularProducts()).isEmpty();
        assertThat(response.priceTrends()).isEmpty();
    }

    @Test
    @DisplayName("인기 키워드 조회 성공 테스트")
    void getPopularKeywords_Success() {
        // given
        List<Object[]> keywordStats = Arrays.asList(
                new Object[]{"테스트", 100L},
                new Object[]{"상품", 80L},
                new Object[]{"검색", 60L}
        );

        when(productClickLogRepository.findPopularKeywordsByDate(eq(testDate)))
                .thenReturn(keywordStats);

        // when
        List<String> keywords = clickoutAnalyticsUseCase.getPopularKeywords(testDate);

        // then
        assertThat(keywords).hasSize(3);
        assertThat(keywords).containsExactly("테스트", "상품", "검색");
    }

    @Test
    @DisplayName("빈 결과에 대한 인기 키워드 조회 테스트")
    void getPopularKeywords_EmptyResult() {
        // given
        when(productClickLogRepository.findPopularKeywordsByDate(eq(testDate)))
                .thenReturn(Arrays.asList());

        // when
        List<String> keywords = clickoutAnalyticsUseCase.getPopularKeywords(testDate);

        // then
        assertThat(keywords).isEmpty();
    }

    @Test
    @DisplayName("카테고리별 인기도 조회 성공 테스트")
    void getCategoryPopularity_Success() {
        // given
        List<Object[]> categoryStats = Arrays.asList(
                new Object[]{"전자제품", 150L},
                new Object[]{"의류", 120L},
                new Object[]{"도서", 90L}
        );

        when(productClickLogRepository.findCategoryPopularityByDate(eq(testDate)))
                .thenReturn(categoryStats);

        // when
        List<ClickoutAnalyticsResponse.ClickoutStatistic> popularity = clickoutAnalyticsUseCase.getCategoryPopularity(testDate);

        // then
        assertThat(popularity).hasSize(3);
        
        ClickoutAnalyticsResponse.ClickoutStatistic firstCategory = popularity.get(0);
        assertThat(firstCategory.category()).isEqualTo("전자제품");
        assertThat(firstCategory.clickCount()).isEqualTo(150L);
        assertThat(firstCategory.uniqueUsers()).isEqualTo(150L); // 카테고리별 조회에서는 clickCount와 동일
        assertThat(firstCategory.averagePrice()).isEqualTo(0.0); // 카테고리별 조회에서는 0.0
    }

    @Test
    @DisplayName("null 카테고리 처리 테스트")
    void getCategoryPopularity_NullCategory() {
        // given
        List<Object[]> categoryStats = Arrays.asList(
                new Object[]{null, 50L},
                new Object[]{"전자제품", 100L}
        );

        when(productClickLogRepository.findCategoryPopularityByDate(eq(testDate)))
                .thenReturn(categoryStats);

        // when
        List<ClickoutAnalyticsResponse.ClickoutStatistic> popularity = clickoutAnalyticsUseCase.getCategoryPopularity(testDate);

        // then
        assertThat(popularity).hasSize(2);
        
        ClickoutAnalyticsResponse.ClickoutStatistic firstCategory = popularity.get(0);
        assertThat(firstCategory.category()).isEqualTo("기타"); // null은 "기타"로 변환
        assertThat(firstCategory.clickCount()).isEqualTo(50L);
    }

    @Test
    @DisplayName("빈 결과에 대한 카테고리별 인기도 조회 테스트")
    void getCategoryPopularity_EmptyResult() {
        // given
        when(productClickLogRepository.findCategoryPopularityByDate(eq(testDate)))
                .thenReturn(Arrays.asList());

        // when
        List<ClickoutAnalyticsResponse.ClickoutStatistic> popularity = clickoutAnalyticsUseCase.getCategoryPopularity(testDate);

        // then
        assertThat(popularity).isEmpty();
    }
}
