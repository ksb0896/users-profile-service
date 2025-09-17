package com.ksb.micro.user_profile.service.impl;

import com.ksb.micro.user_profile.model.ProfilePhoto;
import com.ksb.micro.user_profile.repository.ProfilePhotoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

public class ProfilePhotoServiceImpl implements ProfilePhotoService{

    @Autowired
    private ProfilePhotoRepository profilePhotoRepository;

    @Override
    public Optional<ProfilePhoto> getProfilePhotoByUserId(Long userId) {
        return profilePhotoRepository.findByUserId(userId);
    }

    @Override
    public ProfilePhoto saveProfilePhoto(Long userId, byte[] photoData, String contentType) {
        ProfilePhoto photo = new ProfilePhoto();
        photo.setUserId(userId);
        photo.setPhotoData(photoData);
        photo.setContentType(contentType);
        return profilePhotoRepository.save(photo); //saving the photo
    }

    @Override
    public ProfilePhoto updateProfilePhoto(Long userId, byte[] photoData, String contentType) {
        Optional<ProfilePhoto> existingPhoto = profilePhotoRepository.findByUserId(userId);
        if (existingPhoto.isPresent()) {
            ProfilePhoto photoToUpdate = existingPhoto.get();
            photoToUpdate.setPhotoData(photoData);
            photoToUpdate.setContentType(contentType);
            return profilePhotoRepository.save(photoToUpdate);
        }
        return null;
    }

    @Override
    @Transactional
    public void deleteProfilePhoto(Long userId) {
        profilePhotoRepository.deleteByUserId(userId);
    }
}
