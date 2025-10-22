package com.teambiund.bander.auth_server.auth.repository;

import com.teambiund.bander.auth_server.auth.entity.History;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoryRepository extends JpaRepository<History, Long> {}
