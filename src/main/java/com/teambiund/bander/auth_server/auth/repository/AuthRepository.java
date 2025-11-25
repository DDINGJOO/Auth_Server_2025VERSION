package com.teambiund.bander.auth_server.auth.repository;

import com.teambiund.bander.auth_server.auth.entity.Auth;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthRepository extends JpaRepository<Auth, String> {

  Optional<Auth> findByEmail(String email);

  // using Test
  @Query("select a from Auth a left join fetch a.history h where a.email = :email")
  Optional<Auth> findByEmailWithHistory(@Param("email") String email);

  @Query("select a from Auth a left join fetch a.consent c where a.email = :email")
  Optional<Auth> findByEmailWithConsent(String email);

  @Query("select a from Auth a left join fetch a.consent c where a.id = :id")
  Optional<Auth> findByIdWithConsent(@Param("id") String id);

  // LoginService를 위한 메서드 - LoginStatus와 함께 조회
  @Query("select a from Auth a left join fetch a.loginStatus where a.email = :email")
  Optional<Auth> findByEmailWithLoginStatus(@Param("email") String email);

  @Query("select a from Auth a left join fetch a.loginStatus where a.id = :id")
  Optional<Auth> findByIdWithLoginStatus(@Param("id") String id);

  // WithdrawalService를 위한 메서드 - Withdraw와 함께 조회
  @Query("select a from Auth a left join fetch a.withdraw where a.email = :email")
  Optional<Auth> findByEmailWithWithdraw(@Param("email") String email);

  void deleteByDeletedAtBefore(LocalDateTime deletedAtBefore);

  boolean existsByEmail(String email);
}
