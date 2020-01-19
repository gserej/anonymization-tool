package com.github.gserej.anonymizationtool.rectangles;

import com.github.gserej.anonymizationtool.document.Document;
import com.github.gserej.anonymizationtool.document.DocumentNotFoundException;
import com.github.gserej.anonymizationtool.document.DocumentRepository;
import com.github.gserej.anonymizationtool.filestorage.StorageCannotSaveFileException;
import com.github.gserej.anonymizationtool.filestorage.StorageService;
import com.github.gserej.anonymizationtool.imageprocessing.ImageToPdfConversionException;
import com.github.gserej.anonymizationtool.imageprocessing.ImageToPdfConversionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class MarkedRectanglesProcessingServiceImpl implements MarkedRectanglesProcessingService {

    private final StorageService storageService;
    private final DocumentRepository documentRepository;
    private final ImageToPdfConversionService imageToPdfConversionService;
    private final WordsDrawingService wordsDrawingService;

    public MarkedRectanglesProcessingServiceImpl(ImageToPdfConversionService imageToPdfConversionService,
                                                 WordsDrawingService wordsDrawingService, StorageService storageService, DocumentRepository documentRepository) {
        this.imageToPdfConversionService = imageToPdfConversionService;
        this.wordsDrawingService = wordsDrawingService;
        this.storageService = storageService;
        this.documentRepository = documentRepository;
    }

    @Override
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
