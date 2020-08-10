package com.github.gserej.anonymizationtool.controllers;

import com.github.gserej.anonymizationtool.exceptions.DocumentNotFoundException;
import com.github.gserej.anonymizationtool.exceptions.ImageToPdfConversionException;
import com.github.gserej.anonymizationtool.exceptions.StorageCannotSaveFileException;
import com.github.gserej.anonymizationtool.exceptions.WordsDrawingException;
import com.github.gserej.anonymizationtool.models.CurrentMessage;
import com.github.gserej.anonymizationtool.models.Document;
import com.github.gserej.anonymizationtool.models.RectangleBox;
import com.github.gserej.anonymizationtool.repositories.DocumentRepository;
import com.github.gserej.anonymizationtool.service.rectangles.MarkedRectanglesProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

@Slf4j
@RestController
public class RectangleBoxController {

    private final MarkedRectanglesProcessingService markedRectanglesProcessingService;
    private final DocumentRepository documentRepository;

    public RectangleBoxController(MarkedRectanglesProcessingService markedRectanglesProcessingService,
                                  DocumentRepository documentRepository) {
        this.markedRectanglesProcessingService = markedRectanglesProcessingService;
        this.documentRepository = documentRepository;
    }

    @PostMapping(value = "/api/rectangles/{uuid}")
    public String postRectangles(@RequestBody Set<RectangleBox> rectangleBoxesMarked, @PathVariable("uuid") UUID uuid)
            throws DocumentNotFoundException, StorageCannotSaveFileException, WordsDrawingException, ImageToPdfConversionException {
        markedRectanglesProcessingService.processReceivedRectangleSet(rectangleBoxesMarked, uuid);
        Document document = documentRepository.findById(uuid).orElseThrow(DocumentNotFoundException::new);
        document.setCurrentMessage(CurrentMessage.DOWNLOAD_LINK_READY.getValue());
        documentRepository.save(document);
        return "link created";
    }

    @GetMapping("/api/rectangles/{uuid}")
    public Set<RectangleBox> getRectangles(@PathVariable("uuid") UUID uuid) throws DocumentNotFoundException {
        Document document = documentRepository.findById(uuid).orElseThrow(DocumentNotFoundException::new);
        return document.getParsedRectangles();
    }
}
