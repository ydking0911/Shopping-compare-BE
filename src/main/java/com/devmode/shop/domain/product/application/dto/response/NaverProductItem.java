package com.devmode.shop.domain.product.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NaverProductItem(
    @JsonProperty("title")
    String title,
    @JsonProperty("link")
    String link,
    @JsonProperty("image")
    String image,
    @JsonProperty("lprice")
    String lprice,
    @JsonProperty("hprice")
    String hprice,
    @JsonProperty("mallName")
    String mallName,
    @JsonProperty("productId")
    String productId,
    @JsonProperty("productType")
    String productType,
    @JsonProperty("brand")
    String brand,
    @JsonProperty("maker")
    String maker,
    @JsonProperty("category1")
    String category1,
    @JsonProperty("category2")
    String category2,
    @JsonProperty("category3")
    String category3,
    @JsonProperty("category4")
    String category4,
    @JsonProperty("naverProductId")
    String naverProductId,
    @JsonProperty("reviewCount")
    String reviewCount,
    @JsonProperty("rating")
    String rating,
    @JsonProperty("shippingInfo")
    String shippingInfo,
    @JsonProperty("additionalInfo")
    String additionalInfo
) {
    public NaverProductItem {
        // 빈 문자열을 null로 변환
        title = (title != null && title.trim().isEmpty()) ? null : title;
        link = (link != null && link.trim().isEmpty()) ? null : link;
        image = (image != null && image.trim().isEmpty()) ? null : image;
        mallName = (mallName != null && mallName.trim().isEmpty()) ? null : mallName;
        productId = (productId != null && productId.trim().isEmpty()) ? null : productId;
        productType = (productType != null && productType.trim().isEmpty()) ? null : productType;
        brand = (brand != null && brand.trim().isEmpty()) ? null : brand;
        maker = (maker != null && maker.trim().isEmpty()) ? null : maker;
        category1 = (category1 != null && category1.trim().isEmpty()) ? null : category1;
        category2 = (category2 != null && category2.trim().isEmpty()) ? null : category2;
        category3 = (category3 != null && category3.trim().isEmpty()) ? null : category3;
        category4 = (category4 != null && category4.trim().isEmpty()) ? null : category4;
        naverProductId = (naverProductId != null && naverProductId.trim().isEmpty()) ? null : naverProductId;
        reviewCount = (reviewCount != null && reviewCount.trim().isEmpty()) ? null : reviewCount;
        rating = (rating != null && rating.trim().isEmpty()) ? null : rating;
        shippingInfo = (shippingInfo != null && shippingInfo.trim().isEmpty()) ? null : shippingInfo;
        additionalInfo = (additionalInfo != null && additionalInfo.trim().isEmpty()) ? null : additionalInfo;
    }
    

}
