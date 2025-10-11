package com.ksb.micro.user_profile.repository;

import com.ksb.micro.user_profile.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile,Long> {
    Optional<UserProfile> findByIdAndBankId(Long id, Long bankId);

    //Find all users for a specific bankId
    List<UserProfile> findAllByBankId(Long bankId);
}
