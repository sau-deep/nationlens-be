package com.nationlens.repository;

import com.nationlens.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByMobile(String mobile);
    boolean existsByEmail(String email);
    boolean existsByMobile(String mobile);

    @Query("SELECT u FROM User u WHERE u.districtId IN :districtIds")
    List<User> findByDistrictIdIn(@Param("districtIds") List<Long> districtIds);

    long countByIsActiveTrue();
}
