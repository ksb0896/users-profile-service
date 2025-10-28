package com.ksb.micro.user_profile.service.impl;

import com.ksb.micro.user_profile.model.UserProfile;

import java.util.List;
import java.util.Optional;

public interface UserProfileService {
    Optional<UserProfile> getUserProfile(Long bankId, Long userId); //GET
    UserProfile createUserProfile(UserProfile userProfile); //POST
    Optional<UserProfile> updateUserProfile(Long bankId, Long userId, UserProfile userProfile); //PUT
    void deleteUserProfile(Long bankId, Long userId); //DEL
    List<UserProfile> getAllUserProfiles(Long bankId); //GET all users(for specific bankID)
}
