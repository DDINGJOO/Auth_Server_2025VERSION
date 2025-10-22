package com.teambiund.bander.auth_server.auth.repository;

import com.teambiund.bander.auth_server.auth.entity.LoginStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoginStatusRepository extends JpaRepository<LoginStatus, Long> {}
