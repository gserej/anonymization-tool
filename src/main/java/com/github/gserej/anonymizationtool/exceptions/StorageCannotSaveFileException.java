package com.github.gserej.anonymizationtool.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Cannot save a file")
public class StorageCannotSaveFileException extends RuntimeException {

    public StorageCannotSaveFileException(String message) {
        super(message);
    }

    public StorageCannotSaveFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
