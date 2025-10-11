package com.ksb.micro.user_profile.controller;


import com.ksb.micro.user_profile.model.UserProfile;
import com.ksb.micro.user_profile.service.impl.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/banks/{bankId}/users")
public class UserProfileController {

    @Autowired
    private UserProfileService userProfileService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserProfile> getUserProfile(@PathVariable Long bankId, @PathVariable Long userId){
        return userProfileService.getUserProfile(bankId, userId).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<UserProfile> createUserProfile(@PathVariable Long bankId, @RequestBody UserProfile userProfile){
        userProfile.setBankId(bankId);
        UserProfile createUser = userProfileService.createUserProfile(userProfile);
        return ResponseEntity.ok(createUser);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserProfile> updateUserProfile(@PathVariable Long bankId, @PathVariable Long userId, @RequestBody UserProfile userProfile){
        UserProfile updateUser = userProfileService.updateUserProfile(bankId,userId,userProfile);
        return updateUser !=null ? ResponseEntity.ok(updateUser) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<UserProfile> deleteUserProfile(@PathVariable Long bankId, @PathVariable Long userId){
        userProfileService.deleteUserProfile(bankId,userId);
        return ResponseEntity.noContent().build();
    }
}
