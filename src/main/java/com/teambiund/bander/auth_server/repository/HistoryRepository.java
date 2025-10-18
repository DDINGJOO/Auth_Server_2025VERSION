package com.teambiund.bander.auth_server.repository;

import com.teambiund.bander.auth_server.entity.History;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoryRepository extends JpaRepository<History, Long> {}
