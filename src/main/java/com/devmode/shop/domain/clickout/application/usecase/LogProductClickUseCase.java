package com.devmode.shop.domain.clickout.application.usecase;

import com.devmode.shop.domain.clickout.application.dto.request.ProductClickRequest;
import com.devmode.shop.domain.clickout.domain.service.ClickoutLoggingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
@RequiredArgsConstructor
public class LogProductClickUseCase {
    
    private final ClickoutLoggingService clickoutLoggingService;
    
    public void execute(ProductClickRequest request, HttpServletRequest httpRequest) {
        // 키워드가 비어있으면 상품명에서 추출하거나 기본값 설정
        String keyword = StringUtils.hasText(request.keyword()) ? 
                request.keyword() : 
                extractKeywordFromTitle(request.productTitle());
        
        // IP 주소 및 세션 정보 수집
        ProductClickRequest enrichedRequest = new ProductClickRequest(
                request.productId(),
                request.productTitle(),
                keyword,
                request.category(),
                request.brand(),
                request.price(),
                request.mallName(),
                request.userId(),
                getSessionId(httpRequest),
                request.userAgent(),
                getClientIpAddress(httpRequest),
                request.referrer(),
                request.searchFilters()
        );
        
        // 클릭 로깅 실행
        clickoutLoggingService.logProductClick(enrichedRequest);
    }
    
    private String extractKeywordFromTitle(String productTitle) {
        if (!StringUtils.hasText(productTitle)) {
            return "unknown";
        }
        // 상품명에서 첫 번째 단어를 키워드로 사용
        String[] words = productTitle.split("\\s+");
        return words.length > 0 ? words[0] : "unknown";
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
    
    private String getSessionId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null ? session.getId() : "anonymous";
    }
}
