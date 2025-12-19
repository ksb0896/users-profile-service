package com.ksb.micro.user_profile.controller;

import com.ksb.micro.user_profile.model.UserProfile;
import com.ksb.micro.user_profile.service.impl.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/banks/{bankId}/users")
@Tag(name = "Users", description = "User Management endpoints")
public class UserProfileController {

    @Autowired
    private UserProfileService userProfileService;

    @GetMapping("/{userId}") //by user ID
    @Operation(summary = "Get user by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserProfile> getUserProfile(@PathVariable Long bankId, @PathVariable Long userId){
        UserProfile profile = userProfileService.getUserProfile(bankId, userId);
        return ResponseEntity.ok(profile);
    }

    @GetMapping
    @Operation(summary = "Get ALL users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<List<UserProfile>> getAllUserProfiles(@PathVariable Long bankId){
        List<UserProfile> profiles = userProfileService.getAllUserProfiles(bankId);
        if (profiles.isEmpty()){
            return ResponseEntity.noContent().build(); //return 204, if list is empty
        }
        return ResponseEntity.ok(profiles);//return the list of users for bankID specific
    }

    @PostMapping
    @Operation(summary = "User created")
    @ApiResponse(responseCode = "201", description = "User created with ID")
    public ResponseEntity<UserProfile> createUserProfile(@PathVariable Long bankId, @RequestBody UserProfile userProfile){
        userProfile.setBankId(bankId);
        UserProfile createdProfile = userProfileService.createUserProfile(userProfile);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProfile);
    }

    @PutMapping("/{userId}")
    @Operation(summary = "User updated")
    public ResponseEntity<? extends Object> updateUserProfile(@PathVariable Long bankId, @PathVariable Long userId, @RequestBody UserProfile userProfile){
        UserProfile updateUser = userProfileService.updateUserProfile(bankId,userId,userProfile);
        return ResponseEntity.ok(updateUser);
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "User deleted")
    public ResponseEntity<UserProfile> deleteUserProfile(@PathVariable Long bankId, @PathVariable Long userId){
        userProfileService.deleteUserProfile(bankId,userId);
        return ResponseEntity.noContent().build();
    }
}
