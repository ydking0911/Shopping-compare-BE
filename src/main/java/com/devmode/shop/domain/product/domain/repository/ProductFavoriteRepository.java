package com.devmode.shop.domain.product.domain.repository;

import com.devmode.shop.domain.product.domain.entity.ProductFavorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductFavoriteRepository extends JpaRepository<ProductFavorite, Long> {
    
    /**
     * 사용자 ID와 상품 ID로 즐겨찾기 조회
     */
    Optional<ProductFavorite> findByUserIdAndProductId(String userId, Long productId);
    
    /**
     * 사용자 ID로 즐겨찾기한 상품 목록 조회
     */
    List<ProductFavorite> findByUserIdOrderByCreatedAtDesc(String userId);
    
    /**
     * 상품 ID로 즐겨찾기한 사용자 수 조회
     */
    long countByProductId(Long productId);
    
    /**
     * 사용자 ID로 즐겨찾기 개수 조회
     */
    long countByUserId(String userId);
    
    /**
     * 특정 상품이 즐겨찾기되었는지 확인
     */
    boolean existsByUserIdAndProductId(String userId, Long productId);
    
    /**
     * 사용자의 즐겨찾기 상품 중 특정 카테고리 상품 조회
     */
    @Query("SELECT pf FROM ProductFavorite pf JOIN pf.product p WHERE pf.userId = :userId AND p.category1 = :category")
    List<ProductFavorite> findByUserIdAndCategory(@Param("userId") String userId, @Param("category") String category);
    
    /**
     * 사용자의 즐겨찾기 상품 중 특정 브랜드 상품 조회
     */
    @Query("SELECT pf FROM ProductFavorite pf JOIN pf.product p WHERE pf.userId = :userId AND p.brand = :brand")
    List<ProductFavorite> findByUserIdAndBrand(@Param("userId") String userId, @Param("brand") String brand);
    
    /**
     * 사용자의 즐겨찾기 상품 중 특정 쇼핑몰 상품 조회
     */
    @Query("SELECT pf FROM ProductFavorite pf JOIN pf.product p WHERE pf.userId = :userId AND p.mallName = :mallName")
    List<ProductFavorite> findByUserIdAndMallName(@Param("userId") String userId, @Param("mallName") String mallName);
    
    /**
     * 사용자의 즐겨찾기 상품 중 가격 범위 내 상품 조회
     */
    @Query("SELECT pf FROM ProductFavorite pf JOIN pf.product p WHERE pf.userId = :userId AND p.lprice >= :minPrice AND p.hprice <= :maxPrice")
    List<ProductFavorite> findByUserIdAndPriceRange(@Param("userId") String userId, @Param("minPrice") Integer minPrice, @Param("maxPrice") Integer maxPrice);
    
    /**
     * 사용자의 즐겨찾기 상품 중 최저가 순으로 정렬
     */
    @Query("SELECT pf FROM ProductFavorite pf JOIN pf.product p WHERE pf.userId = :userId ORDER BY p.lprice ASC")
    List<ProductFavorite> findByUserIdOrderByLowestPrice(@Param("userId") String userId);
    
    /**
     * 사용자의 즐겨찾기 상품 중 최고가 순으로 정렬
     */
    @Query("SELECT pf FROM ProductFavorite pf JOIN pf.product p WHERE pf.userId = :userId ORDER BY p.hprice DESC")
    List<ProductFavorite> findByUserIdOrderByHighestPrice(@Param("userId") String userId);
    
    /**
     * 사용자의 즐겨찾기 상품 중 평점 순으로 정렬
     */
    @Query("SELECT pf FROM ProductFavorite pf JOIN pf.product p WHERE pf.userId = :userId AND p.rating IS NOT NULL ORDER BY p.rating DESC")
    List<ProductFavorite> findByUserIdOrderByRating(@Param("userId") String userId);
    
    /**
     * 사용자의 즐겨찾기 상품 중 리뷰 수 순으로 정렬
     */
    @Query("SELECT pf FROM ProductFavorite pf JOIN pf.product p WHERE pf.userId = :userId AND p.reviewCount IS NOT NULL ORDER BY p.reviewCount DESC")
    List<ProductFavorite> findByUserIdOrderByReviewCount(@Param("userId") String userId);
    
    /**
     * 특정 사용자의 즐겨찾기 삭제
     */
    void deleteByUserId(String userId);
    
    /**
     * 특정 상품의 모든 즐겨찾기 삭제
     */
    void deleteByProductId(Long productId);
}
