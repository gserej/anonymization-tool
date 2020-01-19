package com.github.gserej.anonymizationtool.rectangles;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Cannot extract words from pdf file")
public class WordsExtractionException extends Exception {
}
