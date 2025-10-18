package com.teambiund.bander.auth_server.repository;

import com.teambiund.bander.auth_server.entity.Suspend;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SuspendRepository  extends JpaRepository<Suspend, String> {

    List<Suspend> findAllBySuspendUntilIsBefore(LocalDate suspendUntilBefore);

    List<Suspend> findAllBySuspendedUserId(String suspendedUserId);
}
