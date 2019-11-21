package com.github.gserej.anonymizationtool.rectangles;

import com.github.gserej.anonymizationtool.messages.MessageService;
import com.github.gserej.anonymizationtool.rectangles.model.RectangleBox;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@Slf4j
@RestController
public class RectanglesHandlingController {

    private MarkedRectanglesProcessingService markedRectanglesProcessingService;
    private MessageService messageService;
    private RectangleBoxSets rectangleBoxSets;

    @Autowired
    public RectanglesHandlingController(MarkedRectanglesProcessingService markedRectanglesProcessingService, MessageService messageService, RectangleBoxSets rectangleBoxSets) {
        this.markedRectanglesProcessingService = markedRectanglesProcessingService;
        this.messageService = messageService;
        this.rectangleBoxSets = rectangleBoxSets;
    }

    @PostMapping(value = "/api/rectangles")
    public String postRectangles(@RequestBody Set<RectangleBox> rectangleBoxesMarked) {
        markedRectanglesProcessingService.processReceivedRectangleSet(rectangleBoxesMarked);
        messageService.setCurrentMessage(messageService.getDOWNLOAD_LINK_READY());
        return "link created";
    }

    @GetMapping("/api/rectangles")
    public Set<RectangleBox> sendRectangles() {
        return rectangleBoxSets.getRectangleBoxSetParsed();
    }
}
