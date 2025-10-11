package com.ksb.micro.user_profile.service.impl;

import com.ksb.micro.user_profile.model.UserProfile;
import com.ksb.micro.user_profile.repository.UserProfileRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class UserProfileServiceImpl implements UserProfileService {

    @Autowired
    private UserProfileRepository userProfileRepository;

    //GET BY ID-user profile
    @Override
    public Optional<UserProfile> getUserProfile(Long bankId, Long userId) {
        return userProfileRepository.findByIdAndBankId(userId, bankId);
    }

    //POST-create new user
    @Override
    public UserProfile createUserProfile(UserProfile userProfile) {
        //generating random ID
        Long randomId;
        do{
            randomId = ThreadLocalRandom.current().nextLong(100,10000L);
        } while(userProfileRepository.existsById(randomId));
        userProfile.setId(randomId);

        return userProfileRepository.save(userProfile);
    }

    //PUT-update existing user, if it is there
    @Override
    public UserProfile updateUserProfile(Long bankId, Long userId, UserProfile userProfile) {
        Optional<UserProfile> existingProfile = userProfileRepository.findByIdAndBankId(userId, bankId);
        if (existingProfile.isPresent()) {
            UserProfile profileToUpdate = existingProfile.get();
            profileToUpdate.setFirstName(userProfile.getFirstName());
            profileToUpdate.setLastName(userProfile.getLastName());
            profileToUpdate.setEmail(userProfile.getEmail());
            return userProfileRepository.save(profileToUpdate);
        }
        return null;
    }

    @Override
    @Transactional
    public void deleteUserProfile(Long bankId, Long userId) {
        userProfileRepository.findByIdAndBankId(userId, bankId).ifPresent(userProfileRepository::delete);
    }

    //GET all USERS by bankID
    @Override
    public List<UserProfile> getAllUserProfiles(Long bankId) {
        return userProfileRepository.findAllByBankId(bankId);
    }
}
