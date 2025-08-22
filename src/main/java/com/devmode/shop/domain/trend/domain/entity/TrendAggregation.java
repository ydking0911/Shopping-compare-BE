package com.devmode.shop.domain.trend.domain.entity;

import com.devmode.shop.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "trend_aggregations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TrendAggregation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "aggregation_date", nullable = false)
    private LocalDate aggregationDate;

    @Column(name = "aggregation_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private AggregationType aggregationType;

    @Column(name = "keyword", nullable = false)
    private String keyword;

    @Column(name = "category_id")
    private String categoryId;

    @Column(name = "category_name")
    private String categoryName;

    @Column(name = "total_ratio", precision = 10, scale = 4)
    private BigDecimal totalRatio;

    @Column(name = "total_click_count")
    private Long totalClickCount;

    @Column(name = "avg_ratio", precision = 10, scale = 4)
    private BigDecimal avgRatio;

    @Column(name = "max_ratio", precision = 10, scale = 4)
    private BigDecimal maxRatio;

    @Column(name = "min_ratio", precision = 10, scale = 4)
    private BigDecimal minRatio;

    @Column(name = "trend_direction")
    @Enumerated(EnumType.STRING)
    private TrendDirection trendDirection;

    @Column(name = "source", nullable = false)
    private String source; // "naver_datalab"

    public enum AggregationType {
        DAILY, WEEKLY, MONTHLY
    }

    public enum TrendDirection {
        RISING, FALLING, STABLE
    }



    public void updateAggregationData(
            BigDecimal totalRatio,
            Long totalClickCount,
            BigDecimal avgRatio,
            BigDecimal maxRatio,
            BigDecimal minRatio,
            TrendDirection trendDirection
    ) {
        this.totalRatio = totalRatio;
        this.totalClickCount = totalClickCount;
        this.avgRatio = avgRatio;
        this.maxRatio = maxRatio;
        this.minRatio = minRatio;
        this.trendDirection = trendDirection;
    }
}
