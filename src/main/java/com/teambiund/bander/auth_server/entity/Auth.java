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


    @Builder.Default
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<History> history = new ArrayList<>();


    @Builder.Default
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Consent> consent = new ArrayList<>();


    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Withdraw withdraw;


    @Builder.Default
    @OneToMany(mappedBy = "suspendedUser", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Suspend> suspensions = new ArrayList<>();


    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private LoginStatus loginStatus;


    // === 편의 메서드 (Convenience Methods) ===

    /**
     * History 추가 편의 메서드
     * 양방향 연관관계 설정
     */
    public void addHistory(History history) {
        this.history.add(history);
        history.setUser(this);
    }

    /**
     * Consent 추가 편의 메서드
     * 양방향 연관관계 설정
     */
    public void addConsent(Consent consent) {
        this.consent.add(consent);
        consent.setUser(this);
    }

    /**
     * Consent 제거 편의 메서드
     */
    public void removeConsent(Consent consent) {
        this.consent.remove(consent);
        consent.setUser(null);
    }

    /**
     * Withdraw 설정 편의 메서드
     * 양방향 연관관계 설정
     */
    public void setWithdraw(Withdraw withdraw) {
        this.withdraw = withdraw;
        if (withdraw != null) {
            withdraw.setUser(this);
        }
    }

    /**
     * Suspend 추가 편의 메서드
     * 양방향 연관관계 설정
     */
    public void addSuspension(Suspend suspend) {
        this.suspensions.add(suspend);
        suspend.setSuspendedUser(this);
    }

    /**
     * LoginStatus 설정 편의 메서드
     * 양방향 연관관계 설정
     */
    public void setLoginStatus(LoginStatus loginStatus) {
        this.loginStatus = loginStatus;
        if (loginStatus != null) {
            loginStatus.setUser(this);
        }
    }

    /**
     * 회원 탈퇴 처리 편의 메서드
     */
    public void markAsDeleted(String withdrawReason) {
        this.status = Status.DELETED;
        this.deletedAt = LocalDateTime.now();

        Withdraw withdraw = Withdraw.builder()
                .withdrawReason(withdrawReason)
                .withdrawAt(LocalDateTime.now())
                .build();

        this.setWithdraw(withdraw);
    }

    /**
     * 회원 탈퇴 철회 편의 메서드
     */
    public void cancelWithdrawal() {
        this.status = Status.ACTIVE;
        this.deletedAt = null;
        this.withdraw = null;
    }

}
