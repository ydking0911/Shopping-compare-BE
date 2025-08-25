package com.devmode.shop.domain.batch.application.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

public record BatchJobResponse(
        String jobId,
        String jobType,
        String status, // "STARTED", "COMPLETED", "FAILED"
        LocalDateTime startTime,
        LocalDateTime endTime,
        String message,
        Map<String, Object> metadata
) {}
