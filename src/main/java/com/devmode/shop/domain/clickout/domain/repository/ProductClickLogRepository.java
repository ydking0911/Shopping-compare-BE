package com.devmode.shop.domain.clickout.domain.repository;

import com.devmode.shop.domain.clickout.domain.entity.ProductClickLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
public interface ProductClickLogRepository extends JpaRepository<ProductClickLog, Long> {
    
    // 키워드별 클릭 통계
    @Query("SELECT p.category as category, " +
           "COUNT(p) as clickCount, " +
           "COUNT(DISTINCT p.userId) as uniqueUsers, " +
           "AVG(p.price) as averagePrice " +
           "FROM ProductClickLog p " +
           "WHERE p.keyword = :keyword " +
           "AND DATE(p.clickedAt) BETWEEN :startDate AND :endDate " +
           "GROUP BY p.category")
    List<Object[]> findClickoutStatisticsByKeywordAndDateRange(
            @Param("keyword") String keyword,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    
    // 인기 상품 조회
    @Query("SELECT p.productId as productId, p.productTitle as productTitle, " +
           "COUNT(p) as clickCount, p.category as category, p.brand as brand " +
           "FROM ProductClickLog p " +
           "WHERE p.keyword = :keyword " +
           "AND DATE(p.clickedAt) BETWEEN :startDate AND :endDate " +
           "GROUP BY p.productId, p.productTitle, p.category, p.brand " +
           "ORDER BY clickCount DESC")
    List<Object[]> findPopularProductsByKeywordAndDateRange(
            @Param("keyword") String keyword,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    
    // 인기 키워드 조회
    @Query("SELECT p.keyword as keyword, COUNT(p) as clickCount " +
           "FROM ProductClickLog p " +
           "WHERE DATE(p.clickedAt) = :date " +
           "GROUP BY p.keyword " +
           "ORDER BY clickCount DESC")
    List<Object[]> findPopularKeywordsByDate(@Param("date") LocalDate date);
    
    // 카테고리별 인기도 조회
    @Query("SELECT p.category as category, COUNT(p) as clickCount " +
           "FROM ProductClickLog p " +
           "WHERE DATE(p.clickedAt) = :date " +
           "AND p.category IS NOT NULL " +
           "GROUP BY p.category " +
           "ORDER BY clickCount DESC")
    List<Object[]> findCategoryPopularityByDate(@Param("date") LocalDate date);
    
    // 사용자별 클릭 히스토리 관련 메서드들
    
    /**
     * 사용자의 특정 기간 클릭한 상품 ID 목록 조회
     */
    @Query("SELECT DISTINCT p.productId FROM ProductClickLog p " +
           "WHERE p.userId = :userId " +
           "AND DATE(p.clickedAt) BETWEEN :startDate AND :endDate " +
           "ORDER BY p.clickedAt DESC")
    List<String> findProductIdsByUserIdAndDateRange(
            @Param("userId") String userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    
    /**
     * 사용자가 선호하는 카테고리 조회
     */
    @Query("SELECT p.category FROM ProductClickLog p " +
           "WHERE p.userId = :userId " +
           "AND p.category IS NOT NULL " +
           "GROUP BY p.category " +
           "ORDER BY COUNT(p) DESC")
    List<String> findUserPreferredCategories(@Param("userId") String userId);
    
    /**
     * 사용자가 선호하는 브랜드 조회
     */
    @Query("SELECT p.brand FROM ProductClickLog p " +
           "WHERE p.userId = :userId " +
           "AND p.brand IS NOT NULL " +
           "GROUP BY p.brand " +
           "ORDER BY COUNT(p) DESC")
    List<String> findUserPreferredBrands(@Param("userId") String userId);
    
    /**
     * 사용자 선호도 기반 추천 상품 조회
     */
    @Query("SELECT DISTINCT p.productId FROM ProductClickLog p " +
           "WHERE (p.category IN :categories OR p.brand IN :brands) " +
           "AND p.userId != :userId " +
           "GROUP BY p.productId " +
           "ORDER BY COUNT(p) DESC")
    List<String> findRecommendedProductsByPreferences(
            @Param("categories") List<String> categories,
            @Param("brands") List<String> brands,
            @Param("userId") String userId
    );
    
    /**
     * 사용자를 제외한 인기 상품 조회
     */
    @Query(value = "SELECT p.product_id FROM product_click_logs p " +
           "WHERE p.user_id != :userId " +
           "GROUP BY p.product_id " +
           "ORDER BY COUNT(p) DESC " +
           "LIMIT :limit", nativeQuery = true)
    List<String> findPopularProductsExcludingUser(
            @Param("userId") String userId,
            @Param("limit") int limit
    );
    
    /**
     * 인기 상품 조회 (제한된 수)
     */
    @Query(value = "SELECT p.product_id FROM product_click_logs p " +
           "GROUP BY p.product_id " +
           "ORDER BY COUNT(p) DESC " +
           "LIMIT :limit", nativeQuery = true)
    List<String> findPopularProducts(@Param("limit") int limit);
    
    /**
     * 사용자별 클릭 통계 조회
     */
    @Query("SELECT p.category as category, " +
           "COUNT(p) as clickCount, " +
           "COUNT(DISTINCT p.userId) as uniqueUsers, " +
           "AVG(p.price) as averagePrice " +
           "FROM ProductClickLog p " +
           "WHERE p.userId = :userId " +
           "AND DATE(p.clickedAt) BETWEEN :startDate AND :endDate " +
           "GROUP BY p.category")
    List<Object[]> findUserClickStatistics(
            @Param("userId") String userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    
    /**
     * 사용자별 인기 상품 조회
     */
    @Query("SELECT p.productId as productId, p.productTitle as productTitle, " +
           "COUNT(p) as clickCount, p.category as category, p.brand as brand " +
           "FROM ProductClickLog p " +
           "WHERE p.userId = :userId " +
           "AND DATE(p.clickedAt) BETWEEN :startDate AND :endDate " +
           "GROUP BY p.productId, p.productTitle, p.category, p.brand " +
           "ORDER BY clickCount DESC")
    List<Object[]> findUserPopularProducts(
            @Param("userId") String userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    
    // 정렬 관련 메서드들
    
    /**
     * 사용자별 최저가 순 정렬
     */
    @Query("SELECT p FROM ProductClickLog p " +
           "WHERE p.userId = :userId " +
           "ORDER BY p.price ASC")
    List<ProductClickLog> findByUserIdOrderByLowestPrice(@Param("userId") String userId);
    
    /**
     * 사용자별 최고가 순 정렬
     */
    @Query("SELECT p FROM ProductClickLog p " +
           "WHERE p.userId = :userId " +
           "ORDER BY p.price DESC")
    List<ProductClickLog> findByUserIdOrderByHighestPrice(@Param("userId") String userId);
    
    /**
     * 사용자별 평점 순 정렬 (가격으로 대체)
     */
    @Query("SELECT p FROM ProductClickLog p " +
           "WHERE p.userId = :userId " +
           "ORDER BY p.price DESC")
    List<ProductClickLog> findByUserIdOrderByRating(@Param("userId") String userId);
    
    /**
     * 사용자별 리뷰 수 순 정렬 (클릭 시간으로 대체)
     */
    @Query("SELECT p FROM ProductClickLog p " +
           "WHERE p.userId = :userId " +
           "ORDER BY p.clickedAt DESC")
    List<ProductClickLog> findByUserIdOrderByReviewCount(@Param("userId") String userId);
    
    /**
     * 사용자별 우선순위 순 정렬 (클릭 시간으로 대체)
     */
    @Query("SELECT p FROM ProductClickLog p " +
           "WHERE p.userId = :userId " +
           "ORDER BY p.clickedAt DESC")
    List<ProductClickLog> findByUserIdOrderByPriority(@Param("userId") String userId);
    
    /**
     * 사용자별 생성일 순 정렬 (최신순)
     */
    @Query("SELECT p FROM ProductClickLog p " +
           "WHERE p.userId = :userId " +
           "ORDER BY p.clickedAt DESC")
    List<ProductClickLog> findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(@Param("userId") String userId);
}
