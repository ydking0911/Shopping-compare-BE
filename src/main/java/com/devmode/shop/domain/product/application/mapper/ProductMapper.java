package com.devmode.shop.domain.product.application.mapper;

import com.devmode.shop.domain.product.domain.entity.Product;
import com.devmode.shop.domain.product.application.dto.response.ProductItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductMapper {
    
    /**
     * Product 엔티티를 ProductItem DTO로 변환
     */
    public ProductItem toProductItem(Product product) {
        if (product == null) {
            return null;
        }
        
        return new ProductItem(
            product.getProductId(),
            product.getTitle(),
            null, // description은 Product 엔티티에 없음
            product.getLprice(),
            product.getHprice(),
            null, // discountRate는 계산 필요
            null, // priceDisplay는 생성 필요
            null, // priceRange는 생성 필요
            product.getImage(),
            product.getImage(), // thumbnailUrl은 image와 동일하게 설정
            List.of(), // additionalImages는 Product 엔티티에 없음
            product.getMallName(),
            null, // mallCode는 Product 엔티티에 없음
            null, // sellerType은 Product 엔티티에 없음
            product.getCategory1(),
            product.getCategory2(),
            product.getCategory3(),
            product.getCategory4(),
            null, // categoryPath는 생성 필요
            product.getBrand(),
            null, // brandCode는 Product 엔티티에 없음
            product.getMaker(),
            product.getProductType(),
            null, // condition은 Product 엔티티에 없음
            product.getShippingInfo(),
            null, // availability는 Product 엔티티에 없음
            product.getRating() != null ? BigDecimal.valueOf(product.getRating()) : null,
            product.getReviewCount(),
            null, // ratingDisplay는 생성 필요
            product.getLink(),
            product.getUpdatedAt(),
            "database", // source는 database로 설정
            product.getSearchKeyword(),
            List.of(), // appliedFilters는 Product 엔티티에 없음
            null // searchRank는 Product 엔티티에 없음
        );
    }
    
    /**
     * Product 엔티티 리스트를 ProductItem DTO 리스트로 변환
     */
    public List<ProductItem> toProductItemList(List<Product> products) {
        if (products == null) {
            return List.of();
        }
        
        return products.stream()
            .map(this::toProductItem)
            .collect(Collectors.toList());
    }
    
    /**
     * 기본 ProductItem 생성 (상품 정보 조회 실패 시)
     */
    public static ProductItem createDefaultProductItem(Long productId) {
        return new ProductItem(
            productId.toString(),
            "상품 정보를 불러올 수 없습니다",
            "상품 정보를 불러올 수 없습니다",
            null, // price
            null, // originalPrice
            null, // discountRate
            "가격 정보 없음", // priceDisplay
            "가격 정보 없음", // priceRange
            null, // imageUrl
            null, // thumbnailUrl
            List.of(), // additionalImages
            "알 수 없음", // mallName
            null, // mallCode
            null, // sellerType
            null, // category1
            null, // category2
            null, // category3
            null, // category4
            null, // categoryPath
            null, // brand
            null, // brandCode
            null, // maker
            null, // productType
            null, // condition
            null, // shippingInfo
            null, // availability
            null, // rating
            null, // reviewCount
            null, // ratingDisplay
            null, // productUrl
            null, // lastUpdated
            "error", // source
            null, // searchKeyword
            List.of(), // appliedFilters
            null // searchRank
        );
    }
}
