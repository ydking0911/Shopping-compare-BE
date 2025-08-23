package com.devmode.shop.domain.clickout.domain.repository;

import com.devmode.shop.domain.clickout.domain.entity.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {
    
    // 상품의 최신 가격 조회
    Optional<PriceHistory> findTopByProductIdOrderByRecordedAtDesc(String productId);
    
    // 키워드별 가격 트렌드 조회
    @Query("SELECT ph.productId as productId, ph.productTitle as productTitle, " +
           "ph.priceChange as priceChange, " +
           "CASE " +
           "  WHEN ph.priceChangeAmount > 0 THEN (ph.priceChangeAmount / (ph.price - ph.priceChangeAmount)) * 100 " +
           "  ELSE 0 " +
           "END as priceChangePercentage, " +
           "DATE(ph.recordedAt) as recordedDate " +
           "FROM PriceHistory ph " +
           "WHERE ph.productId IN (" +
           "  SELECT DISTINCT pcl.productId FROM ProductClickLog pcl " +
           "  WHERE pcl.keyword = :keyword " +
           "  AND DATE(pcl.clickedAt) BETWEEN :startDate AND :endDate" +
           ") " +
           "AND DATE(ph.recordedAt) BETWEEN :startDate AND :endDate " +
           "ORDER BY ph.recordedAt DESC")
    List<Object[]> findPriceTrendsByKeywordAndDateRange(
            @Param("keyword") String keyword,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    
    // 상품별 가격 히스토리 조회
    List<PriceHistory> findByProductIdOrderByRecordedAtDesc(String productId);
    
    // 특정 기간의 가격 변화가 있는 상품 조회
    @Query("SELECT ph FROM PriceHistory ph " +
           "WHERE ph.priceChange != 'STABLE' " +
           "AND DATE(ph.recordedAt) BETWEEN :startDate AND :endDate " +
           "ORDER BY ph.recordedAt DESC")
    List<PriceHistory> findPriceChangesByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    
    // 사용자별 가격 트렌드 조회
    @Query("SELECT ph.productId as productId, ph.productTitle as productTitle, " +
           "ph.priceChange as priceChange, " +
           "CASE " +
           "  WHEN ph.priceChangeAmount > 0 THEN (ph.priceChangeAmount / (ph.price - ph.priceChangeAmount)) * 100 " +
           "  ELSE 0 " +
           "END as priceChangePercentage, " +
           "DATE(ph.recordedAt) as recordedDate " +
           "FROM PriceHistory ph " +
           "WHERE ph.productId IN (" +
           "  SELECT DISTINCT pcl.productId FROM ProductClickLog pcl " +
           "  WHERE pcl.userId = :userId " +
           "  AND DATE(pcl.clickedAt) BETWEEN :startDate AND :endDate" +
           ") " +
           "AND DATE(ph.recordedAt) BETWEEN :startDate AND :endDate " +
           "ORDER BY ph.recordedAt DESC")
    List<Object[]> findUserPriceTrends(
            @Param("userId") String userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
