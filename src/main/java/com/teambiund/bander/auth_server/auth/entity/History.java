package com.teambiund.bander.auth_server.auth.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Table(name = "history")
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class History {
  @Id
  @Column(name = "id")
  private String id;

  @ManyToOne
  @JoinColumn(name = "user_id")
  private Auth user;

  @Version
  @Column(name = "version")
  private int version;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "updated_column")
  private String updatedColumn;

  @Column(name = "before_column_value")
  private String beforeColumnValue;

  @Column(name = "after_column_value")
  private String afterColumnValue;
}
