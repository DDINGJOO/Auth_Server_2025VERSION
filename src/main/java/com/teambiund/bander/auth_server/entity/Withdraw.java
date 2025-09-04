package com.teambiund.bander.auth_server.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Table( name = "withdraw")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Withdraw{


    @Id
    @Column(name= "user_id")
    private String id;


    @Column(name= "withdraw_reason")
    private String withdrawReason;

    @Column(name= "withdraw_at")
    private LocalDateTime withdrawAt;



}
