package com.devmode.shop.domain.clickout.ui;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.devmode.shop.domain.clickout.application.dto.response.ClickoutAnalyticsResponse;
import com.devmode.shop.domain.clickout.application.usecase.ClickoutAnalyticsUseCase;
import com.devmode.shop.domain.clickout.application.usecase.LogProductClickUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClickoutController 테스트")
class ClickoutControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private LogProductClickUseCase logProductClickUseCase;

    @Mock
    private ClickoutAnalyticsUseCase clickoutAnalyticsUseCase;

    private String productClickRequestJson;

    @InjectMocks
    private ClickoutController clickoutController;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(clickoutController).build();
        objectMapper = new ObjectMapper();
        
        productClickRequestJson = """
                {
                    "productId": "test123",
                    "productTitle": "테스트 상품",
                    "keyword": "테스트",
                    "category": "전자제품",
                    "brand": "테스트브랜드",
                    "price": 10000.00,
                    "mallName": "테스트몰",
                    "userId": "user123",
                    "sessionId": "session123",
                    "userAgent": "Mozilla/5.0",
                    "ipAddress": "127.0.0.1",
                    "referrer": "https://test.com",
                    "searchFilters": "{\\"category\\": \\"electronics\\"}"
                }
                """;
    }

    @Test
    @DisplayName("상품 클릭 로깅 API 성공 테스트")
    void logProductClick_Success() throws Exception {
        // given
        doNothing().when(logProductClickUseCase).execute(any(), any());

        // when & then
        mockMvc.perform(post("/api/clickout/log-click")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productClickRequestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.message").value("요청에 성공하였습니다."));

        verify(logProductClickUseCase, times(1)).execute(any(), any());
    }

    @Test
    @DisplayName("상품 클릭 로깅 API 유효성 검증 실패 테스트")
    void logProductClick_ValidationFailed() throws Exception {
        // given
        String invalidRequestJson = """
                {
                    "productId": "",
                    "productTitle": "",
                    "keyword": "",
                    "price": -1000
                }
                """;

        // when & then
        mockMvc.perform(post("/api/clickout/log-click")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestJson))
                .andExpect(status().isBadRequest());

        verify(logProductClickUseCase, never()).execute(any(), any());
    }

    @Test
    @DisplayName("클릭아웃 통계 조회 API 성공 테스트")
    void getClickoutStatistics_Success() throws Exception {
        // given
        String keyword = "테스트";
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);

        ClickoutAnalyticsResponse response = new ClickoutAnalyticsResponse(
                keyword,
                startDate,
                endDate,
                Arrays.asList(
                        new ClickoutAnalyticsResponse.ClickoutStatistic("전자제품", 100L, 50L, 150000.0)
                ),
                Arrays.asList(
                        new ClickoutAnalyticsResponse.PriceTrend("product1", "상품1", "UP", 5.5, startDate)
                ),
                Arrays.asList(
                        new ClickoutAnalyticsResponse.PopularProduct("product1", "상품1", 50L, "전자제품", "브랜드1")
                )
        );

        when(clickoutAnalyticsUseCase.getClickoutStatistics(eq(keyword), eq(startDate), eq(endDate)))
                .thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/clickout/analytics/statistics")
                        .param("keyword", keyword)
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.result.keyword").value(keyword))
                .andExpect(jsonPath("$.result.startDate").isArray())
                .andExpect(jsonPath("$.result.endDate").isArray())
                .andExpect(jsonPath("$.result.clickStatistics").isArray())
                .andExpect(jsonPath("$.result.clickStatistics[0].category").value("전자제품"))
                .andExpect(jsonPath("$.result.clickStatistics[0].clickCount").value(100))
                .andExpect(jsonPath("$.result.priceTrends").isArray())
                .andExpect(jsonPath("$.result.popularProducts").isArray());

        verify(clickoutAnalyticsUseCase, times(1)).getClickoutStatistics(eq(keyword), eq(startDate), eq(endDate));
    }

    @Test
    @DisplayName("클릭아웃 통계 조회 API 필수 파라미터 누락 테스트")
    void getClickoutStatistics_MissingParameters() throws Exception {
        // when & then
        mockMvc.perform(get("/api/clickout/analytics/statistics")
                        .param("keyword", "테스트"))
                .andExpect(status().isBadRequest());

        verify(clickoutAnalyticsUseCase, never()).getClickoutStatistics(any(), any(), any());
    }

    @Test
    @DisplayName("인기 키워드 조회 API 성공 테스트")
    void getPopularKeywords_Success() throws Exception {
        // given
        LocalDate date = LocalDate.of(2024, 1, 15);
        List<String> keywords = Arrays.asList("테스트", "상품", "검색");

        when(clickoutAnalyticsUseCase.getPopularKeywords(eq(date)))
                .thenReturn(keywords);

        // when & then
        mockMvc.perform(get("/api/clickout/analytics/popular-keywords")
                        .param("date", "2024-01-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result[0]").value("테스트"))
                .andExpect(jsonPath("$.result[1]").value("상품"))
                .andExpect(jsonPath("$.result[2]").value("검색"));

        verify(clickoutAnalyticsUseCase, times(1)).getPopularKeywords(eq(date));
    }

    @Test
    @DisplayName("카테고리별 인기도 조회 API 성공 테스트")
    void getCategoryPopularity_Success() throws Exception {
        // given
        LocalDate date = LocalDate.of(2024, 1, 15);
        List<ClickoutAnalyticsResponse.ClickoutStatistic> popularity = Arrays.asList(
                new ClickoutAnalyticsResponse.ClickoutStatistic("전자제품", 150L, 75L, 200000.0),
                new ClickoutAnalyticsResponse.ClickoutStatistic("의류", 120L, 60L, 80000.0)
        );

        when(clickoutAnalyticsUseCase.getCategoryPopularity(eq(date)))
                .thenReturn(popularity);

        // when & then
        mockMvc.perform(get("/api/clickout/analytics/category-popularity")
                        .param("date", "2024-01-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result[0].category").value("전자제품"))
                .andExpect(jsonPath("$.result[0].clickCount").value(150))
                .andExpect(jsonPath("$.result[0].uniqueUsers").value(75))
                .andExpect(jsonPath("$.result[0].averagePrice").value(200000.0))
                .andExpect(jsonPath("$.result[1].category").value("의류"))
                .andExpect(jsonPath("$.result[1].clickCount").value(120));

        verify(clickoutAnalyticsUseCase, times(1)).getCategoryPopularity(eq(date));
    }

    @Test
    @DisplayName("잘못된 날짜 형식으로 API 호출 시 400 에러 테스트")
    void getAnalytics_InvalidDateFormat() throws Exception {
        // when & then
        mockMvc.perform(get("/api/clickout/analytics/popular-keywords")
                        .param("date", "invalid-date"))
                .andExpect(status().isBadRequest());

        verify(clickoutAnalyticsUseCase, never()).getPopularKeywords(any());
    }

    @Test
    @DisplayName("빈 결과에 대한 분석 API 테스트")
    void getAnalytics_EmptyResult() throws Exception {
        // given
        LocalDate date = LocalDate.of(2024, 1, 15);

        when(clickoutAnalyticsUseCase.getPopularKeywords(eq(date)))
                .thenReturn(Arrays.asList());
        when(clickoutAnalyticsUseCase.getCategoryPopularity(eq(date)))
                .thenReturn(Arrays.asList());

        // when & then - 인기 키워드
        mockMvc.perform(get("/api/clickout/analytics/popular-keywords")
                        .param("date", "2024-01-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result").isEmpty());

        // when & then - 카테고리별 인기도
        mockMvc.perform(get("/api/clickout/analytics/category-popularity")
                        .param("date", "2024-01-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result").isEmpty());

        verify(clickoutAnalyticsUseCase, times(1)).getPopularKeywords(eq(date));
        verify(clickoutAnalyticsUseCase, times(1)).getCategoryPopularity(eq(date));
    }
}
