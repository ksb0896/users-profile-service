package com.ksb.micro.user_profile.exception;

public class PhotoServiceException extends RuntimeException{
    public PhotoServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
