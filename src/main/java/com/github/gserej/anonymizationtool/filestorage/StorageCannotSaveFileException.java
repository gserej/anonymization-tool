package com.github.gserej.anonymizationtool.filestorage;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Cannot save a file")
public class StorageCannotSaveFileException extends RuntimeException {

    StorageCannotSaveFileException(String message) {
        super(message);
    }

    StorageCannotSaveFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
