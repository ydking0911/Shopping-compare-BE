package com.devmode.shop.domain.trend.domain.entity;

import com.devmode.shop.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "trends")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Trend extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "search_date", nullable = false)
    private LocalDate searchDate;

    @Column(name = "keyword", nullable = false)
    private String keyword;

    @Column(name = "category_id")
    private String categoryId;

    @Column(name = "category_name")
    private String categoryName;

    @Column(name = "ratio", precision = 10, scale = 4)
    private BigDecimal ratio;

    @Column(name = "click_count")
    private Long clickCount;

    @Column(name = "device_distribution", columnDefinition = "TEXT")
    private String deviceDistribution; // JSON 형태로 저장

    @Column(name = "gender_distribution", columnDefinition = "TEXT")
    private String genderDistribution; // JSON 형태로 저장

    @Column(name = "age_distribution", columnDefinition = "TEXT")
    private String ageDistribution; // JSON 형태로 저장

    @Column(name = "source", nullable = false)
    private String source; // "naver_datalab"



    public void updateTrendData(
            BigDecimal ratio,
            Long clickCount,
            String deviceDistribution,
            String genderDistribution,
            String ageDistribution
    ) {
        this.ratio = ratio;
        this.clickCount = clickCount;
        this.deviceDistribution = deviceDistribution;
        this.genderDistribution = genderDistribution;
        this.ageDistribution = ageDistribution;
    }
}
