package com.devmode.shop.domain.trend.domain.repository;

import com.devmode.shop.domain.trend.application.dto.response.TrendAggregationResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Repository
public class TrendAggregationRepository {

    private final JdbcTemplate jdbcTemplate;

    // 허용된 정렬 컬럼들
    private static final Set<String> ALLOWED_SORT_COLUMNS = Set.of(
            "avg_ratio", "max_ratio", "min_ratio", "total_click", "strength", "keyword"
    );

    public TrendAggregationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<TrendAggregationResponse> dailyRowMapper = (rs, rowNum) ->
            new TrendAggregationResponse(
                    rs.getString("keyword"),
                    rs.getDate("agg_date").toLocalDate(),
                    rs.getBigDecimal("avg_ratio"),
                    rs.getBigDecimal("max_ratio"),
                    rs.getBigDecimal("min_ratio"),
                    rs.getLong("total_click"),
                    rs.getString("direction"),
                    rs.getBigDecimal("strength")
            );

    private final RowMapper<TrendAggregationResponse> weeklyRowMapper = (rs, rowNum) ->
            new TrendAggregationResponse(
                    rs.getString("keyword"),
                    null, // weekly doesn't have specific date
                    rs.getBigDecimal("avg_ratio"),
                    rs.getBigDecimal("max_ratio"),
                    rs.getBigDecimal("min_ratio"),
                    rs.getLong("total_click"),
                    rs.getString("direction"),
                    rs.getBigDecimal("strength")
            );

    private final RowMapper<TrendAggregationResponse> monthlyRowMapper = (rs, rowNum) ->
            new TrendAggregationResponse(
                    rs.getString("keyword"),
                    null, // monthly doesn't have specific date
                    rs.getBigDecimal("avg_ratio"),
                    rs.getBigDecimal("max_ratio"),
                    null, // monthly doesn't have min_ratio
                    rs.getLong("total_click"),
                    rs.getString("direction"),
                    rs.getBigDecimal("strength")
            );

    public void upsertDaily(String keyword,
                            LocalDate date,
                            String avgRatio,
                            String maxRatio,
                            String minRatio,
                            Long totalClick,
                            String direction,
                            String strength) {
        jdbcTemplate.update(
                "INSERT INTO trend_agg_daily(keyword, agg_date, avg_ratio, max_ratio, min_ratio, total_click, direction, strength) " +
                        "VALUES(?,?,?,?,?,?,?,?) " +
                        "ON DUPLICATE KEY UPDATE avg_ratio=VALUES(avg_ratio), max_ratio=VALUES(max_ratio), min_ratio=VALUES(min_ratio), total_click=VALUES(total_click), direction=VALUES(direction), strength=VALUES(strength)",
                keyword, date, avgRatio, maxRatio, minRatio, totalClick, direction, strength
        );
    }

    public void upsertWeekly(String keyword,
                             String yearWeek,
                             String avgRatio,
                             String maxRatio,
                             String minRatio,
                             Long totalClick,
                             String direction,
                             String strength) {
        jdbcTemplate.update(
                "INSERT INTO trend_agg_weekly(keyword, year_week, avg_ratio, max_ratio, min_ratio, total_click, direction, strength) " +
                        "VALUES(?,?,?,?,?,?,?,?) " +
                        "ON DUPLICATE KEY UPDATE avg_ratio=VALUES(avg_ratio), max_ratio=VALUES(max_ratio), min_ratio=VALUES(min_ratio), total_click=VALUES(total_click), direction=VALUES(direction), strength=VALUES(strength)",
                keyword, yearWeek, avgRatio, maxRatio, minRatio, totalClick, direction, strength
        );
    }

    public void upsertMonthly(String keyword,
                              String yearMonth,
                              String avgRatio,
                              String maxRatio,
                              Long totalClick,
                              String direction,
                              String strength) {
        jdbcTemplate.update(
                "INSERT INTO trend_agg_monthly(keyword, year_month, avg_ratio, max_ratio, total_click, direction, strength) " +
                        "VALUES(?,?,?,?,?,?,?) " +
                        "ON DUPLICATE KEY UPDATE avg_ratio=VALUES(avg_ratio), max_ratio=VALUES(max_ratio), total_click=VALUES(total_click), direction=VALUES(direction), strength=VALUES(strength)",
                keyword, yearMonth, avgRatio, maxRatio, totalClick, direction, strength
        );
    }

