package com.devmode.shop.domain.product.domain.repository;

import com.devmode.shop.domain.product.domain.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    /**
     * 상품 ID로 상품 조회
     */
    Optional<Product> findByProductId(String productId);
    
    /**
     * 네이버 상품 ID로 상품 조회
     */
    Optional<Product> findByNaverProductId(String naverProductId);
    
    /**
     * 쇼핑몰명으로 상품 목록 조회
     */
    List<Product> findByMallName(String mallName);
    
    /**
     * 브랜드명으로 상품 목록 조회
     */
    List<Product> findByBrand(String brand);
    
    /**
     * 카테고리로 상품 목록 조회
     */
    List<Product> findByCategory1(String category1);
    
    /**
     * 가격 범위로 상품 목록 조회
     */
    @Query("SELECT p FROM Product p WHERE p.lprice >= :minPrice AND p.hprice <= :maxPrice")
    List<Product> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);
    
    /**
     * 중고 상품만 조회
     */
    List<Product> findByIsUsedTrue();
    
    /**
     * 렌탈 상품만 조회
     */
    List<Product> findByIsRentalTrue();
    
    /**
     * 해외 상품만 조회
     */
    List<Product> findByIsOverseasTrue();
    
    /**
     * NPay 전용 상품만 조회
     */
    List<Product> findByIsNPayTrue();
    
    /**
     * 키워드로 상품 제목 검색 (LIKE 검색)
     */
    @Query("SELECT p FROM Product p WHERE p.title LIKE %:keyword%")
    List<Product> findByTitleContaining(@Param("keyword") String keyword);
    
    /**
     * 최저가 기준으로 정렬하여 상품 목록 조회
     */
    @Query("SELECT p FROM Product p ORDER BY p.lprice ASC")
    List<Product> findAllOrderByLowestPrice();
    
    /**
     * 최고가 기준으로 정렬하여 상품 목록 조회
     */
    @Query("SELECT p FROM Product p ORDER BY p.hprice DESC")
    List<Product> findAllOrderByHighestPrice();
    
    /**
     * 리뷰 수 기준으로 정렬하여 상품 목록 조회
     */
    @Query("SELECT p FROM Product p WHERE p.reviewCount IS NOT NULL ORDER BY p.reviewCount DESC")
    List<Product> findAllOrderByReviewCount();
    
    /**
     * 평점 기준으로 정렬하여 상품 목록 조회
     */
    @Query("SELECT p FROM Product p WHERE p.rating IS NOT NULL ORDER BY p.rating DESC")
    List<Product> findAllOrderByRating();
    
    /**
     * 특정 쇼핑몰의 상품 중 최저가 상품 조회
     */
    @Query("SELECT p FROM Product p WHERE p.mallName = :mallName ORDER BY p.lprice ASC")
    List<Product> findByMallNameOrderByLowestPrice(@Param("mallName") String mallName);
    
    /**
     * 특정 브랜드의 상품 중 최저가 상품 조회
     */
    @Query("SELECT p FROM Product p WHERE p.brand = :brand ORDER BY p.lprice ASC")
    List<Product> findByBrandOrderByLowestPrice(@Param("brand") String brand);
}
