package com.lssgoo.mail.repository;

import com.lssgoo.mail.entity.SessionActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionActivityRepository extends JpaRepository<SessionActivity, Long> {

    @Query("SELECT sa FROM SessionActivity sa WHERE sa.session.id = :sessionId ORDER BY sa.activityTimestamp DESC")
    List<SessionActivity> findBySessionId(@Param("sessionId") Long sessionId);

    @Query("SELECT sa FROM SessionActivity sa WHERE sa.session.id = :sessionId AND sa.activityType = :activityType ORDER BY sa.activityTimestamp DESC")
    List<SessionActivity> findBySessionIdAndActivityType(@Param("sessionId") Long sessionId, @Param("activityType") String activityType);
}

