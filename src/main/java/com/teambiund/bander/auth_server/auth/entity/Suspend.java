package com.teambiund.bander.auth_server.auth.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.*;

@Table(name = "suspend")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Suspend {
  @Id
  @Column(name = "id")
  private String id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private Auth suspendedUser;

  @Column(name = "reason")
  private String reason;

  @Column(name = "suspend_at")
  private LocalDateTime suspendAt;

  @Column(name = "suspend_until")
  private LocalDate suspendUntil;

  @Column(name = "suspender")
  private String suspenderUserId;

  @Version
  @Column(name = "version")
  private int version;
}
