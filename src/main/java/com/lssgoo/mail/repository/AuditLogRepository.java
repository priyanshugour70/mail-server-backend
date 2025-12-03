package com.lssgoo.mail.repository;

import com.lssgoo.mail.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query("SELECT a FROM AuditLog a WHERE a.user.id = :userId ORDER BY a.timestamp DESC")
    List<AuditLog> findByUserId(@Param("userId") Long userId);

    @Query("SELECT a FROM AuditLog a WHERE a.session.id = :sessionId ORDER BY a.timestamp DESC")
    List<AuditLog> findBySessionId(@Param("sessionId") Long sessionId);

    @Query("SELECT a FROM AuditLog a WHERE a.action = :action ORDER BY a.timestamp DESC")
    List<AuditLog> findByAction(@Param("action") String action);

    @Query("SELECT a FROM AuditLog a WHERE a.entityType = :entityType AND a.entityId = :entityId ORDER BY a.timestamp DESC")
    List<AuditLog> findByEntity(@Param("entityType") String entityType, @Param("entityId") Long entityId);

    @Query("SELECT a FROM AuditLog a WHERE a.timestamp BETWEEN :startDate AND :endDate ORDER BY a.timestamp DESC")
    List<AuditLog> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}

