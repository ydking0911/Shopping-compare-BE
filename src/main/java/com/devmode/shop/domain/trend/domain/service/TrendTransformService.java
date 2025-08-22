package com.devmode.shop.domain.trend.domain.service;

import com.devmode.shop.domain.trend.application.dto.response.datalab.NaverDataLabResponse;
import com.devmode.shop.domain.trend.application.dto.response.trend.TrendSearchResponse;
import com.devmode.shop.domain.trend.application.dto.response.trend.TrendDataPoint;
import com.devmode.shop.domain.trend.application.dto.response.trend.TrendSummary;
import com.devmode.shop.domain.trend.application.dto.response.common.DeviceDistribution;
import com.devmode.shop.domain.trend.application.dto.response.common.GenderDistribution;
import com.devmode.shop.domain.trend.application.dto.response.common.AgeDistribution;
import com.devmode.shop.domain.trend.application.dto.response.datalab.DataLabResult;
import com.devmode.shop.domain.trend.application.dto.response.datalab.DataLabDataPoint;
import com.devmode.shop.domain.trend.application.dto.response.datalab.DataLabDistribution;
import com.devmode.shop.domain.trend.application.dto.request.TrendSearchRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TrendTransformService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public TrendSearchResponse transformToTrendSearchResponse(
            NaverDataLabResponse naverResponse,
            TrendSearchRequest request,
            String cacheStatus,
            Long responseTime,
            Long apiCallCount,
            String quotaStatus
    ) {
        List<TrendDataPoint> dataPoints = new ArrayList<>();
        
        if (naverResponse.results() != null && !naverResponse.results().isEmpty()) {
            DataLabResult result = naverResponse.results().get(0);
            if (result.data() != null) {
                for (DataLabDataPoint dataPoint : result.data()) {
                    dataPoints.add(transformDataPoint(dataPoint));
                }
            }
        }

        TrendSummary summary = buildTrendSummary(dataPoints);
        
        return new TrendSearchResponse(
                request.keyword(),
                request.startDate(),
                request.endDate(),
                naverResponse.timeUnit(),
                "naver_datalab",
                (long) dataPoints.size(),
                dataPoints,
                summary,
                cacheStatus,
                responseTime,
                apiCallCount,
                quotaStatus
        );
    }

    private TrendDataPoint transformDataPoint(DataLabDataPoint dataPoint) {
        LocalDate date = LocalDate.parse(dataPoint.period(), DATE_FORMATTER);
        
        return new TrendDataPoint(
                date,
                dataPoint.ratio(),
                dataPoint.clickCount(),
                transformDeviceDistribution(dataPoint.deviceDistribution()),
                transformGenderDistribution(dataPoint.genderDistribution()),
                transformAgeDistribution(dataPoint.ageDistribution())
        );
    }

    private DeviceDistribution transformDeviceDistribution(List<DataLabDistribution> deviceDist) {
        if (deviceDist == null || deviceDist.isEmpty()) {
            return new com.devmode.shop.domain.trend.application.dto.response.common.DeviceDistribution(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        BigDecimal mobileRatio = BigDecimal.ZERO;
        BigDecimal pcRatio = BigDecimal.ZERO;
        BigDecimal tabletRatio = BigDecimal.ZERO;

        for (DataLabDistribution dist : deviceDist) {
            switch (dist.key().toLowerCase()) {
                case "mo" -> mobileRatio = dist.ratio();
                case "pc" -> pcRatio = dist.ratio();
                case "ta" -> tabletRatio = dist.ratio();
            }
        }

        return new com.devmode.shop.domain.trend.application.dto.response.common.DeviceDistribution(mobileRatio, pcRatio, tabletRatio);
    }

    private GenderDistribution transformGenderDistribution(List<DataLabDistribution> genderDist) {
        if (genderDist == null || genderDist.isEmpty()) {
            return new com.devmode.shop.domain.trend.application.dto.response.common.GenderDistribution(BigDecimal.ZERO, BigDecimal.ZERO);
        }

        BigDecimal maleRatio = BigDecimal.ZERO;
        BigDecimal femaleRatio = BigDecimal.ZERO;

        for (DataLabDistribution dist : genderDist) {
            switch (dist.key().toLowerCase()) {
                case "m" -> maleRatio = dist.ratio();
                case "f" -> femaleRatio = dist.ratio();
            }
        }

        return new com.devmode.shop.domain.trend.application.dto.response.common.GenderDistribution(maleRatio, femaleRatio);
    }

    private AgeDistribution transformAgeDistribution(List<DataLabDistribution> ageDist) {
        if (ageDist == null || ageDist.isEmpty()) {
            return new com.devmode.shop.domain.trend.application.dto.response.common.AgeDistribution(
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
            );
        }

        BigDecimal age10s = BigDecimal.ZERO, age20s = BigDecimal.ZERO, age30s = BigDecimal.ZERO;
        BigDecimal age40s = BigDecimal.ZERO, age50s = BigDecimal.ZERO, age60s = BigDecimal.ZERO;

        for (DataLabDistribution dist : ageDist) {
            switch (dist.key()) {
                case "10" -> age10s = dist.ratio();
                case "20" -> age20s = dist.ratio();
                case "30" -> age30s = dist.ratio();
                case "40" -> age40s = dist.ratio();
                case "50" -> age50s = dist.ratio();
                case "60" -> age60s = dist.ratio();
            }
        }

        return new com.devmode.shop.domain.trend.application.dto.response.common.AgeDistribution(age10s, age20s, age30s, age40s, age50s, age60s);
    }

    private TrendSummary buildTrendSummary(List<TrendDataPoint> dataPoints) {
        if (dataPoints.isEmpty()) {
            return new com.devmode.shop.domain.trend.application.dto.response.trend.TrendSummary(
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    0L, "STABLE", BigDecimal.ZERO
            );
        }

        BigDecimal totalRatio = BigDecimal.ZERO;
        BigDecimal maxRatio = BigDecimal.ZERO;
        BigDecimal minRatio = dataPoints.get(0).ratio();
        Long totalClickCount = 0L;

        for (com.devmode.shop.domain.trend.application.dto.response.trend.TrendDataPoint point : dataPoints) {
            totalRatio = totalRatio.add(point.ratio());
            maxRatio = maxRatio.max(point.ratio());
            minRatio = minRatio.min(point.ratio());
            totalClickCount += point.clickCount();
        }

        BigDecimal avgRatio = totalRatio.divide(BigDecimal.valueOf(dataPoints.size()), 4, RoundingMode.HALF_UP);
        String trendDirection = determineTrendDirection(dataPoints);
        BigDecimal trendStrength = calculateTrendStrength(dataPoints);

        return new com.devmode.shop.domain.trend.application.dto.response.trend.TrendSummary(
                avgRatio, maxRatio, minRatio, totalClickCount, trendDirection, trendStrength
        );
    }

    private String determineTrendDirection(List<TrendDataPoint> dataPoints) {
        if (dataPoints.size() < 2) return "STABLE";
        
        BigDecimal firstRatio = dataPoints.get(0).ratio();
        BigDecimal lastRatio = dataPoints.get(dataPoints.size() - 1).ratio();
        
        if (lastRatio.compareTo(firstRatio) > 0) return "RISING";
        if (lastRatio.compareTo(firstRatio) < 0) return "FALLING";
        return "STABLE";
    }

    private BigDecimal calculateTrendStrength(List<TrendDataPoint> dataPoints) {
        if (dataPoints.size() < 2) return BigDecimal.ZERO;
        
        BigDecimal firstRatio = dataPoints.get(0).ratio();
        BigDecimal lastRatio = dataPoints.get(dataPoints.size() - 1).ratio();
        
        return lastRatio.subtract(firstRatio).abs();
    }
}
