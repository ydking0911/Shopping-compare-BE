package com.devmode.shop.domain.favorite.domain.repository;

import com.devmode.shop.domain.favorite.domain.entity.FavoriteGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteGroupRepository extends JpaRepository<FavoriteGroup, Long> {
    
    /**
     * 사용자 ID로 즐겨찾기 그룹 목록 조회
     */
    List<FavoriteGroup> findByUserIdAndIsActiveTrueOrderBySortOrderAsc(String userId);
    
    /**
     * 사용자 ID와 그룹 이름으로 그룹 조회
     */
    Optional<FavoriteGroup> findByUserIdAndNameAndIsActiveTrue(String userId, String name);
    
    /**
     * 사용자 ID로 그룹 개수 조회
     */
    long countByUserIdAndIsActiveTrue(String userId);
    
    /**
     * 사용자 ID로 모든 그룹 삭제
     */
    void deleteByUserId(String userId);
}
