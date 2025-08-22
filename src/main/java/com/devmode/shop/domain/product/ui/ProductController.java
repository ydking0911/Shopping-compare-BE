package com.devmode.shop.domain.product.ui;

import com.devmode.shop.domain.product.application.dto.request.ProductSearchRequest;
import com.devmode.shop.domain.product.application.dto.response.ProductResponse;
import com.devmode.shop.domain.product.application.dto.response.ProductSearchResponse;
import com.devmode.shop.domain.product.domain.service.ProductService;
import com.devmode.shop.global.annotation.ProductApi;
import com.devmode.shop.global.common.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController implements ProductApi {
    
    private final ProductService productService;
    
    @PostMapping("/search")
    @Override
    public BaseResponse<ProductSearchResponse> searchProducts(@Valid @RequestBody ProductSearchRequest request) {
        ProductSearchResponse response = productService.searchProducts(request);
        return BaseResponse.onSuccess(response);
    }
    
    @GetMapping("/search")
    @Override
    public BaseResponse<ProductResponse> searchProductsGet(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(defaultValue = "sim") String sort,
            @RequestParam(required = false) String excludeFilters,
            @RequestParam(defaultValue = "false") Boolean onlyNPay) {

        ProductSearchRequest request = new ProductSearchRequest(
            keyword, page, size, sort,
            excludeFilters != null && !excludeFilters.isEmpty()
                ? Arrays.asList(excludeFilters.split(","))
                : null,
            onlyNPay, null, null, null, null, null, null,
            null, null, null, null
        );

        ProductSearchResponse response = productService.searchProducts(request);
        ProductResponse productResponse = ProductResponse.create(response);
        return BaseResponse.onSuccess(productResponse);
    }
    
    @GetMapping("/health")
    @Override
    public BaseResponse<String> healthCheck() {
        String healthStatus = productService.checkHealth();
        return BaseResponse.onSuccess(healthStatus);
    }
}
