package com.teambiund.bander.auth_server.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Table(name = "login_status")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginStatus {

    //TODO : write sql
    @Id
    private String id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;
}
