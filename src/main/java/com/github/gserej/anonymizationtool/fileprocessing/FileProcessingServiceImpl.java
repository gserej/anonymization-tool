package com.github.gserej.anonymizationtool.fileprocessing;

import com.github.gserej.anonymizationtool.datacategory.RectangleParsingService;
import com.github.gserej.anonymizationtool.filestorage.DocumentMetaInfo;
import com.github.gserej.anonymizationtool.filestorage.StorageService;
import com.github.gserej.anonymizationtool.imageprocessing.ImageLocationsExtractionService;
import com.github.gserej.anonymizationtool.imageprocessing.ImageToPdfConversionService;
import com.github.gserej.anonymizationtool.imageprocessing.OCRService;
import com.github.gserej.anonymizationtool.messages.MessageService;
import com.github.gserej.anonymizationtool.rectangles.RectangleBoxSets;
import com.github.gserej.anonymizationtool.rectangles.WordsPrintingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

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
    private MessageService messageService;


    @Autowired
    public FileProcessingServiceImpl(StorageService storageService, DocumentMetaInfo documentMetaInfo, OCRService ocrService,
                                     ImageToPdfConversionService imageToPdfConversionService,
                                     RectangleParsingService rectangleParsingService,
                                     ImageLocationsExtractionService imageLocationsExtractionService,
                                     WordsPrintingService wordsPrintingService, RectangleBoxSets rectangleBoxSets, MessageService messageService) {
        this.storageService = storageService;
        this.documentMetaInfo = documentMetaInfo;
        this.ocrService = ocrService;
        this.imageToPdfConversionService = imageToPdfConversionService;
        this.rectangleParsingService = rectangleParsingService;
        this.imageLocationsExtractionService = imageLocationsExtractionService;
        this.wordsPrintingService = wordsPrintingService;
        this.rectangleBoxSets = rectangleBoxSets;
        this.messageService = messageService;
    }


    @Override
    public void processUploadedFile(String filename) {
        File fileToProcess = storageService.load(filename).toFile();
        String fileExtension = FilenameUtils.getExtension(filename);

        if (fileExtension.equalsIgnoreCase("pdf")) {
            log.info("PDF file found!");
            documentMetaInfo.setDocumentName(filename);
            processPdfFile(fileToProcess);
            messageService.setCurrentMessage(messageService.getSUCCESSFUL_UPLOAD() + filename + "!");

        } else if (fileExtension.equalsIgnoreCase("jpg") || fileExtension.equalsIgnoreCase("png")) {
            log.info("Image file found!");
            processImageFile(fileToProcess);
            messageService.setCurrentMessage(messageService.getSUCCESSFUL_UPLOAD() + filename + "!");

        } else {
            log.info("File with a wrong extension found!");
            messageService.setCurrentMessage(messageService.getWRONG_EXTENSION());

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

        rectangleBoxSets.setRectangleBoxSetParsed(rectangleParsingService
                .parseRectangleBoxSet(rectangleBoxSets.getRectangleBoxSetOriginal()));

        log.info("Parsed rectangles extracted from text to be sent to the page: "
                + rectangleBoxSets.getRectangleBoxSetParsed().toString());

        Runnable r = () -> {
            log.info("Trying to find images embedded in PDF file: starting...");
            try {
                imageLocationsExtractionService.extractImages(fileToProcess);
            } catch (IOException e) {
                log.info("Failed to extract images from document: " + e);
            }

            rectangleBoxSets.setRectangleBoxSetParsed(rectangleParsingService
                    .parseRectangleBoxSet(rectangleBoxSets.getRectangleBoxSetOriginal()));

            log.info("Additional rectangles extracted from image(s) to be sent sent to the page: "
                    + rectangleBoxSets.getRectangleBoxSetParsed().toString());

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
                    rectangleBoxSets.setRectangleBoxSetParsed(rectangleParsingService
                            .parseRectangleBoxSet(rectangleBoxSets.getRectangleBoxSetOriginal()));
                    log.info("Parsed rectangles extracted from single image to be sent to the page: "
                            + rectangleBoxSets.getRectangleBoxSetParsed().toString());
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
