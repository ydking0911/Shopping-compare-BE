package com.devmode.shop.global.test;

import com.devmode.shop.global.annotation.CurrentUser;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * 테스트용 CurrentUserArgumentResolver
 * 
 * @CurrentUser 어노테이션을 시뮬레이션하여 테스트에서 사용할 수 있도록 합니다.
 * 실제 JWT 토큰 없이도 Controller 테스트가 가능합니다.
 * 
 * 사용 예시:
 * <pre>
 * {@code
 * @BeforeEach
 * void setUp() {
 *     TestCurrentUserArgumentResolver testResolver = new TestCurrentUserArgumentResolver();
 *     testResolver.setTestUserId("testUser123");
 *     
 *     mockMvc = MockMvcBuilders.standaloneSetup(controller)
 *             .setCustomArgumentResolvers(testResolver)
 *             .build();
 * }
 * }
 * </pre>
 */
public class TestCurrentUserArgumentResolver implements HandlerMethodArgumentResolver {
    
    private String testUserId = "user123"; // 기본 테스트 사용자 ID
    
    /**
     * 테스트에서 사용할 userId를 설정합니다.
     * 
     * @param userId 테스트용 사용자 ID (null 가능)
     */
    public void setTestUserId(String userId) {
        this.testUserId = userId;
    }
    
    /**
     * 현재 설정된 테스트 userId를 반환합니다.
     * 
     * @return 현재 테스트 userId
     */
    public String getTestUserId() {
        return this.testUserId;
    }
    
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(CurrentUser.class) != null
                && String.class.isAssignableFrom(parameter.getParameterType());
    }
    
    @Override
    public Object resolveArgument(MethodParameter parameter,
                               ModelAndViewContainer mavContainer,
                               NativeWebRequest webRequest,
                               WebDataBinderFactory binderFactory) throws Exception {
        // testUserId를 반환 (null일 수도 있음)
        return testUserId;
    }
}
