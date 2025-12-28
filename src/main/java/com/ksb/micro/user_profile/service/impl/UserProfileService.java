package com.ksb.micro.user_profile.service.impl;

import com.ksb.micro.user_profile.model.UserProfile;

import java.util.List;

public interface UserProfileService {
    UserProfile getUserProfile(Long bankId, Long userId); //GET
    UserProfile createUserProfile(UserProfile userProfile); //POST
    UserProfile updateUserProfile(Long bankId, Long userId, UserProfile userProfile); //PUT
    void deleteUserProfile(Long bankId, Long userId); //DEL
    List<UserProfile> getAllUserProfiles(Long bankId); //GET all users(for specific bankID)
}
