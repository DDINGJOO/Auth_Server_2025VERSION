package com.teambiund.bander.auth_server.repository;

import com.teambiund.bander.auth_server.entity.Auth;
import com.teambiund.bander.auth_server.entity.LoginStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoginStatusRepository extends JpaRepository<LoginStatus, Auth> {
}

