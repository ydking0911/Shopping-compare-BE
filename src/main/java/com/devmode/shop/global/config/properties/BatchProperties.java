package com.devmode.shop.global.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "batch")
public class BatchProperties {

    /**
     * 프리페치 잡 설정
     */
    private Prefetch prefetch = new Prefetch();

    /**
     * 집계 잡 설정
     */
    private Aggregation aggregation = new Aggregation();

    /**
     * 공통 배치 설정
     */
    private Common common = new Common();

    @Getter
    @Setter
    public static class Prefetch {
        /**
         * 프리페치 잡 활성화 여부
         */
        private boolean enabled = true;

        /**
         * 프리페치 크론 표현식
         */
        private String cron = "0 0/30 * * * *"; // 30분마다

        /**
         * 프리페치 키워드 개수
         */
        private int keywordCount = 10;

        /**
         * 프리페치 타임아웃 (초)
         */
        private Duration timeout = Duration.ofMinutes(5);

        /**
         * 프리페치 실패 시 재시도 횟수
         */
        private int maxRetries = 3;

        /**
         * 재시도 간격 (초)
         */
        private Duration retryInterval = Duration.ofMinutes(1);
    }

    @Getter
    @Setter
    public static class Aggregation {
        /**
         * 집계 잡 활성화 여부
         */
        private boolean enabled = true;

        /**
         * 일별 집계 크론 표현식
         */
        private String dailyCron = "0 0 3 * * ?"; // 매일 새벽 3시

        /**
         * 주별 집계 크론 표현식
         */
        private String weeklyCron = "0 0 4 ? * MON"; // 매주 월요일 새벽 4시

        /**
         * 월별 집계 크론 표현식
         */
        private String monthlyCron = "0 0 5 1 * ?"; // 매월 1일 새벽 5시

        /**
         * 집계 타임아웃 (초)
         */
        private Duration timeout = Duration.ofMinutes(10);

        /**
         * 집계 실패 시 재시도 횟수
         */
        private int maxRetries = 3;

        /**
         * 재시도 간격 (초)
         */
        private Duration retryInterval = Duration.ofMinutes(2);

        /**
         * 집계 배치 크기
         */
        private int batchSize = 100;

        /**
         * 집계 병렬 처리 스레드 수
         */
        private int parallelThreads = 2;
    }

    @Getter
    @Setter
    public static class Common {
        /**
         * 배치 메타데이터 테이블 생성 여부
         */
        private boolean initializeSchema = true;

        /**
         * 배치 잡 실행 로그 레벨
         */
        private String logLevel = "INFO";

        /**
         * 배치 잡 실행 결과 보관 기간 (일)
         */
        private int jobHistoryRetentionDays = 30;

        /**
         * 배치 잡 실행 결과 보관 개수
         */
        private int jobHistoryRetentionCount = 100;

        /**
         * 배치 잡 실행 중복 방지 여부
         */
        private boolean preventDuplicateExecution = true;

        /**
         * 배치 잡 실행 실패 시 알림 여부
         */
        private boolean failureNotificationEnabled = true;

        /**
         * 배치 잡 실행 성공 시 알림 여부
         */
        private boolean successNotificationEnabled = false;
    }
}
