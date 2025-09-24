package com.teambiund.bander.auth_server.entity;

import jakarta.persistence.*;
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
    @OneToOne
    @JoinColumn(name = "user_id")
    private Auth auth;

    private LocalDateTime lastLogin;
}
