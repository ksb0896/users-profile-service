//JPA Entity for the `user_profiles` table.
package com.ksb.micro.user_profile.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "user_profiles")
public class UserProfile {
    @Id
    private Long id;

    @Column(nullable = false)
    private Long bankId;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(unique = true)
    private String email;

    @Transient
    private String hasProfilePhoto;
}
