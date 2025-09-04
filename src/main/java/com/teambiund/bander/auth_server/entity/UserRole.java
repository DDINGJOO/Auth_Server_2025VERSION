package com.teambiund.bander.auth_server.entity;


import com.teambiund.bander.auth_server.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Table( name = "user_role")
@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class UserRole {

    @Id
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "userRole")
    private List<Auth> users = new ArrayList<>();

}
