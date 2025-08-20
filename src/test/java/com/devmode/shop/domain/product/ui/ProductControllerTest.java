package com.devmode.shop.domain.product.ui;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.devmode.shop.domain.product.application.dto.request.ProductSearchRequest;
import com.devmode.shop.domain.product.application.dto.response.ProductSearchResponse;
import com.devmode.shop.domain.product.application.usecase.ProductSearchUseCase;
import com.devmode.shop.domain.product.domain.service.ProductService;
import com.devmode.shop.global.exception.ExceptionAdvice;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @Mock
    private ProductSearchUseCase productSearchUseCase;

    @InjectMocks
    private ProductController productController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController)
                .setControllerAdvice(new ExceptionAdvice())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("POST /api/products/search - 상품 검색 성공 테스트")
    void testSearchProducts_Success() throws Exception {
        // given
        ProductSearchRequest request = ProductSearchRequest.of("노트북");

        ProductSearchResponse response = ProductSearchResponse.of("노트북", Arrays.asList());

        when(productService.searchProducts(any(ProductSearchRequest.class)))
                .thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/products/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.result.keyword").value("노트북"))
                .andExpect(jsonPath("$.result.totalResults").value(150));
    }

    @Test
    @DisplayName("POST /api/products/search - 상품 검색 실패 테스트")
    void testSearchProducts_Failure() throws Exception {
        // given
        ProductSearchRequest request = ProductSearchRequest.of("노트북");

        when(productService.searchProducts(any(ProductSearchRequest.class)))
                .thenThrow(new RuntimeException("검색 실패"));

        // when & then
        mockMvc.perform(post("/api/products/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /api/products/search - GET 방식 상품 검색 성공 테스트")
    void testSearchProductsGet_Success() throws Exception {
        // given
        ProductSearchResponse response = new ProductSearchResponse(
            "노트북", 1, 20, 150, 1, 20, 8, "sim", "fresh", 
            Arrays.asList("used", "rental"), Arrays.asList(), null
        );

        when(productService.searchProducts(any(ProductSearchRequest.class)))
                .thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/products/search")
                        .param("keyword", "노트북")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.result.keyword").value("노트북"))
                .andExpect(jsonPath("$.result.totalResults").value(150));
    }

    @Test
    @DisplayName("GET /api/products/search - GET 방식 상품 검색 실패 테스트")
    void testSearchProductsGet_Failure() throws Exception {
        // given
        when(productService.searchProducts(any(ProductSearchRequest.class)))
                .thenThrow(new RuntimeException("GET 검색 실패"));

        // when & then
        mockMvc.perform(get("/api/products/search")
                        .param("keyword", "노트북"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /api/products/search - 기본값 테스트")
    void testSearchProductsGet_DefaultValues() throws Exception {
        // given
        ProductSearchResponse response = new ProductSearchResponse(
            "노트북", 1, 20, 150, 1, 20, 8, "sim", "fresh", 
            Arrays.asList(), Arrays.asList(), null
        );

        when(productService.searchProducts(any(ProductSearchRequest.class)))
                .thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/products/search")
                        .param("keyword", "노트북"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.result.keyword").value("노트북"))
                .andExpect(jsonPath("$.result.totalResults").value(150));
    }

    @Test
    @DisplayName("GET /api/products/search - excludeFilters 파싱 테스트")
    void testSearchProductsGet_ExcludeFiltersParsing() throws Exception {
        // given
        ProductSearchResponse response = new ProductSearchResponse(
            "노트북", 1, 20, 150, 1, 20, 8, "sim", "fresh", 
            Arrays.asList("used", "rental"), Arrays.asList(), null
        );

        when(productService.searchProducts(any(ProductSearchRequest.class)))
                .thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/products/search")
                        .param("keyword", "노트북")
                        .param("excludeFilters", "used,rental"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.result.keyword").value("노트북"));
    }

    @Test
    @DisplayName("GET /api/products/health - 헬스체크 테스트")
    void testHealthCheck() throws Exception {
        // given
        when(productService.checkHealth())
                .thenReturn("상품 검색 서비스가 정상적으로 작동 중입니다.");

        // when & then
        mockMvc.perform(get("/api/products/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.message").value("요청에 성공하였습니다."));
    }

    @Test
    @DisplayName("POST /api/products/search - 유효성 검증 실패 테스트")
    void testSearchProducts_ValidationFailure() throws Exception {
        // given - 빈 키워드로 요청 (유효성 검증 실패)
        ProductSearchRequest request = new ProductSearchRequest(
            "", null, null, null, null, null,
            null, null, null, null, null, null,
            null, null, null, null
        );

        // when & then
        mockMvc.perform(post("/api/products/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); // 유효성 검증 실패 시 400 응답
    }

    @Test
    @DisplayName("POST /api/products/search - 복잡한 필터 요청 테스트")
    void testSearchProducts_ComplexFilters() throws Exception {
        // given
        ProductSearchRequest request = new ProductSearchRequest(
            "노트북", 2, 30, "date", Arrays.asList("used", "overseas"), true,
            "전자제품", "컴퓨터", "노트북", "15인치", "삼성", "삼성전자",
            500000, 2000000, 4.5, 50
        );
        
        ProductSearchResponse response = ProductSearchResponse.of("노트북", Arrays.asList());

        when(productService.searchProducts(any(ProductSearchRequest.class)))
                .thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/products/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.result.keyword").value("노트북"));
    }

    @Test
    @DisplayName("Record 특성 활용 테스트")
    void testRecordFeatures() throws Exception {
        // given
        ProductSearchRequest request = ProductSearchRequest.of("노트북"); // 정적 팩토리 메서드 사용
        
        ProductSearchResponse response = ProductSearchResponse.of("노트북", Arrays.asList());

        when(productService.searchProducts(any(ProductSearchRequest.class)))
                .thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/products/search")
                        .param("keyword", "노트북"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.result.keyword").value("노트북"));
        
        // Record의 불변성 테스트
        ProductSearchRequest originalRequest = request;
        ProductSearchRequest newRequest = ProductSearchRequest.of("태블릿");
        assertNotSame(originalRequest, newRequest);
        assertEquals("노트북", originalRequest.keyword());
        assertEquals("태블릿", newRequest.keyword());
    }
}
