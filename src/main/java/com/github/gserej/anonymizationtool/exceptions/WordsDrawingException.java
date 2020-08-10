package com.github.gserej.anonymizationtool.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Cannot draw rectangles around words")
public class WordsDrawingException extends RuntimeException {
}
