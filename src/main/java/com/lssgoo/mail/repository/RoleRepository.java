package com.lssgoo.mail.repository;

import com.lssgoo.mail.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    @Query("SELECT r FROM Role r WHERE r.name = :name")
    Optional<Role> findByName(@Param("name") String name);

    @Query("SELECT r FROM Role r WHERE r.code = :code")
    Optional<Role> findByCode(@Param("code") String code);

    @Query("SELECT r FROM Role r WHERE r.id = :id AND r.isActive = true")
    Optional<Role> findActiveById(@Param("id") Long id);

    @Query("SELECT r FROM Role r WHERE r.isActive = true")
    java.util.List<Role> findAllActive();
}

