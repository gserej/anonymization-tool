package com.github.gserej.anonymizationtool.messages;

import com.github.gserej.anonymizationtool.document.Document;
import com.github.gserej.anonymizationtool.document.DocumentRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class MessageController {

    private final DocumentRepository documentRepository;

    public MessageController(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @GetMapping("/api/message/{uuid}")
    public String getMessage(@PathVariable("uuid") UUID uuid) {
        if (documentRepository.findById(uuid).isPresent()) {
            Document document = documentRepository.findById(uuid).get();
            return document.getCurrentMessage();
        }
        return "";
    }
}
