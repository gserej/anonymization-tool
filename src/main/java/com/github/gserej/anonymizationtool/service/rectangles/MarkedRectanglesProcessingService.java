package com.github.gserej.anonymizationtool.service.rectangles;

import com.github.gserej.anonymizationtool.exceptions.DocumentNotFoundException;
import com.github.gserej.anonymizationtool.exceptions.ImageToPdfConversionException;
import com.github.gserej.anonymizationtool.exceptions.StorageCannotSaveFileException;
import com.github.gserej.anonymizationtool.exceptions.WordsDrawingException;
import com.github.gserej.anonymizationtool.models.Document;
import com.github.gserej.anonymizationtool.models.RectangleBox;
import com.github.gserej.anonymizationtool.repositories.DocumentRepository;
import com.github.gserej.anonymizationtool.service.filestorage.StorageService;
import com.github.gserej.anonymizationtool.service.imageprocessing.ImageToPdfConversionService;
import com.github.gserej.anonymizationtool.service.rectangles.drawing.WordsDrawingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class MarkedRectanglesProcessingService {

    private final StorageService storageService;
    private final DocumentRepository documentRepository;
    private final ImageToPdfConversionService imageToPdfConversionService;
    private final WordsDrawingService wordsDrawingService;

    public MarkedRectanglesProcessingService(ImageToPdfConversionService imageToPdfConversionService,
                                             WordsDrawingService wordsDrawingService, StorageService storageService, DocumentRepository documentRepository) {
        this.imageToPdfConversionService = imageToPdfConversionService;
        this.wordsDrawingService = wordsDrawingService;
        this.storageService = storageService;
        this.documentRepository = documentRepository;
    }

    public void processReceivedRectangleSet(Set<RectangleBox> rectangleBoxesMarked, UUID uuid)
            throws DocumentNotFoundException, StorageCannotSaveFileException, WordsDrawingException, ImageToPdfConversionException {

        log.debug("Marked rectangles received from the page: " + rectangleBoxesMarked.toString());
        Document document = documentRepository.findById(uuid).orElseThrow(DocumentNotFoundException::new);
        document.setMarkedRectangles(rectangleBoxesMarked);
        documentRepository.save(document);
        File pdfFileToProcess = storageService.load(document.getDocumentName(), uuid).toFile();
        log.debug("Loaded PDF file: " + pdfFileToProcess.getName());
        wordsDrawingService.drawBoxesAroundMarkedWords(pdfFileToProcess, uuid);
        document = documentRepository.findById(uuid).orElseThrow(DocumentNotFoundException::new);

        log.debug("List of images: " + document.getImageList().toString());
        String pathToProcessedPdf = imageToPdfConversionService.createPdfFromMultipleImages(
                document.getDocumentName(), pdfFileToProcess, uuid);

        storageService.storeAsFile(new File(pathToProcessedPdf), uuid);
        log.debug("Document is ready to download.");
    }
}
