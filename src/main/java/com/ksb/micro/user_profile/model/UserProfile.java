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

    private Long bankId;
    private String firstName;
    private String lastName;
    private String email;
}
