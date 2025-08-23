package com.devmode.shop.domain.favorite.domain.repository;

import com.devmode.shop.domain.favorite.domain.entity.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    
    /**
     * 사용자 ID와 상품 ID로 즐겨찾기 조회
     */
    Optional<Favorite> findByUserIdAndProductId(String userId, Long productId);
    
    /**
     * 사용자 ID로 즐겨찾기한 상품 목록 조회 (페이징)
     */
    Page<Favorite> findByUserIdAndIsActiveTrue(String userId, Pageable pageable);
    
    /**
     * 사용자 ID로 즐겨찾기한 상품 목록 조회 (전체)
     */
    List<Favorite> findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(String userId);
    
    /**
     * 상품 ID로 즐겨찾기한 사용자 수 조회
     */
    long countByProductIdAndIsActiveTrue(Long productId);
    
    /**
     * 사용자 ID로 즐겨찾기 개수 조회
     */
    long countByUserIdAndIsActiveTrue(String userId);
    
    /**
     * 특정 상품이 즐겨찾기되었는지 확인
     */
    boolean existsByUserIdAndProductIdAndIsActiveTrue(String userId, Long productId);
    
    /**
     * 사용자의 즐겨찾기 상품 중 특정 카테고리 상품 조회
     */
    @Query("SELECT f FROM Favorite f JOIN f.product p WHERE f.userId = :userId AND f.isActive = true AND p.category1 = :category")
    List<Favorite> findByUserIdAndCategory(@Param("userId") String userId, @Param("category") String category);
    
    /**
     * 사용자의 즐겨찾기 상품 중 특정 브랜드 상품 조회
     */
    @Query("SELECT f FROM Favorite f JOIN f.product p WHERE f.userId = :userId AND f.isActive = true AND p.brand = :brand")
    List<Favorite> findByUserIdAndBrand(@Param("userId") String userId, @Param("brand") String brand);
    
    /**
     * 사용자의 즐겨찾기 상품 중 특정 쇼핑몰 상품 조회
     */
    @Query("SELECT f FROM Favorite f JOIN f.product p WHERE f.userId = :userId AND f.isActive = true AND p.mallName = :mallName")
    List<Favorite> findByUserIdAndMallName(@Param("userId") String userId, @Param("mallName") String mallName);
    
    /**
     * 사용자의 즐겨찾기 상품 중 가격 범위 내 상품 조회
     */
    @Query("SELECT f FROM Favorite f JOIN f.product p WHERE f.userId = :userId AND f.isActive = true AND p.lprice >= :minPrice AND p.hprice <= :maxPrice")
    List<Favorite> findByUserIdAndPriceRange(@Param("userId") String userId, @Param("minPrice") Integer minPrice, @Param("maxPrice") Integer maxPrice);
    
    /**
     * 사용자의 즐겨찾기 상품 중 최저가 순으로 정렬
     */
    @Query("SELECT f FROM Favorite f JOIN f.product p WHERE f.userId = :userId AND f.isActive = true ORDER BY p.lprice ASC")
    List<Favorite> findByUserIdOrderByLowestPrice(@Param("userId") String userId);
    
    /**
     * 사용자의 즐겨찾기 상품 중 최고가 순으로 정렬
     */
    @Query("SELECT f FROM Favorite f JOIN f.product p WHERE f.userId = :userId AND f.isActive = true ORDER BY p.hprice DESC")
    List<Favorite> findByUserIdOrderByHighestPrice(@Param("userId") String userId);
    
    /**
     * 사용자의 즐겨찾기 상품 중 평점 순으로 정렬
     */
    @Query("SELECT f FROM Favorite f JOIN f.product p WHERE f.userId = :userId AND f.isActive = true AND p.rating IS NOT NULL ORDER BY p.rating DESC")
    List<Favorite> findByUserIdOrderByRating(@Param("userId") String userId);
    
    /**
     * 사용자의 즐겨찾기 상품 중 리뷰 수 순으로 정렬
     */
    @Query("SELECT f FROM Favorite f JOIN f.product p WHERE f.userId = :userId AND f.isActive = true AND p.reviewCount IS NOT NULL ORDER BY p.reviewCount DESC")
    List<Favorite> findByUserIdOrderByReviewCount(@Param("userId") String userId);
    
    /**
     * 사용자의 즐겨찾기 상품 중 우선순위 순으로 정렬
     */
    @Query("SELECT f FROM Favorite f WHERE f.userId = :userId AND f.isActive = true ORDER BY f.priority DESC, f.createdAt DESC")
    List<Favorite> findByUserIdOrderByPriority(@Param("userId") String userId);
    
    /**
     * 사용자의 즐겨찾기 상품 중 특정 그룹 상품 조회
     */
    List<Favorite> findByUserIdAndFavoriteGroupAndIsActiveTrue(String userId, String favoriteGroup);
    
    /**
     * 알림이 활성화된 즐겨찾기 상품 조회
     */
    List<Favorite> findByNotificationEnabledTrueAndIsActiveTrue();
    
    /**
     * 특정 상품의 모든 즐겨찾기 조회 (알림 발송용)
     */
    List<Favorite> findByProductIdAndNotificationEnabledTrueAndIsActiveTrue(Long productId);
    
    /**
     * 특정 사용자의 즐겨찾기 삭제
     */
    void deleteByUserId(String userId);
    
    /**
     * 특정 상품의 모든 즐겨찾기 삭제
     */
    void deleteByProductId(Long productId);
    
    /**
     * 카테고리별 즐겨찾기 통계 조회
     */
    @Query("SELECT p.category1 as category, COUNT(f) as count, " +
           "ROUND(COUNT(f) * 100.0 / (SELECT COUNT(f2) FROM Favorite f2 WHERE f2.userId = :userId AND f2.isActive = true), 1) as percentage " +
           "FROM Favorite f JOIN f.product p " +
           "WHERE f.userId = :userId AND f.isActive = true " +
           "GROUP BY p.category1 " +
           "ORDER BY count DESC")
    List<Object[]> getCategoryStats(@Param("userId") String userId);
    
    /**
     * 브랜드별 즐겨찾기 통계 조회
     */
    @Query("SELECT p.brand as brand, COUNT(f) as count, " +
           "ROUND(COUNT(f) * 100.0 / (SELECT COUNT(f2) FROM Favorite f2 WHERE f2.userId = :userId AND f2.isActive = true), 1) as percentage " +
           "FROM Favorite f JOIN f.product p " +
           "WHERE f.userId = :userId AND f.isActive = true " +
           "GROUP BY p.brand " +
           "ORDER BY count DESC")
    List<Object[]> getBrandStats(@Param("userId") String userId);
    
    /**
     * 쇼핑몰별 즐겨찾기 통계 조회
     */
    @Query("SELECT p.mallName as mallName, COUNT(f) as count, " +
           "ROUND(COUNT(f) * 100.0 / (SELECT COUNT(f2) FROM Favorite f2 WHERE f2.userId = :userId AND f2.isActive = true), 1) as percentage " +
           "FROM Favorite f JOIN f.product p " +
           "WHERE f.userId = :userId AND f.isActive = true " +
           "GROUP BY p.mallName " +
           "ORDER BY count DESC")
    List<Object[]> getMallStats(@Param("userId") String userId);
    
    /**
     * 가격대별 즐겨찾기 통계 조회
     */
    @Query("SELECT " +
           "CASE " +
           "  WHEN p.lprice < 10000 THEN '1만원 미만' " +
           "  WHEN p.lprice < 50000 THEN '1만원-5만원' " +
           "  WHEN p.lprice < 100000 THEN '5만원-10만원' " +
           "  WHEN p.lprice < 500000 THEN '10만원-50만원' " +
           "  ELSE '50만원 이상' " +
           "END as priceRange, " +
           "COUNT(f) as count, " +
           "ROUND(COUNT(f) * 100.0 / (SELECT COUNT(f2) FROM Favorite f2 WHERE f2.userId = :userId AND f2.isActive = true), 1) as percentage " +
           "FROM Favorite f JOIN f.product p " +
           "WHERE f.userId = :userId AND f.isActive = true " +
           "GROUP BY " +
           "CASE " +
           "  WHEN p.lprice < 10000 THEN '1만원 미만' " +
           "  WHEN p.lprice < 50000 THEN '1만원-5만원' " +
           "  WHEN p.lprice < 100000 THEN '5만원-10만원' " +
           "  WHEN p.lprice < 500000 THEN '10만원-50만원' " +
           "  ELSE '50만원 이상' " +
           "END " +
           "ORDER BY count DESC")
    List<Object[]> getPriceRangeStats(@Param("userId") String userId);
    
    /**
     * 평점별 즐겨찾기 통계 조회
     */
    @Query("SELECT " +
           "CASE " +
           "  WHEN p.rating >= 4.5 THEN '4.5점 이상' " +
           "  WHEN p.rating >= 4.0 THEN '4.0-4.4점' " +
           "  WHEN p.rating >= 3.5 THEN '3.5-3.9점' " +
           "  WHEN p.rating >= 3.0 THEN '3.0-3.4점' " +
           "  ELSE '3.0점 미만' " +
           "END as ratingRange, " +
           "COUNT(f) as count, " +
           "ROUND(COUNT(f) * 100.0 / (SELECT COUNT(f2) FROM Favorite f2 WHERE f2.userId = :userId AND f2.isActive = true), 1) as percentage " +
           "FROM Favorite f JOIN f.product p " +
           "WHERE f.userId = :userId AND f.isActive = true AND p.rating IS NOT NULL " +
           "GROUP BY " +
           "CASE " +
           "  WHEN p.rating >= 4.5 THEN '4.5점 이상' " +
           "  WHEN p.rating >= 4.0 THEN '4.0-4.4점' " +
           "  WHEN p.rating >= 3.5 THEN '3.5-3.9점' " +
           "  WHEN p.rating >= 3.0 THEN '3.0-3.4점' " +
           "  ELSE '3.0점 미만' " +
           "END " +
           "ORDER BY count DESC")
    List<Object[]> getRatingStats(@Param("userId") String userId);
}
