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
}
