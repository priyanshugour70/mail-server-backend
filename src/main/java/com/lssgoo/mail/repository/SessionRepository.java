package com.lssgoo.mail.repository;

import com.lssgoo.mail.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    @Query("SELECT s FROM Session s WHERE s.sessionToken = :sessionToken AND s.isActive = true")
    Optional<Session> findBySessionTokenAndActive(@Param("sessionToken") String sessionToken);

    @Query("SELECT s FROM Session s WHERE s.user.id = :userId AND s.isActive = true ORDER BY s.loginAt DESC")
    List<Session> findActiveSessionsByUserId(@Param("userId") Long userId);

    @Query("SELECT s FROM Session s WHERE s.user.id = :userId ORDER BY s.loginAt DESC")
    List<Session> findAllSessionsByUserId(@Param("userId") Long userId);

    @Query("SELECT s FROM Session s WHERE s.id = :id AND s.isActive = true")
    Optional<Session> findActiveById(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Session s SET s.isActive = false, s.logoutAt = :logoutAt, s.logoutReason = :reason WHERE s.id = :sessionId")
    void deactivateSession(@Param("sessionId") Long sessionId, @Param("logoutAt") LocalDateTime logoutAt, @Param("reason") String reason);

    @Modifying
    @Query("UPDATE Session s SET s.isActive = false, s.logoutAt = :logoutAt, s.logoutReason = :reason WHERE s.user.id = :userId AND s.isActive = true")
    void deactivateAllUserSessions(@Param("userId") Long userId, @Param("logoutAt") LocalDateTime logoutAt, @Param("reason") String reason);

    @Modifying
    @Query("UPDATE Session s SET s.lastActivityAt = :lastActivityAt WHERE s.id = :sessionId")
    void updateLastActivity(@Param("sessionId") Long sessionId, @Param("lastActivityAt") LocalDateTime lastActivityAt);

    @Query("SELECT s FROM Session s WHERE s.expiresAt < :now AND s.isActive = true")
    List<Session> findExpiredActiveSessions(@Param("now") LocalDateTime now);
}

