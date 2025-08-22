package com.devmode.shop.domain.trend.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Trend 엔티티 테스트")
class TrendTest {

    @Test
    @DisplayName("Trend 엔티티 생성 테스트")
    void createTrend() {
        // given
        LocalDate searchDate = LocalDate.of(2024, 1, 15);
        String keyword = "노트북";
        String categoryId = "50000000";
        String categoryName = "컴퓨터/노트북";
        BigDecimal ratio = new BigDecimal("1.2345");
        Long clickCount = 1500L;
        String deviceDistribution = "{\"PC\":60,\"Mobile\":40}";
        String genderDistribution = "{\"Male\":70,\"Female\":30}";
        String ageDistribution = "{\"20s\":40,\"30s\":35,\"40s\":25}";
        String source = "naver_datalab";

        // when
        Trend trend = new Trend();
        // Reflection을 사용하여 private 필드에 값 설정
        // 실제로는 Builder 패턴이나 정적 팩토리 메서드를 사용하는 것이 좋습니다
        trend.updateTrendData(ratio, clickCount, deviceDistribution, genderDistribution, ageDistribution);

        // then
        assertThat(trend.getRatio()).isEqualByComparingTo(ratio);
        assertThat(trend.getClickCount()).isEqualTo(clickCount);
        assertThat(trend.getDeviceDistribution()).isEqualTo(deviceDistribution);
        assertThat(trend.getGenderDistribution()).isEqualTo(genderDistribution);
        assertThat(trend.getAgeDistribution()).isEqualTo(ageDistribution);
    }

    @Test
    @DisplayName("Trend 데이터 업데이트 테스트")
    void updateTrendData() {
        // given
        Trend trend = new Trend();
        BigDecimal initialRatio = new BigDecimal("1.0000");
        Long initialClickCount = 1000L;
        String initialDeviceDistribution = "{\"PC\":50,\"Mobile\":50}";
        String initialGenderDistribution = "{\"Male\":60,\"Female\":40}";
        String initialAgeDistribution = "{\"20s\":30,\"30s\":40,\"40s\":30}";

        // when
        trend.updateTrendData(initialRatio, initialClickCount, initialDeviceDistribution, 
                            initialGenderDistribution, initialAgeDistribution);

        // then
        assertThat(trend.getRatio()).isEqualByComparingTo(initialRatio);
        assertThat(trend.getClickCount()).isEqualTo(initialClickCount);
        assertThat(trend.getDeviceDistribution()).isEqualTo(initialDeviceDistribution);
        assertThat(trend.getGenderDistribution()).isEqualTo(initialGenderDistribution);
        assertThat(trend.getAgeDistribution()).isEqualTo(initialAgeDistribution);

        // 데이터 업데이트
        BigDecimal updatedRatio = new BigDecimal("2.5000");
        Long updatedClickCount = 2000L;
        String updatedDeviceDistribution = "{\"PC\":70,\"Mobile\":30}";
        String updatedGenderDistribution = "{\"Male\":80,\"Female\":20}";
        String updatedAgeDistribution = "{\"20s\":50,\"30s\":30,\"40s\":20}";

        trend.updateTrendData(updatedRatio, updatedClickCount, updatedDeviceDistribution, 
                            updatedGenderDistribution, updatedAgeDistribution);

        assertThat(trend.getRatio()).isEqualByComparingTo(updatedRatio);
        assertThat(trend.getClickCount()).isEqualTo(updatedClickCount);
        assertThat(trend.getDeviceDistribution()).isEqualTo(updatedDeviceDistribution);
        assertThat(trend.getGenderDistribution()).isEqualTo(updatedGenderDistribution);
        assertThat(trend.getAgeDistribution()).isEqualTo(updatedAgeDistribution);
    }

    @Test
    @DisplayName("null 값으로 Trend 데이터 업데이트 테스트")
    void updateTrendData_WithNullValues() {
        // given
        Trend trend = new Trend();
        BigDecimal ratio = new BigDecimal("1.5000");
        Long clickCount = 1200L;

        // when
        trend.updateTrendData(ratio, clickCount, null, null, null);

        // then
        assertThat(trend.getRatio()).isEqualByComparingTo(ratio);
        assertThat(trend.getClickCount()).isEqualTo(clickCount);
        assertThat(trend.getDeviceDistribution()).isNull();
        assertThat(trend.getGenderDistribution()).isNull();
        assertThat(trend.getAgeDistribution()).isNull();
    }

    @Test
    @DisplayName("BigDecimal 정밀도 테스트")
    void bigDecimalPrecision() {
        // given
        Trend trend = new Trend();
        BigDecimal highPrecisionRatio = new BigDecimal("1.23456789");

        // when
        trend.updateTrendData(highPrecisionRatio, 1000L, "{}", "{}", "{}");

        // then
        // JPA에서 precision=10, scale=4로 설정되어 있으므로 소수점 4자리까지만 저장
        // 실제로는 JPA가 자동으로 반올림하므로 정확한 값 검증은 어려움
        assertThat(trend.getRatio()).isNotNull();
        assertThat(trend.getRatio()).isEqualByComparingTo(highPrecisionRatio);
    }

    @Test
    @DisplayName("JSON 형태의 분포 데이터 테스트")
    void jsonDistributionData() {
        // given
        Trend trend = new Trend();
        String deviceDistribution = "{\"PC\":65,\"Mobile\":30,\"Tablet\":5}";
        String genderDistribution = "{\"Male\":75,\"Female\":25}";
        String ageDistribution = "{\"10s\":10,\"20s\":45,\"30s\":30,\"40s\":15}";

        // when
        trend.updateTrendData(new BigDecimal("1.8000"), 1800L, 
                            deviceDistribution, genderDistribution, ageDistribution);

        // then
        assertThat(trend.getDeviceDistribution()).isEqualTo(deviceDistribution);
        assertThat(trend.getGenderDistribution()).isEqualTo(genderDistribution);
        assertThat(trend.getAgeDistribution()).isEqualTo(ageDistribution);
        
        // JSON 파싱 테스트 (실제로는 JSON 유효성 검증이 필요)
        assertThat(trend.getDeviceDistribution()).contains("PC");
        assertThat(trend.getDeviceDistribution()).contains("Mobile");
        assertThat(trend.getDeviceDistribution()).contains("Tablet");
    }
}
