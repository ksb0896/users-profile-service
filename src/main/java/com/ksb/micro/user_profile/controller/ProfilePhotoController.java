package com.ksb.micro.user_profile.controller;

import com.ksb.micro.user_profile.model.ProfilePhoto;
import com.ksb.micro.user_profile.service.impl.ProfilePhotoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("/v1/banks/{bankId}/users/{userId}/photo")
public class ProfilePhotoController {

    @Autowired
    private ProfilePhotoService profilePhotoService;

    @GetMapping
    public ResponseEntity<byte[]> getProfilePhotoByUserId(@PathVariable Long userId){
        Optional<ProfilePhoto> photo = profilePhotoService.getProfilePhotoByUserId(userId);
        if(photo.isPresent()){
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(photo.get().getContentType()));
            headers.setContentLength(photo.get().getPhotoData().length);
            return new ResponseEntity<>(photo.get().getPhotoData(),headers, HttpStatus.OK);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadProfilePhoto(@PathVariable Long userId, @RequestParam("file") MultipartFile file) throws IOException{
        Optional<ProfilePhoto> existingPhoto = profilePhotoService.getProfilePhotoByUserId(userId);
        if(existingPhoto.isPresent()){
            return new ResponseEntity<>("Photo already exists!! Try to update it", HttpStatus.CONFLICT);
        }
        profilePhotoService.saveProfilePhoto(userId, file.getBytes(), file.getContentType());
        return ResponseEntity.status(HttpStatus.CREATED).body("Photo uploaded successfully.");
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> updateProfilePhoto(@PathVariable Long userId, @RequestParam("file") MultipartFile file) throws IOException{
        ProfilePhoto updatePhoto = profilePhotoService.updateProfilePhoto(userId,file.getBytes(),file.getContentType());
        if(updatePhoto !=null){
            return ResponseEntity.ok("Photo updated successfully!!");
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteProfilePhoto(@PathVariable Long userId){
        Optional<ProfilePhoto> photo = profilePhotoService.getProfilePhotoByUserId(userId);
        if(photo.isEmpty()){
            return ResponseEntity.notFound().build(); //for 404
        }
        profilePhotoService.deleteProfilePhoto(userId);
        return ResponseEntity.noContent().build(); //for 204 status code
    }
}
