package com.devmode.shop.global.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "monitoring")
public class MonitoringProperties {

    /**
     * 헬스체크 활성화 여부
     */
    private boolean healthCheckEnabled = true;

    /**
     * 메트릭 수집 활성화 여부
     */
    private boolean metricsEnabled = true;

    /**
     * 로깅 레벨 설정
     */
    private Logging logging = new Logging();

    /**
     * 캐시 모니터링 설정
     */
    private Cache cache = new Cache();

    /**
     * 알림 설정
     */
    private Alert alert = new Alert();

    @Getter
    @Setter
    public static class Logging {
        /**
         * 집계 작업 로깅 레벨
         */
        private String aggregationLevel = "INFO";

        /**
         * 캐시 작업 로깅 레벨
         */
        private String cacheLevel = "DEBUG";

        /**
         * API 요청/응답 로깅 레벨
         */
        private String apiLevel = "INFO";

        /**
         * 성능 모니터링 로깅 레벨
         */
        private String performanceLevel = "INFO";
    }

    @Getter
    @Setter
    public static class Cache {
        /**
         * 캐시 히트율 임계값 (0.0 ~ 1.0)
         */
        private double hitRateThreshold = 0.8;

        /**
         * 캐시 통계 수집 주기 (초)
         */
        private Duration statsInterval = Duration.ofMinutes(5);

        /**
         * 캐시 키 개수 임계값
         */
        private int maxKeyCount = 10000;
    }

    @Getter
    @Setter
    public static class Alert {
        /**
         * 집계 실패 임계값 (연속 실패 횟수)
         */
        private int aggregationFailureThreshold = 3;

        /**
         * 캐시 히트율 저하 임계값
         */
        private double cacheHitRateAlertThreshold = 0.6;

        /**
         * 응답 시간 임계값 (밀리초)
         */
        private long responseTimeThreshold = 5000;
    }
}
