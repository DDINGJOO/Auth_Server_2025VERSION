package com.teambiund.bander.auth_server.entity.consents_name;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "consents_name")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsentsTable {

    @Id
    @Column(name = "id")
    private String id;


    @Column(name = "consent_name")
    private String consentName;

    @Column(name = "version")
    private String version;

    @Column(name = "consent_url")
    private String consentUrl;

}
