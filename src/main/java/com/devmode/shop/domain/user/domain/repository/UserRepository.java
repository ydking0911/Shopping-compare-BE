package com.devmode.shop.domain.user.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.devmode.shop.domain.user.domain.entity.User;

public interface UserRepository extends JpaRepository<User, String> {

    @Query("select count(u) > 0 from User u where u.email = :email")
    Boolean existsByEmail(@Param("email") String email);

    @Query("select u from User u where u.email = :email")
    Optional<User> findByEmail(@Param("email") String email);
    
    @Query("select count(u) > 0 from User u where u.userId = :userId")
    Boolean existsByUserId(@Param("userId") String userId);
    
    @Query("select u from User u where u.userId = :userId")
    Optional<User> findByUserId(@Param("userId") String userId);
}