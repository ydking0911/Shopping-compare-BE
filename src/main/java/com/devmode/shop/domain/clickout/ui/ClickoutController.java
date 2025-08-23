package com.devmode.shop.domain.clickout.ui;

import com.devmode.shop.domain.clickout.application.dto.request.ProductClickRequest;
import com.devmode.shop.domain.clickout.application.dto.response.ClickoutAnalyticsResponse;
import com.devmode.shop.domain.clickout.application.usecase.ClickoutAnalyticsUseCase;
import com.devmode.shop.domain.clickout.application.usecase.LogProductClickUseCase;
import com.devmode.shop.global.annotation.CurrentUser;
import com.devmode.shop.global.swagger.ClickoutApi;
import com.devmode.shop.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/clickout")
@Slf4j
public class ClickoutController implements ClickoutApi {
    
    private final LogProductClickUseCase logProductClickUseCase;
    private final ClickoutAnalyticsUseCase clickoutAnalyticsUseCase;
    
    @PostMapping("/log-click")
    @Override
    public BaseResponse<Void> logProductClick(
            @Parameter(description = "상품 클릭 정보") @Valid @RequestBody ProductClickRequest request,
            @Parameter(hidden = true) HttpServletRequest httpRequest) {
        
        logProductClickUseCase.execute(request, httpRequest);
        return BaseResponse.onSuccess();
    }
    
    /**
     * 키워드별 클릭아웃 통계 조회
     */
    @GetMapping("/analytics/statistics")
    public BaseResponse<ClickoutAnalyticsResponse> getClickoutStatistics(
            @RequestParam String keyword,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        ClickoutAnalyticsResponse response = clickoutAnalyticsUseCase.getClickoutStatistics(keyword, startDate, endDate);
        return BaseResponse.onSuccess(response);
    }
    
    /**
     * 인기 키워드 조회
     */
    @GetMapping("/analytics/popular-keywords")
    public BaseResponse<List<String>> getPopularKeywords(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        List<String> keywords = clickoutAnalyticsUseCase.getPopularKeywords(date);
        return BaseResponse.onSuccess(keywords);
    }
    
    /**
     * 카테고리별 인기도 조회
     */
    @GetMapping("/analytics/category-popularity")
    public BaseResponse<List<ClickoutAnalyticsResponse.ClickoutStatistic>> getCategoryPopularity(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        List<ClickoutAnalyticsResponse.ClickoutStatistic> popularity = clickoutAnalyticsUseCase.getCategoryPopularity(date);
        return BaseResponse.onSuccess(popularity);
    }

    /**
     * 사용자별 클릭 기록 조회
     */
    @GetMapping("/user/click-history")
    public BaseResponse<List<String>> getUserClickHistory(
            @Parameter(hidden = true) @CurrentUser String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        // UseCase 호출
        List<String> clickHistory = 
            clickoutAnalyticsUseCase.getUserClickHistory(userId, startDate, endDate);
        
        return BaseResponse.onSuccess(clickHistory);
    }

    /**
     * 사용자 개인화 추천 상품 (클릭 패턴 기반)
     */
    @GetMapping("/user/recommendations")
    public BaseResponse<List<String>> getPersonalizedRecommendations(
            @Parameter(hidden = true) @CurrentUser String userId) {
        
        // UseCase 호출
        List<String> recommendations = clickoutAnalyticsUseCase.getPersonalizedRecommendations(userId);
        
        return BaseResponse.onSuccess(recommendations);
    }

    /**
     * 사용자 클릭 통계 조회
     */
    @GetMapping("/user/statistics")
    public BaseResponse<ClickoutAnalyticsResponse> getUserClickStatistics(
            @Parameter(hidden = true) @CurrentUser String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        // UseCase 호출
        ClickoutAnalyticsResponse response = clickoutAnalyticsUseCase.getUserClickStatistics(userId, startDate, endDate);
        
        return BaseResponse.onSuccess(response);
    }
}
