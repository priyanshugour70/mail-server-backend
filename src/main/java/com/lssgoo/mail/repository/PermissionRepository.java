package com.lssgoo.mail.repository;

import com.lssgoo.mail.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    @Query("SELECT p FROM Permission p WHERE p.name = :name")
    Optional<Permission> findByName(@Param("name") String name);

    @Query("SELECT p FROM Permission p WHERE p.code = :code")
    Optional<Permission> findByCode(@Param("code") String code);

    @Query("SELECT p FROM Permission p WHERE p.id = :id AND p.isActive = true")
    Optional<Permission> findActiveById(@Param("id") Long id);

    @Query("SELECT p FROM Permission p WHERE p.isActive = true")
    java.util.List<Permission> findAllActive();
}

