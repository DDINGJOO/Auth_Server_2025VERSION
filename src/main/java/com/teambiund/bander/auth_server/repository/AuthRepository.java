package com.teambiund.bander.auth_server.repository;


import com.teambiund.bander.auth_server.entity.Auth;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthRepository extends JpaRepository<Auth, String>
{

    Optional<Auth> findByEmail(String email);

    //using Test
    @Query("select a from Auth a left join fetch a.history h where a.email = :email")
    Optional<Auth> findByEmailWithHistory(@Param("email") String email);

    @Query("select a from Auth a left join fetch a.consent c where a.email = :email")
    Optional<Auth> findByEmailWithConsent(String email);

    @Query("select a from Auth a left join fetch a.consent c where a.id = :id")
    Optional<Auth> findByIdWithConsent(@Param("id") String id);


    void deleteByDeletedAtBefore(LocalDateTime deletedAtBefore);

    boolean existsByEmail(String email);
}
