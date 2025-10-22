package com.teambiund.bander.auth_server.auth.repository;

import com.teambiund.bander.auth_server.auth.entity.Withdraw;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WithdrawRepository extends JpaRepository<Withdraw, String> {}
