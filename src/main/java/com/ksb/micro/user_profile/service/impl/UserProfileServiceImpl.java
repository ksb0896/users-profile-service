package com.ksb.micro.user_profile.service.impl;

import com.ksb.micro.user_profile.exception.ResourceNotFoundException;
import com.ksb.micro.user_profile.model.UserProfile;
import com.ksb.micro.user_profile.repository.UserProfileRepository;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.timelimiter.TimeLimiterOperator;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final WebClient photoServiceWebClient;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final TimeLimiterRegistry timeLimiterRegistry;

    private CircuitBreaker getCircuitBreaker() {
        return circuitBreakerRegistry.circuitBreaker("photoServiceCircuitBreaker");
    }

    private TimeLimiter getTimeLimiter() {
        return timeLimiterRegistry.timeLimiter("photoServiceCircuitBreaker");
    }

    @Override
    @Cacheable(value = "userProfiles", key = "#bankId + ':' + #userId")
    public UserProfile getUserProfile(Long bankId, Long userId) {
        log.info("Cache Miss for user: {}", userId);

        UserProfile user = userProfileRepository.findByIdAndBankId(userId, bankId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        String photoStatus = checkProfilePhotoStatus(bankId, userId)
                .block(Duration.ofMillis(1500));

        user.setHasProfilePhoto(photoStatus);
        return user;
    }

    private Mono<String> checkProfilePhotoStatus(Long bankId, Long userId) {
        return photoServiceWebClient.get()
                .uri("/v1/banks/{bankId}/users/{userId}/photo", bankId, userId)
                .retrieve()
                .toBodilessEntity()
                .map(response -> "Yes")
                // Apply TimeLimiter first, then CircuitBreaker
                .transformDeferred(TimeLimiterOperator.of(getTimeLimiter()))
                .transformDeferred(CircuitBreakerOperator.of(getCircuitBreaker()))
                .onErrorResume(e -> {
                    log.warn("Fallback triggered for user {}: {}", userId, e.getMessage());
                    return Mono.just("No");
                });
    }

    @Override
    @Transactional
    public UserProfile createUserProfile(UserProfile userProfile) {
        userProfile.setId(ThreadLocalRandom.current().nextLong(10000, Long.MAX_VALUE));
        userProfile.setHasProfilePhoto("No");
        return userProfileRepository.save(userProfile);
    }

    @Override
    @Transactional
    @CacheEvict(value = "userProfiles", key = "#bankId + ':' + #userId")
    public UserProfile updateUserProfile(Long bankId, Long userId, UserProfile userProfile) {
        return userProfileRepository.findByIdAndBankId(userId, bankId)
                .map(existing -> {
                    existing.setFirstName(userProfile.getFirstName());
                    existing.setLastName(userProfile.getLastName());
                    existing.setEmail(userProfile.getEmail());
                    return userProfileRepository.save(existing);
                })
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    @Transactional
    @CacheEvict(value = "userProfiles", key = "#bankId + ':' + #userId")
    public void deleteUserProfile(Long bankId, Long userId) {
        userProfileRepository.findByIdAndBankId(userId, bankId)
                .ifPresent(userProfileRepository::delete);
    }

    @Override
    public List<UserProfile> getAllUserProfiles(Long bankId) {
        List<UserProfile> users = userProfileRepository.findAllByBankId(bankId);

        return Flux.fromIterable(users)
                .flatMap(user -> checkProfilePhotoStatus(bankId, user.getId())
                        .map(status -> {
                            user.setHasProfilePhoto(status);
                            return user;
                        }))
                .collectList()
                .block(Duration.ofSeconds(10));
    }
}
