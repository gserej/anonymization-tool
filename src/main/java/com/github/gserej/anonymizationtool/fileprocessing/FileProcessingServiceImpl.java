package com.github.gserej.anonymizationtool.fileprocessing;

import com.github.gserej.anonymizationtool.datacategory.RectangleParsingService;
import com.github.gserej.anonymizationtool.filestorage.Document;
import com.github.gserej.anonymizationtool.filestorage.DocumentRepository;
import com.github.gserej.anonymizationtool.filestorage.StorageService;
import com.github.gserej.anonymizationtool.imageprocessing.ImageLocationsExtractionService;
import com.github.gserej.anonymizationtool.imageprocessing.ImageToPdfConversionService;
import com.github.gserej.anonymizationtool.imageprocessing.OCRService;
import com.github.gserej.anonymizationtool.messages.CurrentMessage;
import com.github.gserej.anonymizationtool.rectangles.WordsExtractionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.UUID;

@Service
@Slf4j
public class FileProcessingServiceImpl implements FileProcessingService {

    private StorageService storageService;
    private DocumentRepository documentRepository;
    private OCRService ocrService;
    private ImageToPdfConversionService imageToPdfConversionService;
    private RectangleParsingService rectangleParsingService;
    private ImageLocationsExtractionService imageLocationsExtractionService;
    private WordsExtractionService wordsExtractionService;


    @Autowired
    public FileProcessingServiceImpl(StorageService storageService, DocumentRepository documentRepository, OCRService ocrService,
                                     ImageToPdfConversionService imageToPdfConversionService,
                                     RectangleParsingService rectangleParsingService,
                                     ImageLocationsExtractionService imageLocationsExtractionService,
                                     WordsExtractionService wordsExtractionService) {
        this.storageService = storageService;
        this.documentRepository = documentRepository;
        this.ocrService = ocrService;
        this.imageToPdfConversionService = imageToPdfConversionService;
        this.rectangleParsingService = rectangleParsingService;
        this.imageLocationsExtractionService = imageLocationsExtractionService;
        this.wordsExtractionService = wordsExtractionService;
    }

    @Override
    public void processUploadedFile(String filename, UUID uuid) {
        File fileToProcess = storageService.load(filename, uuid).toFile();
        String fileExtension = FilenameUtils.getExtension(filename);

        if (fileExtension.equalsIgnoreCase("pdf")) {
            log.info("UUID: " + uuid + " PDF file found!");
            processPdfFile(fileToProcess, uuid);
        } else if (fileExtension.equalsIgnoreCase("jpg") || fileExtension.equalsIgnoreCase("png")) {
            log.info("UUID: " + uuid + " Image file found!");
            processImageFile(fileToProcess, uuid);
        } else {
            log.info("File with a wrong extension found!");
            saveWrongExtensionMessage(uuid);
        }
    }

    private void saveDocumentInfo(String filename, UUID uuid) {
        Document document = new Document(uuid);
        document.setDocumentName(filename);
        document.setCurrentMessage(CurrentMessage.SUCCESSFUL_UPLOAD.getValue() + filename + "!");
        document.setOriginalRectangles(new HashSet<>());
        document.setParsedRectangles(new HashSet<>());
        document.setMarkedRectangles(new HashSet<>());
        documentRepository.save(document);
    }

    private void saveWrongExtensionMessage(UUID uuid) {
        Document document = new Document(uuid);
        document.setCurrentMessage(CurrentMessage.WRONG_EXTENSION.getValue());
        documentRepository.save(document);
    }

    private void processPdfFile(File fileToProcess, UUID uuid) {
        saveDocumentInfo(fileToProcess.getName(), uuid);
        try {
            wordsExtractionService.getWordsLocations(fileToProcess, uuid);
        } catch (IOException e) {
            log.error("Exception: getting words locations from PDF failed." + e);
            e.printStackTrace();
        }

        Document document = documentRepository.findById(uuid).orElseThrow();
        if (document.getOriginalRectangles() != null && !document.getOriginalRectangles().isEmpty()) {
            log.info("Extracted words from PDF page. Starting parsing them.");
            document.setParsedRectangles(rectangleParsingService.parseRectangleBoxSet(document.getOriginalRectangles()));
            log.info("Parsed rectangles extracted from text to be sent to the page: "
                    + document.getParsedRectangles().toString());
        }
        documentRepository.save(document);

        Runnable r = () -> {
            log.info("Trying to find images embedded in PDF file: starting...");
            try {
                imageLocationsExtractionService.extractImages(fileToProcess, uuid);
            } catch (IOException e) {
                log.info("Failed to extract images from document: " + e);
            }

            Document doc = documentRepository.findById(uuid).orElseThrow();
            if (doc.getOriginalRectangles() != null && !doc.getOriginalRectangles().isEmpty()) {
                doc.setParsedRectangles(rectangleParsingService.parseRectangleBoxSet(doc.getOriginalRectangles()));
                log.info("Additional rectangles extracted from image(s) to be sent sent to the page: "
                        + doc.getParsedRectangles().toString());
            }
            documentRepository.save(doc);
        };
        new Thread(r).start();
    }

    private void processImageFile(File fileToProcess, UUID uuid) {
        log.info("Embedding image file in PDF file: starting...");

        try {
            File imagePdfFile = imageToPdfConversionService.createPdfFromSingleImage(fileToProcess, fileToProcess.getName(), uuid);
            saveDocumentInfo(imagePdfFile.getName(), uuid);
            storageService.storeAsFile(imagePdfFile, uuid);
            log.info("Embedding image file in PDF file: done");
        } catch (IOException e) {
            log.error("Error processing image file: " + e);
        }
        Runnable r = () -> {
            log.info("OCR processing: starting...");
            Document document = documentRepository.findById(uuid).orElseThrow();
            float ratio = document.getImageRatio();
            ocrService.doOcrOnSingleImageFile(fileToProcess, ratio, uuid);
            log.info("OCR processing: done");

            Document document2 = documentRepository.findById(uuid).orElseThrow();
            if (document2.getOriginalRectangles().isEmpty()) {
                log.info("No words were found within image!");
                return;
            }
            log.info("Words parsing: starting...");
            document2.setParsedRectangles(rectangleParsingService.parseRectangleBoxSet(document2.getOriginalRectangles()));
            log.info("Parsed rectangles extracted from single image to be sent to the page: "
                    + document2.getParsedRectangles().toString());
            documentRepository.save(document2);
        };
        new Thread(r).start();
    }
}
