package com.ksb.micro.user_profile.service.impl;

import com.ksb.micro.user_profile.exception.PhotoServiceException;
import com.ksb.micro.user_profile.exception.ResourceNotFoundException;
import com.ksb.micro.user_profile.model.UserProfile;
import com.ksb.micro.user_profile.repository.UserProfileRepository;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
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

    private final UserProfileRepository userProfileRepository;
    private final WebClient photoServiceWebClient;
    private final CircuitBreaker circuitBreaker;

    @Autowired
    public UserProfileServiceImpl(UserProfileRepository userProfileRepository, WebClient photoServiceWebClient, CircuitBreakerRegistry circuitBreakerRegistry) {
        this.userProfileRepository = userProfileRepository;
        this.photoServiceWebClient = photoServiceWebClient;
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("photoServiceCircuitBreaker");
    }

    //GET BY ID-user profile
    @Override
    @Cacheable(value = "userProfiles", key = "#bankId + ':' + #userId")
    public UserProfile getUserProfile(Long bankId, Long userId) {

        System.out.println("Executing DB/Photo Service logic for user: " + userId + " (Cache Miss)");

        UserProfile user = userProfileRepository.findByIdAndBankId(userId, bankId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found for ID: " + userId + " in bank: " + bankId));

        try{
            String photoStatus = checkProfilePhotoStatus(bankId, userId).block(Duration.ofMillis(1200));
            user.setHasProfilePhoto(photoStatus);
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("CircuitBreaker is OPEN")) {
                System.err.println("Circuit Breaker OPEN. Skipping photo service for user " + userId);
            } else {
                System.err.println("Error during photo service check: " + e.getMessage());
            }
            user.setHasProfilePhoto("No");
        }
        return user;
    }

    /**
     * Calls the profile-photo-service (Port 8082) using WebClient, wrapped in a Circuit Breaker.
     * Returns a Mono<String> that resolves to "Yes" or "No".
     */
    private Mono<String> checkProfilePhotoStatus(Long bankId, Long userId){
        String photoUri = String.format("/v1/banks/%d/users/%d/photo", bankId, userId);

        Mono<String> webClientCall = photoServiceWebClient.get()
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
                        new PhotoServiceException("Connection failure to Photo Service.", e)
                )
                .onErrorMap(e -> e instanceof WebClientResponseException && !((WebClientResponseException)e).getStatusCode().is4xxClientError(), e ->
                        new PhotoServiceException("Unhandled Photo Service response error.", e)
                );

        // --- Resilience4j Implementation ---
        return webClientCall
                .transform(CircuitBreakerOperator.of(circuitBreaker))
                .onErrorResume(e -> {
                    if (e instanceof io.github.resilience4j.circuitbreaker.CallNotPermittedException) {
                        System.err.println("CIRCUIT OPEN. Immediate Fallback for user: " + userId);
                    }
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
                .onErrorResume(e -> {
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
