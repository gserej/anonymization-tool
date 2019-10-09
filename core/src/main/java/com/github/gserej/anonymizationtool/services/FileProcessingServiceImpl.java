package com.github.gserej.anonymizationtool.services;

import com.github.gserej.anonymizationtool.controllers.RectanglesHandlingController;
import com.github.gserej.anonymizationtool.filestorage.StorageService;
import com.github.gserej.anonymizationtool.filestorage.TempName;
import com.github.gserej.anonymizationtool.model.Ratio;
import com.github.gserej.anonymizationtool.model.RectangleBoxLists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Service
@Slf4j
public class FileProcessingServiceImpl implements FileProcessingService {


    private StorageService storageService;
    private TempName tempName;
    private OCRService ocrService;
    private ImageToPdfConversionService imageToPdfConversionService;
    private RectangleParsingService rectangleParsingService;
    private ImageLocationsExtractionService imageLocationsExtractionService;
    private RectanglesHandlingController rectanglesHandlingController;

    @Autowired
    public FileProcessingServiceImpl(StorageService storageService, TempName tempName, OCRService ocrService,
                                     ImageToPdfConversionService imageToPdfConversionService,
                                     RectangleParsingService rectangleParsingService,
                                     ImageLocationsExtractionService imageLocationsExtractionService,
                                     RectanglesHandlingController rectanglesHandlingController) {
        this.storageService = storageService;
        this.tempName = tempName;
        this.ocrService = ocrService;
        this.imageToPdfConversionService = imageToPdfConversionService;
        this.rectangleParsingService = rectangleParsingService;
        this.imageLocationsExtractionService = imageLocationsExtractionService;
        this.rectanglesHandlingController = rectanglesHandlingController;
    }

    @Override
    public boolean processUploadedFile(MultipartFile file) {
        File fileToProcess = storageService.loadAsFile(file.getOriginalFilename());
        String fileExtension = FilenameUtils.getExtension(fileToProcess.getName());

        if (fileExtension.equalsIgnoreCase("pdf")) {
            log.info("pdf file found");
            processPdfFile(fileToProcess, file);
            return false;
        } else if (fileExtension.equalsIgnoreCase("jpg") || fileExtension.equalsIgnoreCase("png")) {
            log.info("image file found");
            processImageFile(fileToProcess);
            return false;
        } else {
            log.info("file with a wrong extension found");
            return true;
        }
    }

    @Override
    public void processPdfFile(File fileToProcess, MultipartFile file) {
        tempName.setTempFileName(file.getOriginalFilename());
        try {
            WordsPrinterDrawer.printLocations(fileToProcess);
            rectanglesHandlingController.setRectObject(rectangleParsingService.parseRectangleBoxList(RectangleBoxLists.getRectangleBoxListOriginal()));
            log.info("Rectangles sent to the page: " + RectangleBoxLists.getRectangleBoxListParsed().toString());
            Runnable r = () -> {
                try {
                    imageLocationsExtractionService.extractImages(fileToProcess);
                    rectanglesHandlingController.setRectObject(rectangleParsingService.parseRectangleBoxList(RectangleBoxLists.getRectangleBoxListOriginal()));
                    log.info("Additional rectangles sent to the page: " + RectangleBoxLists.getRectangleBoxListParsed().toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            };
            new Thread(r).start();

        } catch (IOException e) {
            log.error("Error processing PDF file: " + e);
        }
    }


    @Override
    public void processImageFile(File fileToProcess) {
        try {
            File imagePdfFile = imageToPdfConversionService.createPdfFromSingleImage(fileToProcess, fileToProcess.getName());
            MultipartFile multipartFile = new MockMultipartFile(imagePdfFile.getName(),
                    imagePdfFile.getName(), "text/plain", IOUtils.toByteArray(new FileInputStream(imagePdfFile)));
            storageService.store(multipartFile);
            tempName.setTempFileName(multipartFile.getOriginalFilename());

            Runnable r = () -> {
                log.info("OCR processing: starting...");
                boolean ocrSuccessful = ocrService.doOcrOnSingleFile(fileToProcess, Ratio.getRatio());
                if (ocrSuccessful) {
                    log.info("OCR processing: done");
                    rectanglesHandlingController.setRectObject(rectangleParsingService.parseRectangleBoxList(RectangleBoxLists.getRectangleBoxListOriginal()));
                    log.info("Rectangles sent to the page: " + RectangleBoxLists.getRectangleBoxListParsed().toString());
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
