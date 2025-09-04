package com.teambiund.bander.auth_server.repository;


import com.teambiund.bander.auth_server.entity.Auth;
import com.teambiund.bander.auth_server.entity.UserRole;
import com.teambiund.bander.auth_server.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Role>
{
    UserRole findByRole(Role role);
}
