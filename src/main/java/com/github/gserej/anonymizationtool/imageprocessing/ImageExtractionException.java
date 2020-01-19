package com.github.gserej.anonymizationtool.imageprocessing;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Cannot convert image file to pdf file")
public class ImageExtractionException extends Exception {
}
