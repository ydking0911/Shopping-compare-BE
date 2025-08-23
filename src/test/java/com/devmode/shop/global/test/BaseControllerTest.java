package com.devmode.shop.global.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Controller 테스트를 위한 베이스 클래스
 * 
 * @CurrentUser 어노테이션을 사용하는 Controller 테스트 시 상속받아 사용합니다.
 * 공통적으로 사용되는 MockMvc 설정과 TestCurrentUserArgumentResolver를 제공합니다.
 * 
 * 사용 예시:
 * <pre>
 * {@code
 * @ExtendWith(MockitoExtension.class)
 * class MyControllerTest extends BaseControllerTest<MyController> {
 * 
 *     @Mock
 *     private MyUseCase myUseCase;
 * 
 *     @InjectMocks
 *     private MyController myController;
 * 
 *     @Override
 *     protected MyController getController() {
 *         return myController;
 *     }
 * 
 *     @Test
 *     void testMethod() {
 *         // given
 *         setTestUserId("testUser123");
 *         
 *         // when & then
 *         mockMvc.perform(get("/api/test"))
 *                 .andExpect(status().isOk());
 *     }
 * }
 * }
 * </pre>
 */
public abstract class BaseControllerTest<T> {
    
    protected MockMvc mockMvc;
    protected ObjectMapper objectMapper;
    protected TestCurrentUserArgumentResolver testResolver;
    
    /**
     * 테스트할 Controller 인스턴스를 반환합니다.
     * 
     * @return Controller 인스턴스
     */
    protected abstract T getController();
    
    @BeforeEach
    void setUpBaseController() {
        // 테스트용 CurrentUserArgumentResolver 생성
        testResolver = new TestCurrentUserArgumentResolver();
        
        // MockMvc 설정
        mockMvc = MockMvcBuilders.standaloneSetup(getController())
                .setCustomArgumentResolvers(testResolver)
                .build();
        
        objectMapper = new ObjectMapper();
    }
    
    /**
     * 테스트에서 사용할 userId를 설정합니다.
     * 
     * @param userId 테스트용 사용자 ID
     */
    protected void setTestUserId(String userId) {
        testResolver.setTestUserId(userId);
    }
    
    /**
     * 현재 설정된 테스트 userId를 반환합니다.
     * 
     * @return 현재 테스트 userId
     */
    protected String getTestUserId() {
        return testResolver.getTestUserId();
    }
}
