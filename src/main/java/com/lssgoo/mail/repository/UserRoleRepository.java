package com.lssgoo.mail.repository;

import com.lssgoo.mail.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    @Query("SELECT ur FROM UserRole ur WHERE ur.user.id = :userId AND ur.isActive = true")
    List<UserRole> findActiveByUserId(@Param("userId") Long userId);

    @Query("SELECT ur FROM UserRole ur WHERE ur.user.id = :userId")
    List<UserRole> findByUserId(@Param("userId") Long userId);

    @Query("SELECT ur FROM UserRole ur WHERE ur.role.id = :roleId AND ur.isActive = true")
    List<UserRole> findActiveByRoleId(@Param("roleId") Long roleId);

    @Query("SELECT ur FROM UserRole ur WHERE ur.user.id = :userId AND ur.role.id = :roleId AND ur.isActive = true")
    java.util.Optional<UserRole> findByUserIdAndRoleId(@Param("userId") Long userId, @Param("roleId") Long roleId);
}

