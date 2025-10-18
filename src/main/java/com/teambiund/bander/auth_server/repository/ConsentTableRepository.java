package com.teambiund.bander.auth_server.repository;

import com.teambiund.bander.auth_server.entity.consents_name.ConsentsTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsentTableRepository extends JpaRepository<ConsentsTable, String> {}
