package com.github.gserej.anonymizationtool.fileprocessing;

import com.github.gserej.anonymizationtool.datacategory.RectangleParsingService;
import com.github.gserej.anonymizationtool.document.Document;
import com.github.gserej.anonymizationtool.document.DocumentRepository;
import com.github.gserej.anonymizationtool.filestorage.StorageCannotSaveFileException;
import com.github.gserej.anonymizationtool.filestorage.StorageService;
import com.github.gserej.anonymizationtool.imageprocessing.*;
import com.github.gserej.anonymizationtool.messages.CurrentMessage;
import com.github.gserej.anonymizationtool.rectangles.WordsExtractionException;
import com.github.gserej.anonymizationtool.rectangles.WordsExtractionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.HashSet;
import java.util.UUID;

@Service
@Slf4j
public class FileProcessingServiceImpl implements FileProcessingService {

    private final StorageService storageService;
    private final DocumentRepository documentRepository;
    private final OCRService ocrService;
    private final ImageToPdfConversionService imageToPdfConversionService;
    private final RectangleParsingService rectangleParsingService;
    private final ImageLocationsExtractionService imageLocationsExtractionService;
    private final WordsExtractionService wordsExtractionService;


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
    public void processUploadedFile(MultipartFile multipartFile, UUID uuid) throws FileProcessingWrongExtensionException,
            StorageCannotSaveFileException, ImageToPdfConversionException, WordsExtractionException {
        String fileExtension = FilenameUtils.getExtension(multipartFile.getOriginalFilename());
        if (fileExtension == null) {
            throw new FileProcessingWrongExtensionException();
        } else {
            if (fileExtension.equalsIgnoreCase("pdf")) {
                log.debug("UUID: " + uuid + " PDF file found!");
                storageService.store(multipartFile, uuid);
                File fileToProcess = storageService.load(multipartFile.getOriginalFilename(), uuid).toFile();
                processPdfFile(fileToProcess, uuid);
            } else if (fileExtension.equalsIgnoreCase("jpg")
                    || fileExtension.equalsIgnoreCase("png")) {
                log.debug("UUID: " + uuid + " Image file found!");
                storageService.store(multipartFile, uuid);
                File fileToProcess = storageService.load(multipartFile.getOriginalFilename(), uuid).toFile();
                processImageFile(fileToProcess, uuid);
            } else {
                log.debug("File with a wrong extension found!");
                saveWrongExtensionMessage(uuid);
                throw new FileProcessingWrongExtensionException();
            }
        }
    }

    private void saveDocumentInfo(String filename, UUID uuid) {
        Document document;
        if (documentRepository.findById(uuid).isPresent()) {
            document = documentRepository.findById(uuid).get();
        } else {
            document = new Document(uuid);
        }
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

    private void processPdfFile(File fileToProcess, UUID uuid) throws WordsExtractionException {
        saveDocumentInfo(fileToProcess.getName(), uuid);
        wordsExtractionService.getWordsLocations(fileToProcess, uuid);
        Document document = documentRepository.findById(uuid).orElseThrow();
        if (document.getOriginalRectangles() != null && !document.getOriginalRectangles().isEmpty()) {
            log.debug("Extracted words from PDF page. Starting parsing them.");
            document.setParsedRectangles(rectangleParsingService.parseRectangleBoxSet(document.getOriginalRectangles()));
            log.debug("Parsed rectangles extracted from text: "
                    + document.getParsedRectangles().toString());
            documentRepository.save(document);
        }

        Runnable r = () -> {
            log.debug("Trying to find images embedded in PDF file: starting...");
            try {
                imageLocationsExtractionService.extractImages(fileToProcess, uuid);
            } catch (ImageExtractionException e) {
                return;
            }
            Document doc = documentRepository.findById(uuid).orElseThrow();
            if (doc.getOriginalRectangles() != null && !doc.getOriginalRectangles().isEmpty()) {
                doc.setParsedRectangles(rectangleParsingService.parseRectangleBoxSet(doc.getOriginalRectangles()));
                log.debug("Additional rectangles extracted from image(s) parsed: "
                        + doc.getParsedRectangles().toString());
            }
            documentRepository.save(doc);
        };
        new Thread(r).start();
    }

    private void processImageFile(File fileToProcess, UUID uuid) throws StorageCannotSaveFileException, ImageToPdfConversionException {
        log.debug("Embedding image file in PDF file: starting...");
        File imagePdfFile = imageToPdfConversionService.createPdfFromSingleImage(fileToProcess, fileToProcess.getName(), uuid);
        saveDocumentInfo(imagePdfFile.getName(), uuid);
        storageService.storeAsFile(imagePdfFile, uuid);
        log.debug("Embedding image file in PDF file: done");
        Runnable r = () -> {
            log.debug("OCR processing: starting...");
            Document document = documentRepository.findById(uuid).orElseThrow();
            float ratio = document.getImageRatio();
            ocrService.doOcrOnSingleImageFile(fileToProcess, ratio, uuid);
            log.debug("OCR processing: done");

            document = documentRepository.findById(uuid).orElseThrow();
            if (document.getOriginalRectangles().isEmpty()) {
                log.debug("No words were found within image!");
                return;
            }
            log.debug("Words parsing: starting...");
            document.setParsedRectangles(rectangleParsingService.parseRectangleBoxSet(document.getOriginalRectangles()));
            log.debug("Parsed rectangles extracted from single image: " + document.getParsedRectangles().toString());
            documentRepository.save(document);
        };
        new Thread(r).start();
    }
}
