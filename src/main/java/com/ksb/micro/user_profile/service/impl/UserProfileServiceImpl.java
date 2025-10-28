package com.ksb.micro.user_profile.service.impl;

import com.ksb.micro.user_profile.model.UserProfile;
import com.ksb.micro.user_profile.repository.UserProfileRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class UserProfileServiceImpl implements UserProfileService {

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private WebClient photoServiceWebClient;

    //GET BY ID-user profile
    @Override
    public Optional<UserProfile> getUserProfile(Long bankId, Long userId) {
        Optional<UserProfile> userOpt = userProfileRepository.findByIdAndBankId(userId, bankId);

        //If the user exists, call the photo service to enrich the data
        if(userOpt.isPresent()){
            UserProfile user = userOpt.get();

            try{
                String photoStatus = checkProfilePhotoStatus(bankId, userId).block(Duration.ofSeconds(1));
                user.setHasProfilePhoto(photoStatus);
            } catch (Exception e) {
                System.err.println("Error calling photo service: " + e.getMessage());
                user.setHasProfilePhoto("No");
            }
            return Optional.of(user);
        }
        return Optional.empty();
    }

    /**
     * Calls the profile-photo-service (Port 8082) using WebClient.
     * Returns a Mono<String> that resolves to "Yes" or "No".
     */
    private Mono<String> checkProfilePhotoStatus(Long bankId, Long userId){
        String photoUri = String.format("/v1/banks/%d/users/%d/photo", bankId, userId);

        return photoServiceWebClient.get()
                .uri(photoUri)
                .retrieve()
                .toBodilessEntity()
                .map(response -> {
                    if(response.getStatusCode().is2xxSuccessful()){
                        return "Yes";
                    }
                    return "No";
                }).onErrorResume(e ->{
                    return Mono.just("No");
                });
    }

    //POST-create new user
    @Override
    public UserProfile createUserProfile(UserProfile userProfile) {
        //generating random ID
        Long randomId;
        do{
            randomId = ThreadLocalRandom.current().nextLong(10000, Long.MAX_VALUE);
        } while(userProfileRepository.existsById(randomId));
        userProfile.setId(randomId);
        userProfile.setHasProfilePhoto("No");
        return userProfileRepository.save(userProfile);
    }

    //PUT-update existing user, if it is there
    @Override
    public Optional<UserProfile> updateUserProfile(Long bankId, Long userId, UserProfile userProfile) {
        Optional<UserProfile> existingProfile = userProfileRepository.findByIdAndBankId(userId, bankId);
        if (existingProfile.isPresent()) {
            UserProfile profileToUpdate = existingProfile.get();
            profileToUpdate.setFirstName(userProfile.getFirstName());
            profileToUpdate.setLastName(userProfile.getLastName());
            profileToUpdate.setEmail(userProfile.getEmail());
            return Optional.of(userProfileRepository.save(profileToUpdate));
        }
        return Optional.empty();
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
