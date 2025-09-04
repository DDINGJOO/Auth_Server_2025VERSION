package com.teambiund.bander.auth_server.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Table( name = "history")
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class History
{
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

    @Column(name = "updatedColumn")
    private String updatedColumn;

    @Column(name = "beforeColumnValue")
    private String beforeColumnValue;

    @Column(name = "afterColumnValue")
    private String afterColumnValue;
}
