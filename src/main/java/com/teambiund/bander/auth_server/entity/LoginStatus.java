package com.teambiund.bander.auth_server.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Table(name = "login_status")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginStatus {

    @Id
    @Column(name = "user_id")
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId  // user_id를 PK이자 FK로 사용
    @JoinColumn(name = "user_id")
    private Auth user;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;
}
