package com.github.gserej.anonymizationtool.rectangles;

import com.github.gserej.anonymizationtool.filestorage.DocumentMetaInfo;
import com.github.gserej.anonymizationtool.filestorage.StorageService;
import com.github.gserej.anonymizationtool.imageprocessing.ImageToPdfConversionService;
import com.github.gserej.anonymizationtool.rectangles.model.RectangleBox;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Set;

@Slf4j
@Service
public class MarkedRectanglesProcessingServiceImpl implements MarkedRectanglesProcessingService {


    private final StorageService storageService;
    private final DocumentMetaInfo documentMetaInfo;
    private ImageToPdfConversionService imageToPdfConversionService;
    private WordsPrintingService wordsPrintingService;
    private RectangleBoxSets rectangleBoxSets;


    public MarkedRectanglesProcessingServiceImpl(ImageToPdfConversionService imageToPdfConversionService, WordsPrintingService wordsPrintingService, StorageService storageService, DocumentMetaInfo documentMetaInfo, RectangleBoxSets rectangleBoxSets) {
        this.imageToPdfConversionService = imageToPdfConversionService;
        this.wordsPrintingService = wordsPrintingService;
        this.storageService = storageService;
        this.documentMetaInfo = documentMetaInfo;
        this.rectangleBoxSets = rectangleBoxSets;
    }


    @Override
    public void processReceivedRectangleSet(Set<RectangleBox> rectangleBoxesMarked) {

        log.info("Marked rectangles received from the page: " + rectangleBoxesMarked.toString());
        rectangleBoxSets.setRectangleBoxSetMarked(rectangleBoxesMarked);

        File pdfFileToProcess = storageService.loadAsFile(documentMetaInfo.getDocumentName());
        log.info("Loaded PDF file: " + pdfFileToProcess.getName());

        try {
            wordsPrintingService.drawBoxesAroundMarkedWords(pdfFileToProcess);
        } catch (IOException e) {
            log.error("Error drawing boxes around words" + e);
        }
        try {
            log.info("List of images: " + documentMetaInfo.getImageList().toString());
            String pathToProcessedPdf = imageToPdfConversionService.createPdfFromMultipleImages(
                    documentMetaInfo.getDocumentName(), pdfFileToProcess);

            storageService.storeAsFile(new File(pathToProcessedPdf));
            log.info("Document is ready to download.");
        } catch (IOException e) {
            log.error("Error creating PDF file from images.");
        } finally {
            documentMetaInfo.setDocumentName(null);
        }
    }
}
