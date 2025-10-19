package com.teambiund.bander.auth_server.entity;

import com.teambiund.bander.auth_server.entity.consentsname.ConsentsTable;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Table(name = "consent")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Consent {
  @Id private String id;

  @ManyToOne
  @JoinColumn(name = "user_id")
  private Auth user;
  
  @ManyToOne
  @JoinColumn(name = "consent_id")
  private ConsentsTable consentsTable;
  @Column(name = "consented_at")
  private LocalDateTime consentedAt;
  
	
}