    public List<TrendAggregationResponse> findDailyAggregations(List<String> keywords, LocalDate startDate, LocalDate endDate, 
                                                               String sortBy, String sortOrder, int offset, int limit) {
        validateSortColumn(sortBy);
        validateSortOrder(sortOrder);
        
        StringBuilder sql = new StringBuilder(
                "SELECT keyword, agg_date, avg_ratio, max_ratio, min_ratio, total_click, direction, strength " +
                "FROM trend_agg_daily WHERE 1=1"
        );

        List<Object> params = new ArrayList<>();

        if (keywords != null && !keywords.isEmpty()) {
            sql.append(" AND keyword IN (").append("?,".repeat(keywords.size() - 1)).append("?)");
            params.addAll(keywords);
        }
        if (startDate != null) {
            sql.append(" AND agg_date >= ?");
            params.add(startDate);
        }
        if (endDate != null) {
            sql.append(" AND agg_date <= ?");
            params.add(endDate);
        }

        sql.append(" ORDER BY ").append(sortBy).append(" ").append(sortOrder);
        sql.append(" LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        return jdbcTemplate.query(sql.toString(), dailyRowMapper, params.toArray());
    }

    public List<TrendAggregationResponse> findWeeklyAggregations(List<String> keywords, String startWeek, String endWeek,
                                                                String sortBy, String sortOrder, int offset, int limit) {
        validateSortColumn(sortBy);
        validateSortOrder(sortOrder);
        
        StringBuilder sql = new StringBuilder(
                "SELECT keyword, year_week, avg_ratio, max_ratio, min_ratio, total_click, direction, strength " +
                "FROM trend_agg_weekly WHERE 1=1"
        );

        List<Object> params = new ArrayList<>();

        if (keywords != null && !keywords.isEmpty()) {
            sql.append(" AND keyword IN (").append("?,".repeat(keywords.size() - 1)).append("?)");
            params.addAll(keywords);
        }
        if (startWeek != null) {
            sql.append(" AND year_week >= ?");
            params.add(startWeek);
        }
        if (endWeek != null) {
            sql.append(" AND year_week <= ?");
            params.add(endWeek);
        }

        sql.append(" ORDER BY ").append(sortBy).append(" ").append(sortOrder);
        sql.append(" LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        return jdbcTemplate.query(sql.toString(), weeklyRowMapper, params.toArray());
    }

    public List<TrendAggregationResponse> findMonthlyAggregations(List<String> keywords, String startMonth, String endMonth,
                                                                 String sortBy, String sortOrder, int offset, int limit) {
        validateSortColumn(sortBy);
        validateSortOrder(sortOrder);
        
        StringBuilder sql = new StringBuilder(
                "SELECT keyword, year_month, avg_ratio, max_ratio, total_click, direction, strength " +
                "FROM trend_agg_monthly WHERE 1=1"
        );

        List<Object> params = new ArrayList<>();

        if (keywords != null && !keywords.isEmpty()) {
            sql.append(" AND keyword IN (").append("?,".repeat(keywords.size() - 1)).append("?)");
            params.addAll(keywords);
        }
        if (startMonth != null) {
            sql.append(" AND year_month >= ?");
            params.add(startMonth);
        }
        if (endMonth != null) {
            sql.append(" AND year_month <= ?");
            params.add(endMonth);
        }

        sql.append(" ORDER BY ").append(sortBy).append(" ").append(sortOrder);
        sql.append(" LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        return jdbcTemplate.query(sql.toString(), monthlyRowMapper, params.toArray());
    }

    public int countDailyAggregations(List<String> keywords, LocalDate startDate, LocalDate endDate) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM trend_agg_daily WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (keywords != null && !keywords.isEmpty()) {
            sql.append(" AND keyword IN (").append("?,".repeat(keywords.size() - 1)).append("?)");
            params.addAll(keywords);
        }
        if (startDate != null) {
            sql.append(" AND agg_date >= ?");
            params.add(startDate);
        }
        if (endDate != null) {
            sql.append(" AND agg_date <= ?");
            params.add(endDate);
        }

        return jdbcTemplate.queryForObject(sql.toString(), Integer.class, params.toArray());
    }

    public int countWeeklyAggregations(List<String> keywords, String startWeek, String endWeek) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM trend_agg_weekly WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (keywords != null && !keywords.isEmpty()) {
            sql.append(" AND keyword IN (").append("?,".repeat(keywords.size() - 1)).append("?)");
            params.addAll(keywords);
        }
        if (startWeek != null) {
            sql.append(" AND year_week >= ?");
            params.add(startWeek);
        }
        if (endWeek != null) {
            sql.append(" AND year_week <= ?");
            params.add(endWeek);
        }

        return jdbcTemplate.queryForObject(sql.toString(), Integer.class, params.toArray());
    }

    public int countMonthlyAggregations(List<String> keywords, String startMonth, String endMonth) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM trend_agg_monthly WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (keywords != null && !keywords.isEmpty()) {
            sql.append(" AND keyword IN (").append("?,".repeat(keywords.size() - 1)).append("?)");
            params.addAll(keywords);
        }
        if (startMonth != null) {
            sql.append(" AND year_month >= ?");
            params.add(startMonth);
        }
        if (endMonth != null) {
            sql.append(" AND year_month <= ?");
            params.add(endMonth);
        }

        return jdbcTemplate.queryForObject(sql.toString(), Integer.class, params.toArray());
    }

    /**
     * 정렬 컬럼 유효성 검사
     */
    private void validateSortColumn(String sortBy) {
        if (sortBy == null || !ALLOWED_SORT_COLUMNS.contains(sortBy)) {
            throw new IllegalArgumentException("지원하지 않는 정렬 컬럼: " + sortBy);
        }
    }

    /**
     * 정렬 순서 유효성 검사
     */
    private void validateSortOrder(String sortOrder) {
        if (sortOrder == null || (!sortOrder.equalsIgnoreCase("asc") && !sortOrder.equalsIgnoreCase("desc"))) {
            throw new IllegalArgumentException("지원하지 않는 정렬 순서: " + sortOrder);
        }
    }
}


