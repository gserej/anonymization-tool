package com.github.gserej.anonymizationtool.fileprocessing;

import com.github.gserej.anonymizationtool.datacategory.RectangleParsingService;
import com.github.gserej.anonymizationtool.filestorage.DocumentMetaInfo;
import com.github.gserej.anonymizationtool.filestorage.StorageService;
import com.github.gserej.anonymizationtool.imageprocessing.ImageLocationsExtractionService;
import com.github.gserej.anonymizationtool.imageprocessing.ImageToPdfConversionService;
import com.github.gserej.anonymizationtool.imageprocessing.OCRService;
import com.github.gserej.anonymizationtool.rectangles.RectangleBoxSets;
import com.github.gserej.anonymizationtool.rectangles.WordsPrintingService;
import com.github.gserej.anonymizationtool.rectangles.model.RectangleBox;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Set;

@Service
@Slf4j
public class FileProcessingServiceImpl implements FileProcessingService {

    private StorageService storageService;
    private DocumentMetaInfo documentMetaInfo;
    private OCRService ocrService;
    private ImageToPdfConversionService imageToPdfConversionService;
    private RectangleParsingService rectangleParsingService;
    private ImageLocationsExtractionService imageLocationsExtractionService;
    private WordsPrintingService wordsPrintingService;
    private RectangleBoxSets rectangleBoxSets;

    @Getter
    @Setter
    private Set<RectangleBox> parsedRectangleSet;


    @Autowired
    public FileProcessingServiceImpl(StorageService storageService, DocumentMetaInfo documentMetaInfo, OCRService ocrService,
                                     ImageToPdfConversionService imageToPdfConversionService,
                                     RectangleParsingService rectangleParsingService,
                                     ImageLocationsExtractionService imageLocationsExtractionService,
                                     WordsPrintingService wordsPrintingService, RectangleBoxSets rectangleBoxSets) {
        this.storageService = storageService;
        this.documentMetaInfo = documentMetaInfo;
        this.ocrService = ocrService;
        this.imageToPdfConversionService = imageToPdfConversionService;
        this.rectangleParsingService = rectangleParsingService;
        this.imageLocationsExtractionService = imageLocationsExtractionService;
        this.wordsPrintingService = wordsPrintingService;
        this.rectangleBoxSets = rectangleBoxSets;
    }

    @Override
    public Set<RectangleBox> getRectSet() {
        return parsedRectangleSet;
    }

    @Override
    public boolean processUploadedFile(String filename) {
        File fileToProcess = storageService.loadAsFile(filename);
        String fileExtension = FilenameUtils.getExtension(filename);

        if (fileExtension.equalsIgnoreCase("pdf")) {
            log.info("PDF file found!");
            documentMetaInfo.setDocumentName(filename);
            processPdfFile(fileToProcess);
            return false;
        } else if (fileExtension.equalsIgnoreCase("jpg") || fileExtension.equalsIgnoreCase("png")) {
            log.info("Image file found!");
            processImageFile(fileToProcess);
            return false;
        } else {
            log.info("File with a wrong extension found!");
            return true;
        }
    }

    private void processPdfFile(File fileToProcess) {


        try {
            wordsPrintingService.getWordsLocations(fileToProcess);
        } catch (IOException e) {
            log.error("Exception: getting words locations from PDF failed.");
        }
        if (!rectangleBoxSets.getRectangleBoxSetOriginal().isEmpty()) {
            log.info("Extracted words from PDF page. Starting parsing them.");
        }

        parsedRectangleSet = rectangleParsingService
                .parseRectangleBoxSet(rectangleBoxSets.getRectangleBoxSetOriginal());

        log.info("Parsed rectangles extracted from text to be sent to the page: " + parsedRectangleSet.toString());

        Runnable r = () -> {
            log.info("Trying to find images embedded in PDF file: starting...");
            try {
                imageLocationsExtractionService.extractImages(fileToProcess);
            } catch (IOException e) {
                log.info("Failed to extract images from document: " + e);
            }

            parsedRectangleSet = rectangleParsingService
                    .parseRectangleBoxSet(rectangleBoxSets.getRectangleBoxSetOriginal());

            log.info("Additional rectangles extracted from image(s) to be sent sent to the page: " + parsedRectangleSet.toString());

        };
        new Thread(r).start();

    }

    private void processImageFile(File fileToProcess) {
        try {
            log.info("Embedding image file in PDF file: starting...");
            File imagePdfFile = imageToPdfConversionService.createPdfFromSingleImage(fileToProcess, fileToProcess.getName());
            documentMetaInfo.setDocumentName(imagePdfFile.getName());
            storageService.storeAsFile(imagePdfFile);
            log.info("Embedding image file in PDF file: done");

            Runnable r = () -> {
                log.info("OCR processing: starting...");
                boolean ocrSuccessful = ocrService.doOcrOnSingleImageFile(fileToProcess, documentMetaInfo.getImageRatio());
                if (ocrSuccessful) {
                    log.info("OCR processing: done");
                    if (rectangleBoxSets.getRectangleBoxSetOriginal().isEmpty()) {
                        log.info("No words were found within image!");
                        return;
                    }
                    log.info("Words parsing: starting...");
                    parsedRectangleSet = rectangleParsingService
                            .parseRectangleBoxSet(rectangleBoxSets.getRectangleBoxSetOriginal());
                    log.info("Parsed rectangles extracted from single image to be sent to the page: "
                            + parsedRectangleSet.toString());
                } else {
                    log.error("OCR processing: fail");
                }
            };
            new Thread(r).start();

        } catch (IOException e) {
            log.error("Error processing image file: " + e);
        }
    }
}
