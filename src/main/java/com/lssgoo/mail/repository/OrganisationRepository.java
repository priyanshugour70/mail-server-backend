package com.lssgoo.mail.repository;

import com.lssgoo.mail.entity.Organisation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganisationRepository extends JpaRepository<Organisation, Long> {

    @Query("SELECT o FROM Organisation o WHERE o.name = :name")
    Optional<Organisation> findByName(@Param("name") String name);

    @Query("SELECT o FROM Organisation o WHERE o.domain = :domain")
    Optional<Organisation> findByDomain(@Param("domain") String domain);

    @Query("SELECT o FROM Organisation o WHERE o.id = :id AND o.isActive = true")
    Optional<Organisation> findActiveById(@Param("id") Long id);

    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END FROM Organisation o WHERE o.name = :name")
    boolean existsByName(@Param("name") String name);

    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END FROM Organisation o WHERE o.domain = :domain")
    boolean existsByDomain(@Param("domain") String domain);
}

