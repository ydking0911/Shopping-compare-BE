package com.devmode.shop.domain.trend.domain.repository;

import com.devmode.shop.domain.trend.domain.entity.Trend;
import com.devmode.shop.domain.trend.domain.entity.TrendAggregation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrendRepository extends JpaRepository<Trend, Long> {

    @Query("SELECT t FROM Trend t WHERE t.keyword = :keyword AND t.searchDate BETWEEN :startDate AND :endDate ORDER BY t.searchDate")
    List<Trend> findByKeywordAndDateRange(
            @Param("keyword") String keyword,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT t FROM Trend t WHERE t.keyword = :keyword AND t.searchDate = :searchDate")
    Optional<Trend> findByKeywordAndDate(
            @Param("keyword") String keyword,
            @Param("searchDate") LocalDate searchDate
    );

    @Query("SELECT t FROM Trend t WHERE t.categoryId = :categoryId AND t.searchDate BETWEEN :startDate AND :endDate ORDER BY t.searchDate")
    List<Trend> findByCategoryAndDateRange(
            @Param("categoryId") String categoryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT DISTINCT t.keyword FROM Trend t WHERE t.searchDate >= :sinceDate ORDER BY t.keyword")
    List<String> findPopularKeywords(@Param("sinceDate") LocalDate sinceDate);

    @Query("SELECT t FROM Trend t WHERE t.searchDate = :searchDate ORDER BY t.ratio DESC LIMIT :limit")
    List<Trend> findTopTrendsByDate(
            @Param("searchDate") LocalDate searchDate,
            @Param("limit") int limit
    );

    // TrendAggregation 관련 쿼리
    @Query("SELECT ta FROM TrendAggregation ta WHERE ta.keyword = :keyword AND ta.aggregationType = :aggregationType AND ta.aggregationDate BETWEEN :startDate AND :endDate ORDER BY ta.aggregationDate")
    List<TrendAggregation> findAggregationsByKeywordAndType(
            @Param("keyword") String keyword,
            @Param("aggregationType") TrendAggregation.AggregationType aggregationType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT ta FROM TrendAggregation ta WHERE ta.keyword = :keyword AND ta.aggregationType = :aggregationType AND ta.aggregationDate = :aggregationDate")
    Optional<TrendAggregation> findAggregationByKeywordAndTypeAndDate(
            @Param("keyword") String keyword,
            @Param("aggregationType") TrendAggregation.AggregationType aggregationType,
            @Param("aggregationDate") LocalDate aggregationDate
    );

    @Query("SELECT ta FROM TrendAggregation ta WHERE ta.aggregationType = :aggregationType AND ta.aggregationDate = :aggregationDate ORDER BY ta.totalRatio DESC LIMIT :limit")
    List<TrendAggregation> findTopAggregationsByTypeAndDate(
            @Param("aggregationType") TrendAggregation.AggregationType aggregationType,
            @Param("aggregationDate") LocalDate aggregationDate,
            @Param("limit") int limit
    );

    @Query("SELECT ta FROM TrendAggregation ta WHERE ta.trendDirection = :trendDirection AND ta.aggregationType = :aggregationType AND ta.aggregationDate = :aggregationDate ORDER BY ta.totalRatio DESC")
    List<TrendAggregation> findAggregationsByTrendDirection(
            @Param("aggregationType") TrendAggregation.AggregationType aggregationType,
            @Param("aggregationDate") LocalDate aggregationDate,
            @Param("trendDirection") TrendAggregation.TrendDirection trendDirection
    );
}
