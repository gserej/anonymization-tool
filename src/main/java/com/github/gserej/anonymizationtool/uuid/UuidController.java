package com.github.gserej.anonymizationtool.uuid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class UuidController {

    @GetMapping("/api/uuid")
    public UUID generateRandomUuid() {
        return UUID.randomUUID();
    }
}
