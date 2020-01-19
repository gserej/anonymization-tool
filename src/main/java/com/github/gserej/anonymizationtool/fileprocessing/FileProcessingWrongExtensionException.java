package com.github.gserej.anonymizationtool.fileprocessing;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNSUPPORTED_MEDIA_TYPE, reason = "Uploaded file have unsupported file extension")
public class FileProcessingWrongExtensionException extends Exception {
}
