package com.teambiund.bander.auth_server.auth.repository;

import com.teambiund.bander.auth_server.auth.entity.Suspend;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SuspendRepository extends JpaRepository<Suspend, String> {

  List<Suspend> findAllBySuspendUntilIsBefore(LocalDate suspendUntilBefore);

  List<Suspend> findAllBySuspendedUserId(String suspendedUserId);

  // SuspendRelease를 위한 메서드 - Auth와 suspensions를 함께 조회
  @Query("select distinct s from Suspend s " +
         "join fetch s.suspendedUser a " +
         "left join fetch a.suspensions " +
         "where s.suspendUntil < :date")
  List<Suspend> findAllWithAuthAndSuspensions(@Param("date") LocalDate date);
}
