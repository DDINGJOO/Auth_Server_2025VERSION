package com.teambiund.bander.auth_server.repository;


import com.teambiund.bander.auth_server.entity.Auth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthRepository extends JpaRepository<Auth, String>
{

    Optional<Auth> findByEmail(String email);


}
