package com.teambiund.bander.auth_server.entity;


import com.teambiund.bander.auth_server.enums.ConsentType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Table(name = "consent")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Consent {
    @Id
    private String id;


    @ManyToOne
    @JoinColumn(name = "user_id")
    private Auth user;

    @Column(name = "agreement_at")
    private LocalDateTime agreementAt;

    @Column(name = "version")
    private String version;

    @Column(name = "consent_type")
    @Enumerated(EnumType.STRING)
    private ConsentType consentType;

    @Column(name = "consent_url")
    private String consentUrl; // url 관련 테이블도 따로 만들어야 하나.,.?

}
