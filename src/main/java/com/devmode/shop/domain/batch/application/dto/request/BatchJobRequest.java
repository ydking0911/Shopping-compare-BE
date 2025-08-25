package com.devmode.shop.domain.batch.application.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Map;

public record BatchJobRequest(
        @NotNull(message = "잡 타입은 필수입니다")
        String jobType, // "prefetch", "daily-aggregation", "weekly-aggregation", "monthly-aggregation"

        @Size(max = 50, message = "설명은 최대 50자까지 가능합니다")
        String description,

        LocalDateTime scheduledTime,

        @Size(max = 20, message = "파라미터는 최대 20개까지 가능합니다")
        Map<String, String> parameters,

        String triggeredBy // "manual", "scheduled", "api"
) {
    public BatchJobRequest {
        if (scheduledTime == null) {
            scheduledTime = LocalDateTime.now();
        }
        if (triggeredBy == null) {
            triggeredBy = "manual";
        }
    }
}
