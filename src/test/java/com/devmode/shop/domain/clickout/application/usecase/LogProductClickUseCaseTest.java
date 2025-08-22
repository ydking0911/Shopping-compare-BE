package com.devmode.shop.domain.clickout.application.usecase;

import com.devmode.shop.domain.clickout.application.dto.request.ProductClickRequest;
import com.devmode.shop.domain.clickout.domain.service.ClickoutLoggingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LogProductClickUseCase 테스트")
class LogProductClickUseCaseTest {

    @Mock
    private ClickoutLoggingService clickoutLoggingService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private HttpSession httpSession;

    @InjectMocks
    private LogProductClickUseCase logProductClickUseCase;

    private ProductClickRequest productClickRequest;

    @BeforeEach
    void setUp() {
        productClickRequest = new ProductClickRequest(
                "test123",
                "테스트 상품",
                "테스트",
                "전자제품",
                "테스트브랜드",
                new BigDecimal("10000.00"),
                "테스트몰",
                "user123",
                "session123", // 이 값은 HttpServletRequest에서 추출되어 덮어씌워짐
                "Mozilla/5.0",
                "127.0.0.1", // 이 값은 HttpServletRequest에서 추출되어 덮어씌워짐
                "https://test.com",
                "{\"category\": \"electronics\"}"
        );
    }

    @Test
    @DisplayName("상품 클릭 로깅 성공 테스트")
    void execute_Success() {
        // given
        String sessionId = "extracted-session-123";
        String clientIp = "192.168.1.1";

        when(httpServletRequest.getSession(false)).thenReturn(httpSession);
        when(httpSession.getId()).thenReturn(sessionId);
        when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        when(httpServletRequest.getRemoteAddr()).thenReturn(clientIp);

        // when
        logProductClickUseCase.execute(productClickRequest, httpServletRequest);

        // then
        verify(clickoutLoggingService, times(1)).logProductClick(any(ProductClickRequest.class));
    }

    @Test
    @DisplayName("X-Forwarded-For 헤더가 있는 경우 IP 추출 테스트")
    void execute_WithXForwardedForHeader() {
        // given
        String sessionId = "extracted-session-123";
        String forwardedIp = "203.0.113.195, 70.41.3.18, 150.172.238.178";
        String expectedIp = "203.0.113.195";

        when(httpServletRequest.getSession(false)).thenReturn(httpSession);
        when(httpSession.getId()).thenReturn(sessionId);
        when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn(forwardedIp);

        // when
        logProductClickUseCase.execute(productClickRequest, httpServletRequest);

        // then
        verify(clickoutLoggingService, times(1)).logProductClick(any(ProductClickRequest.class));
    }

    @Test
    @DisplayName("세션이 없는 경우 anonymous 세션 ID 사용 테스트")
    void execute_NoSession() {
        // given
        String clientIp = "192.168.1.1";

        when(httpServletRequest.getSession(false)).thenReturn(null);
        when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        when(httpServletRequest.getRemoteAddr()).thenReturn(clientIp);

        // when
        logProductClickUseCase.execute(productClickRequest, httpServletRequest);

        // then
        verify(clickoutLoggingService, times(1)).logProductClick(any(ProductClickRequest.class));
    }

    @Test
    @DisplayName("빈 키워드가 주어진 경우 상품명에서 키워드 추출 테스트")
    void execute_EmptyKeyword() {
        // given
        ProductClickRequest requestWithEmptyKeyword = new ProductClickRequest(
                "test123",
                "테스트 상품 제목",
                "", // 빈 키워드
                "전자제품",
                "테스트브랜드",
                new BigDecimal("10000.00"),
                "테스트몰",
                "user123",
                "session123",
                "Mozilla/5.0",
                "127.0.0.1",
                "https://test.com",
                "{\"category\": \"electronics\"}"
        );

        String sessionId = "extracted-session-123";
        String clientIp = "192.168.1.1";

        when(httpServletRequest.getSession(false)).thenReturn(httpSession);
        when(httpSession.getId()).thenReturn(sessionId);
        when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        when(httpServletRequest.getRemoteAddr()).thenReturn(clientIp);

        // when
        logProductClickUseCase.execute(requestWithEmptyKeyword, httpServletRequest);

        // then
        verify(clickoutLoggingService, times(1)).logProductClick(any(ProductClickRequest.class));
    }

    @Test
    @DisplayName("null 키워드가 주어진 경우 상품명에서 키워드 추출 테스트")
    void execute_NullKeyword() {
        // given
        ProductClickRequest requestWithNullKeyword = new ProductClickRequest(
                "test123",
                "테스트 상품 제목",
                null, // null 키워드
                "전자제품",
                "테스트브랜드",
                new BigDecimal("10000.00"),
                "테스트몰",
                "user123",
                "session123",
                "Mozilla/5.0",
                "127.0.0.1",
                "https://test.com",
                "{\"category\": \"electronics\"}"
        );

        String sessionId = "extracted-session-123";
        String clientIp = "192.168.1.1";

        when(httpServletRequest.getSession(false)).thenReturn(httpSession);
        when(httpSession.getId()).thenReturn(sessionId);
        when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        when(httpServletRequest.getRemoteAddr()).thenReturn(clientIp);

        // when
        logProductClickUseCase.execute(requestWithNullKeyword, httpServletRequest);

        // then
        verify(clickoutLoggingService, times(1)).logProductClick(any(ProductClickRequest.class));
    }

    @Test
    @DisplayName("상품명도 비어있는 경우 unknown 키워드 사용 테스트")
    void execute_EmptyProductTitle() {
        // given
        ProductClickRequest requestWithEmptyTitle = new ProductClickRequest(
                "test123",
                "", // 빈 상품명
                null, // null 키워드
                "전자제품",
                "테스트브랜드",
                new BigDecimal("10000.00"),
                "테스트몰",
                "user123",
                "session123",
                "Mozilla/5.0",
                "127.0.0.1",
                "https://test.com",
                "{\"category\": \"electronics\"}"
        );

        String sessionId = "extracted-session-123";
        String clientIp = "192.168.1.1";

        when(httpServletRequest.getSession(false)).thenReturn(httpSession);
        when(httpSession.getId()).thenReturn(sessionId);
        when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        when(httpServletRequest.getRemoteAddr()).thenReturn(clientIp);

        // when
        logProductClickUseCase.execute(requestWithEmptyTitle, httpServletRequest);

        // then
        verify(clickoutLoggingService, times(1)).logProductClick(any(ProductClickRequest.class));
    }
}
