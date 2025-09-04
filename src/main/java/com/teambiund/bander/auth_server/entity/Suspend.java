package com.teambiund.bander.auth_server.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Table(name ="suspend")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Suspend {
    @Id
    @Column(name = "user_id")
    private String suspendedUserId;

    @Column(name = "reason")
    private String reason;

    @Column(name = "suspend_at")
    private LocalDateTime suspendAt;

    @Column(name = "suspend_until")
    private LocalDate suspendUntil;

    @Column(name="suspender")
    private String suspenderUserId;
}
