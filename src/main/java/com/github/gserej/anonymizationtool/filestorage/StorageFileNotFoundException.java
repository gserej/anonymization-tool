package com.github.gserej.anonymizationtool.filestorage;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "File was not found")
public class StorageFileNotFoundException extends Exception {

    StorageFileNotFoundException(String message) {
        super(message);
    }

    StorageFileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
