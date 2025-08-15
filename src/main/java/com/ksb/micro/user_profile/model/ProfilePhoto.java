//JPA Entity for the `profile_photos` table.
package com.ksb.micro.user_profile.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "profile_photos")
public class ProfilePhoto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;

    @Lob
    private byte[] photoData;
    private String contentType;
}
