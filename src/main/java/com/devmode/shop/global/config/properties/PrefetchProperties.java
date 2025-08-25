package com.devmode.shop.global.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "prefetch")
public class PrefetchProperties {

    /**
     * 초기 시드 시 DataLab 상위 키워드를 한 번 로드할지 여부
     */
    private boolean seedFromDataLab = true;

    /**
     * 정적 인기 키워드 리스트 (운영 관리)
     */
    private List<String> popularKeywords = new ArrayList<>();

    /**
     * 트렌드 프리페치 크론 (기본: 30분 마다)
     */
    private String trendCron = "0 0/30 * * * *";

    /**
     * 상품 프리페치 크론 (기본: 30분 마다, 5분 오프셋)
     */
    private String productCron = "0 5/30 * * * *";

    /**
     * 트렌드 프리페치 기간(일)
     */
    private int trendDays = 30;

    /**
     * 상품 프리페치 페이지/사이즈
     */
    private int productPage = 1;
    private int productSize = 20;

    // Getter methods for properties
    public boolean isSeedFromDataLab() {
        return seedFromDataLab;
    }

    public List<String> getPopularKeywords() {
        return popularKeywords;
    }

    public String getTrendCron() {
        return trendCron;
    }

    public String getProductCron() {
        return productCron;
    }

    public int getTrendDays() {
        return trendDays;
    }

    public int getProductPage() {
        return productPage;
    }

    public int getProductSize() {
        return productSize;
    }
}


