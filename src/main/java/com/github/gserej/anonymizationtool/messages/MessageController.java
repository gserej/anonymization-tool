package com.github.gserej.anonymizationtool.messages;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MessageController {

    private MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/api/message")
    public String getMessage() {
        return messageService.getCurrentMessage();
    }
}
