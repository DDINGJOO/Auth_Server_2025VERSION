package com.teambiund.bander.auth_server.entity;


import com.teambiund.bander.auth_server.enums.Provider;
import com.teambiund.bander.auth_server.enums.Role;
import com.teambiund.bander.auth_server.enums.Status;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Table( name = "auth")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Auth
{

    @Id
    @Column(name = "id")
    private String id; // shard key

    @Column(name = "email")
    private String email;


    @Column(name = "provider")
    @Enumerated(EnumType.STRING)
    private Provider provider;


    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status status;


    @Column(name = "password")
    @Nullable
    private String password;


    @Column(name = "phone_number")
    @Nullable
    private String phoneNumber;


    @Version
    @Column(name = "version")
    private int version; // 낙관적 락 버전 정보


    @Column(name = "created_at")
    private LocalDateTime createdAt;


    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;


    @Enumerated(EnumType.STRING)
    @Column(name = "user_role")
    private Role userRole;


    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<History> history = new ArrayList<>();


    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Consent> consent = new ArrayList<>();

}
