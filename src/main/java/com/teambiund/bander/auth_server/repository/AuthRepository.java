package com.teambiund.bander.auth_server.repository;


import com.teambiund.bander.auth_server.entity.Auth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface AuthRepository extends JpaRepository<Auth, String>
{

    Optional<Auth> findByEmail(String email);


    void deleteByDeletedAtAfter(LocalDateTime localDateTime);


    //using Test

    @Query("select a from Auth a left join fetch a.history h where a.email = :email")
    Optional<Auth> findByEmailWithHistory(@Param("email") String email);


    @Query("select a from Auth a left join fetch a.consent c where a.email = :email")
    Optional<Auth> findByEmailWithConsent(String email);

}
