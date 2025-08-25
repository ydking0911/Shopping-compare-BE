package com.devmode.shop.domain.notification.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchNotificationService {

    /**
     * 배치 잡 실패 알림 발송
     */
    public void sendFailureNotification(String jobName, String errorMessage, Map<String, Object> context) {
        try {
            log.error("[Notification] 배치 잡 실패 알림: jobName={}, error={}", jobName, errorMessage);
            
            // Email 알림 발송
            sendEmailNotification(jobName, "FAILED", errorMessage, context);
            
            log.info("[Notification] 배치 잡 실패 알림 발송 완료: {}", jobName);
            
        } catch (Exception e) {
            log.error("[Notification] 배치 잡 실패 알림 발송 중 오류: {}", e.getMessage(), e);
        }
    }

    /**
     * 배치 잡 성공 알림 발송
     */
    public void sendSuccessNotification(String jobName, Map<String, Object> context) {
        try {
            log.info("[Notification] 배치 잡 성공 알림: jobName={}", jobName);
            
            // Email 알림 발송 (설정에 따라)
            if (shouldSendSuccessNotification()) {
                sendEmailNotification(jobName, "SUCCESS", null, context);
            }
            
            log.info("[Notification] 배치 잡 성공 알림 발송 완료: {}", jobName);
            
        } catch (Exception e) {
            log.error("[Notification] 배치 잡 성공 알림 발송 중 오류: {}", e.getMessage(), e);
        }
    }

    /**
     * Email 알림 발송
     */
    private void sendEmailNotification(String jobName, String status, String errorMessage, Map<String, Object> context) {
        try {
            // TODO: Email 서비스 설정에서 SMTP 정보를 가져와서 실제 이메일 발송
            String subject = buildEmailSubject(jobName, status);
            String body = buildEmailBody(jobName, status, errorMessage, context);
            
            log.info("[Email] 알림 메시지: subject={}, body={}", subject, body);
            
            // 실제 Email 발송 로직
            // EmailService.sendEmail(recipient, subject, body);
            
        } catch (Exception e) {
            log.error("[Email] 알림 발송 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * Email 제목 구성
     */
    private String buildEmailSubject(String jobName, String status) {
        if ("FAILED".equals(status)) {
            return "[경고] 배치 잡 실패 - " + jobName;
        } else {
            return "[알림] 배치 잡 성공 - " + jobName;
        }
    }

    /**
     * Email 본문 구성
     */
    private String buildEmailBody(String jobName, String status, String errorMessage, Map<String, Object> context) {
        StringBuilder body = new StringBuilder();
        
        if ("FAILED".equals(status)) {
            body.append("배치 잡이 실패했습니다.\n\n");
            body.append("• 잡 이름: ").append(jobName).append("\n");
            body.append("• 상태: ").append(status).append("\n");
            body.append("• 오류: ").append(errorMessage).append("\n");
            body.append("• 시간: ").append(LocalDateTime.now()).append("\n");
        } else {
            body.append("배치 잡이 성공적으로 완료되었습니다.\n\n");
            body.append("• 잡 이름: ").append(jobName).append("\n");
            body.append("• 상태: ").append(status).append("\n");
            body.append("• 시간: ").append(LocalDateTime.now()).append("\n");
        }
        
        if (context != null && !context.isEmpty()) {
            body.append("• 컨텍스트:\n");
            context.forEach((key, value) -> body.append("  - ").append(key).append(": ").append(value).append("\n"));
        }
        
        return body.toString();
    }

    /**
     * 성공 알림 발송 여부 확인
     */
    private boolean shouldSendSuccessNotification() {
        // TODO: 설정에서 성공 알림 발송 여부 확인
        return false; // 기본적으로는 성공 알림 비활성화
    }
}
