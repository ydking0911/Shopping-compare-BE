package com.devmode.shop.domain.notification.domain.service;

import com.devmode.shop.domain.favorite.domain.entity.Favorite;
import com.devmode.shop.domain.favorite.domain.repository.FavoriteRepository;
import com.devmode.shop.domain.notification.domain.entity.Notification;
import com.devmode.shop.domain.notification.domain.entity.WebPushSubscription;
import com.devmode.shop.domain.notification.domain.repository.NotificationRepository;
import com.devmode.shop.domain.notification.domain.repository.WebPushSubscriptionRepository;
import com.devmode.shop.domain.product.domain.entity.Product;
import com.devmode.shop.global.config.properties.WebPushProperties;
import com.devmode.shop.global.exception.RestApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebPushNotificationServiceTest {

    @Mock
    private WebPushSubscriptionRepository webPushSubscriptionRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private WebPushProperties webPushProperties;

    @InjectMocks
    private WebPushNotificationService webPushNotificationService;

    private String testUserId;
    private String testProductId;
    private WebPushSubscription testSubscription;
    private Product testProduct;
    private Favorite testFavorite;

    @BeforeEach
    void setUp() {
        testUserId = "test-user-123";
        testProductId = "12345";
        
        testSubscription = WebPushSubscription.builder()
                .id(1L)
                .userId(testUserId)
                .endpoint("https://fcm.googleapis.com/fcm/send/test-token")
                .p256dhKey("test-p256dh-key")
                .authSecret("test-auth-key")
                .isActive(true)
                .build();

        testProduct = Product.builder()
                .id(Long.valueOf(testProductId))
                .title("테스트 상품")
                .lprice(new BigDecimal("10000"))
                .hprice(new BigDecimal("15000"))
                .brand("테스트 브랜드")
                .category1("전자제품")
                .category2("스마트폰")
                .category3("갤럭시")
                .category4("S24")
                .mallName("테스트 몰")
                .productId(testProductId)
                .link("https://test.com/product/12345")
                .image("https://test.com/image/12345.jpg")
                .rating(4.5)
                .reviewCount(100)
                .build();

        testFavorite = Favorite.builder()
                .id(1L)
                .userId(testUserId)
                .product(testProduct)
                .memo("테스트 즐겨찾기")
                .favoriteGroup("전자제품")
                .notificationEnabled(true)
                .targetPrice(8000)
                .priority(1)
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("가격 변동 알림 발송 성공")
    void sendPriceChangeNotification_Success() {
        // given
        BigDecimal oldPrice = new BigDecimal("10000");
        BigDecimal newPrice = new BigDecimal("8000");
        
        when(favoriteRepository.findByProductIdAndNotificationEnabledTrueAndIsActiveTrue(Long.valueOf(testProductId)))
                .thenReturn(List.of(testFavorite));
        when(webPushSubscriptionRepository.findByUserIdAndIsActiveTrue(testUserId))
                .thenReturn(List.of(testSubscription));

        // when
        webPushNotificationService.sendPriceChangeNotification(testProductId, oldPrice, newPrice);

        // then
        verify(favoriteRepository).findByProductIdAndNotificationEnabledTrueAndIsActiveTrue(Long.valueOf(testProductId));
        verify(webPushSubscriptionRepository).findByUserIdAndIsActiveTrue(testUserId);
    }

    @Test
    @DisplayName("알림이 활성화된 즐겨찾기가 없는 경우 알림 발송 안함")
    void sendPriceChangeNotification_NoActiveFavorites_NoNotificationSent() {
        // given
        BigDecimal oldPrice = new BigDecimal("10000");
        BigDecimal newPrice = new BigDecimal("8000");
        
        when(favoriteRepository.findByProductIdAndNotificationEnabledTrueAndIsActiveTrue(Long.valueOf(testProductId)))
                .thenReturn(List.of());

        // when
        webPushNotificationService.sendPriceChangeNotification(testProductId, oldPrice, newPrice);

        // then
        verify(favoriteRepository).findByProductIdAndNotificationEnabledTrueAndIsActiveTrue(Long.valueOf(testProductId));
        verify(webPushSubscriptionRepository, never()).findByUserIdAndIsActiveTrue(any());
    }

    @Test
    @DisplayName("Web Push 구독 등록 성공")
    void registerWebPushSubscription_Success() {
        // given
        when(webPushSubscriptionRepository.findByEndpoint(testSubscription.getEndpoint()))
                .thenReturn(Optional.empty());
        when(webPushSubscriptionRepository.save(any(WebPushSubscription.class)))
                .thenReturn(testSubscription);

        // when
        webPushNotificationService.registerWebPushSubscription(testUserId, testSubscription.getEndpoint(), 
                testSubscription.getP256dhKey(), testSubscription.getAuthSecret(), "Chrome");

        // then
        verify(webPushSubscriptionRepository).findByEndpoint(testSubscription.getEndpoint());
        verify(webPushSubscriptionRepository).save(any(WebPushSubscription.class));
    }

    @Test
    @DisplayName("Web Push 구독 삭제 성공")
    void deleteWebPushSubscription_Success() {
        // given
        when(webPushSubscriptionRepository.findByEndpoint(testSubscription.getEndpoint()))
                .thenReturn(Optional.of(testSubscription));

        // when
        webPushNotificationService.deleteWebPushSubscription(testUserId, testSubscription.getEndpoint());

        // then
        verify(webPushSubscriptionRepository).findByEndpoint(testSubscription.getEndpoint());
        verify(webPushSubscriptionRepository).save(any(WebPushSubscription.class));
    }

    @Test
    @DisplayName("Web Push 구독을 찾을 수 없는 경우 로그만 남기고 조용히 처리")
    void deleteWebPushSubscription_NotFound_LogsWarningAndReturns() {
        // given
        when(webPushSubscriptionRepository.findByEndpoint(testSubscription.getEndpoint()))
                .thenReturn(Optional.empty());

        // when & then - 예외가 발생하지 않아야 함
        assertThatCode(() -> webPushNotificationService.deleteWebPushSubscription(testUserId, testSubscription.getEndpoint()))
                .doesNotThrowAnyException();

        verify(webPushSubscriptionRepository).findByEndpoint(testSubscription.getEndpoint());
        verify(webPushSubscriptionRepository, never()).save(any());
    }

    @Test
    @DisplayName("알림 읽음 처리 성공")
    void markNotificationAsRead_Success() {
        // given
        Long notificationId = 1L;
        Notification testNotification = Notification.builder()
                .id(notificationId)
                .userId(testUserId)
                .title("가격 변동 알림")
                .body("상품 가격이 변동되었습니다.")
                .webPushEndpoint(testSubscription.getEndpoint())
                .isRead(false)
                .build();

        when(notificationRepository.findById(notificationId))
                .thenReturn(Optional.of(testNotification));

        // when
        webPushNotificationService.markNotificationAsRead(notificationId, testUserId);

        // then
        verify(notificationRepository).findById(notificationId);
    }

    @Test
    @DisplayName("알림을 찾을 수 없는 경우 예외 발생")
    void markNotificationAsRead_NotFound_ThrowsException() {
        // given
        Long notificationId = 1L;
        when(notificationRepository.findById(notificationId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> webPushNotificationService.markNotificationAsRead(notificationId, testUserId))
                .isInstanceOf(RestApiException.class);

        verify(notificationRepository).findById(notificationId);
        verify(notificationRepository, never()).save(any());
    }
}
