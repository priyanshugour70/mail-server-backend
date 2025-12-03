package com.lssgoo.mail.repository;

import com.lssgoo.mail.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE u.username = :usernameOrEmail OR u.email = :usernameOrEmail")
    Optional<User> findByUsernameOrEmail(@Param("usernameOrEmail") String usernameOrEmail);

    @Query("SELECT u FROM User u WHERE u.username = :username")
    Optional<User> findByUsername(@Param("username") String username);

    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmail(@Param("email") String email);

    @Query("SELECT u FROM User u WHERE u.id = :id AND u.isActive = true")
    Optional<User> findActiveById(@Param("id") Long id);

    @Query("SELECT u FROM User u WHERE (u.username = :usernameOrEmail OR u.email = :usernameOrEmail) AND u.isActive = true")
    Optional<User> findActiveByUsernameOrEmail(@Param("usernameOrEmail") String usernameOrEmail);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.username = :username")
    boolean existsByUsername(@Param("username") String username);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.email = :email")
    boolean existsByEmail(@Param("email") String email);

    @Query("SELECT u FROM User u WHERE u.currentSessionId = :sessionId")
    Optional<User> findByCurrentSessionId(@Param("sessionId") Long sessionId);
}

