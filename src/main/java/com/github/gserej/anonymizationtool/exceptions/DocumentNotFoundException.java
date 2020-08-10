package com.github.gserej.anonymizationtool.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Document was not found")
public class DocumentNotFoundException extends RuntimeException {
}
