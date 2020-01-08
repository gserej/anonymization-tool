package com.github.gserej.anonymizationtool.rectangles;

import com.github.gserej.anonymizationtool.filestorage.Document;
import com.github.gserej.anonymizationtool.filestorage.DocumentRepository;
import com.github.gserej.anonymizationtool.filestorage.StorageService;
import com.github.gserej.anonymizationtool.imageprocessing.ImageToPdfConversionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class MarkedRectanglesProcessingServiceImpl implements MarkedRectanglesProcessingService {

    private final StorageService storageService;
    private final DocumentRepository documentRepository;
    private ImageToPdfConversionService imageToPdfConversionService;
    private WordsDrawingService wordsDrawingService;

    public MarkedRectanglesProcessingServiceImpl(ImageToPdfConversionService imageToPdfConversionService,
                                                 WordsDrawingService wordsDrawingService, StorageService storageService, DocumentRepository documentRepository) {
        this.imageToPdfConversionService = imageToPdfConversionService;
        this.wordsDrawingService = wordsDrawingService;
        this.storageService = storageService;
        this.documentRepository = documentRepository;
    }

    @Override
    public void processReceivedRectangleSet(Set<RectangleBox> rectangleBoxesMarked, UUID uuid) {

        log.info("Marked rectangles received from the page: " + rectangleBoxesMarked.toString());
        Document document = documentRepository.findById(uuid).orElseThrow();
        document.setMarkedRectangles(rectangleBoxesMarked);
        documentRepository.save(document);
        File pdfFileToProcess = storageService.load(document.getDocumentName(), uuid).toFile();
        log.info("Loaded PDF file: " + pdfFileToProcess.getName());

        try {
            wordsDrawingService.drawBoxesAroundMarkedWords(pdfFileToProcess, uuid);
        } catch (IOException e) {
            log.error("Exception during drawing boxes around words" + e);
            return;
        }
        document = documentRepository.findById(uuid).orElseThrow();
        try {
            log.info("List of images: " + document.getImageList().toString());
            String pathToProcessedPdf = imageToPdfConversionService.createPdfFromMultipleImages(
                    document.getDocumentName(), pdfFileToProcess, uuid);

            storageService.storeAsFile(new File(pathToProcessedPdf), uuid);
            log.info("Document is ready to download.");
        } catch (IOException e) {
            log.error("Exception during creating PDF file from images.");
        }
    }
}
