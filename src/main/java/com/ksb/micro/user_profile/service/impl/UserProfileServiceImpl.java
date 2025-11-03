package com.ksb.micro.user_profile.service.impl;

import com.ksb.micro.user_profile.exception.PhotoServiceException;
import com.ksb.micro.user_profile.exception.ResourceNotFoundException;
import com.ksb.micro.user_profile.model.UserProfile;
import com.ksb.micro.user_profile.repository.UserProfileRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class UserProfileServiceImpl implements UserProfileService {

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private WebClient photoServiceWebClient;

    //GET BY ID-user profile
    @Override
    @Cacheable(value = "userProfiles", key = "#bankId + ':' + #userId")
    public UserProfile getUserProfile(Long bankId, Long userId) {
        /*
        Implemented Cache-Aside pattern - When a request for a user comes in,
        the user-profile-service first checks Redis using the user ID as the key.
        - Cache Hit
        - Cache Miss
        - Write Back: The service then stores the fresh data in Redis (with an expiration time, like 30 minutes) and returns the data to the client
        */

        //Cache miss
        System.out.println("--> Executing DB/Photo Service logic for user: " + userId + " (Cache Miss)");

        UserProfile user = userProfileRepository.findByIdAndBankId(userId, bankId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found for ID: " + userId + " in bank: " + bankId));

            try{
                String photoStatus = checkProfilePhotoStatus(bankId, userId).block(Duration.ofSeconds(1));
                user.setHasProfilePhoto(photoStatus);
            } catch (PhotoServiceException e) {
                System.err.println("CRITICAL: Photo Service failed for user " + userId + ". Error: " + e.getMessage());
                user.setHasProfilePhoto("No");
            } catch (Exception e){
                System.err.println("Error during photo service check: " + e.getMessage());
                user.setHasProfilePhoto("No");
            }
            return user;
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
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new PhotoServiceException(
                                "Photo Service returned 5xx error: " + response.statusCode(), null))
                )
                .toBodilessEntity()
                .map(response -> {
                    if(response.getStatusCode().is2xxSuccessful()){
                        return "Yes";
                    }
                    return "No";
                })
                .onErrorMap(e -> e instanceof IOException, e ->
                        new PhotoServiceException("Network/Connection failure to Photo Service.", e)
                )
                .onErrorMap(e -> e instanceof WebClientResponseException && !((WebClientResponseException)e).getStatusCode().is4xxClientError(), e ->
                        new PhotoServiceException("Unhandled Photo Service response error.", e)
                );
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
    @CacheEvict(value = "userProfiles", key = "#bankId + ':' + #userId")
    public UserProfile updateUserProfile(Long bankId, Long userId, UserProfile userProfile) {

        System.out.println("--> Database update, Evicting user: " + userId + " from cache.");

        UserProfile userToUpdate = userProfileRepository.findByIdAndBankId(userId, bankId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found for ID: " + userId + " in bank: " + bankId));
        // Updated fields
        userToUpdate.setFirstName(userProfile.getFirstName());
        userToUpdate.setLastName(userProfile.getLastName());
        userToUpdate.setEmail(userProfile.getEmail());
        userToUpdate.setHasProfilePhoto(userProfile.getHasProfilePhoto());

        return userProfileRepository.save(userToUpdate);
    }

    @Override
    @Transactional
    @CacheEvict(value = "userProfiles", key = "#bankId + ':' + #userId")
    public void deleteUserProfile(Long bankId, Long userId) {
        System.out.println("--> Database delete, Evicting user: " + userId + " from cache.");

        userProfileRepository.findByIdAndBankId(userId, bankId).ifPresent(userProfileRepository::delete);
    }

    //GET all USERS by bankID
    @Override
    public List<UserProfile> getAllUserProfiles(Long bankId) {

        List<UserProfile> users = userProfileRepository.findAllByBankId(bankId); //fetch all users from local db

        if(users.isEmpty()){
            return users;
        }

        List<UserProfile> enrichedUsers = Flux.fromIterable(users).flatMap(user -> checkProfilePhotoStatus(user.getBankId(), user.getId())
                .onErrorResume(PhotoServiceException.class, e -> {
                    System.err.println("CRITICAL: Photo Service failed for user " + user.getId() + ". Error: " + e.getMessage());
                    return Mono.just("No");
                }).onErrorResume(e ->{
                    System.err.println("Unexpected error checking photo status for user " + user.getId() + ": " + e.getMessage());
                    return Mono.just("No");
                }).map(status ->{
                    user.setHasProfilePhoto(status);
                    return user;
                })
        ).collectList().block(Duration.ofSeconds(10));
        return enrichedUsers;
    }
}
