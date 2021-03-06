package com.github.gserej.anonymizationtool.rectangles;

import com.github.gserej.anonymizationtool.document.Document;
import com.github.gserej.anonymizationtool.document.DocumentNotFoundException;
import com.github.gserej.anonymizationtool.document.DocumentRepository;
import com.github.gserej.anonymizationtool.filestorage.StorageCannotSaveFileException;
import com.github.gserej.anonymizationtool.imageprocessing.ImageToPdfConversionException;
import com.github.gserej.anonymizationtool.messages.CurrentMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

@Slf4j
@RestController
public class RectangleController {

    private final MarkedRectanglesProcessingService markedRectanglesProcessingService;
    private final DocumentRepository documentRepository;

    public RectangleController(MarkedRectanglesProcessingService markedRectanglesProcessingService,
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
