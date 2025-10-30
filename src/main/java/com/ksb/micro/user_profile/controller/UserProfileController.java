package com.ksb.micro.user_profile.controller;

import com.ksb.micro.user_profile.model.UserProfile;
import com.ksb.micro.user_profile.service.impl.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/v1/banks/{bankId}/users")
public class UserProfileController {

    @Autowired
    private UserProfileService userProfileService;

    @GetMapping("/{userId}") //by user ID
    public ResponseEntity<UserProfile> getUserProfile(@PathVariable Long bankId, @PathVariable Long userId){
        UserProfile profile = userProfileService.getUserProfile(bankId, userId);
        return ResponseEntity.ok(profile);
    }

    @GetMapping
    public ResponseEntity<List<UserProfile>> getAllUserProfiles(@PathVariable Long bankId){
        List<UserProfile> profiles = userProfileService.getAllUserProfiles(bankId);
        if (profiles.isEmpty()){
            return ResponseEntity.noContent().build(); //return 204, if list is empty
        }
        return ResponseEntity.ok(profiles);//return the list of users for bankID specific
    }

    @PostMapping
    public ResponseEntity<UserProfile> createUserProfile(@PathVariable Long bankId, @RequestBody UserProfile userProfile){
        userProfile.setBankId(bankId);
        UserProfile createUser = userProfileService.createUserProfile(userProfile);
        return ResponseEntity.ok(createUser);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<? extends Object> updateUserProfile(@PathVariable Long bankId, @PathVariable Long userId, @RequestBody UserProfile userProfile){
        UserProfile updateUser = userProfileService.updateUserProfile(bankId,userId,userProfile);
        return ResponseEntity.ok(updateUser);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<UserProfile> deleteUserProfile(@PathVariable Long bankId, @PathVariable Long userId){
        userProfileService.deleteUserProfile(bankId,userId);
        return ResponseEntity.noContent().build();
    }
}
