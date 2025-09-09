package com.teambiund.bander.auth_server.repository;

import com.teambiund.bander.auth_server.entity.Consent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsentRepository extends JpaRepository<Consent, String> {

}
