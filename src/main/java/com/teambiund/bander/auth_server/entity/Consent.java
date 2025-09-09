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
    @Version
    private int version;

    @Column(name = "is_requirement")
    private boolean isRequirement;


    @Column(name = "consent_type")
    @Enumerated(EnumType.STRING)
    private ConsentType consentType;

    @Column(name = "consent_url")
    private String consentUrl;

}
