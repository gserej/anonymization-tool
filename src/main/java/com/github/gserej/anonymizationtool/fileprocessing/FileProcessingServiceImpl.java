package com.github.gserej.anonymizationtool.fileprocessing;

import com.github.gserej.anonymizationtool.datatype.RectangleParsingService;
import com.github.gserej.anonymizationtool.filestorage.StorageService;
import com.github.gserej.anonymizationtool.filestorage.TempName;
import com.github.gserej.anonymizationtool.imageprocessing.ImageLocationsExtractionService;
import com.github.gserej.anonymizationtool.imageprocessing.ImageToPdfConversionService;
import com.github.gserej.anonymizationtool.imageprocessing.OCRService;
import com.github.gserej.anonymizationtool.imageprocessing.model.Ratio;
import com.github.gserej.anonymizationtool.rectangles.RectanglesHandlingController;
import com.github.gserej.anonymizationtool.rectangles.WordsPrintingService;
import com.github.gserej.anonymizationtool.rectangles.model.RectangleBox;
import com.github.gserej.anonymizationtool.rectangles.model.RectangleBoxLists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

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
    private WordsPrintingService wordsPrintingService;

    @Autowired
    public FileProcessingServiceImpl(StorageService storageService, TempName tempName, OCRService ocrService,
                                     ImageToPdfConversionService imageToPdfConversionService,
                                     RectangleParsingService rectangleParsingService,
                                     ImageLocationsExtractionService imageLocationsExtractionService,
                                     RectanglesHandlingController rectanglesHandlingController, WordsPrintingService wordsPrintingService) {
        this.storageService = storageService;
        this.tempName = tempName;
        this.ocrService = ocrService;
        this.imageToPdfConversionService = imageToPdfConversionService;
        this.rectangleParsingService = rectangleParsingService;
        this.imageLocationsExtractionService = imageLocationsExtractionService;
        this.rectanglesHandlingController = rectanglesHandlingController;
        this.wordsPrintingService = wordsPrintingService;
    }

    @Override
    public boolean processUploadedFile(String filename) {
        File fileToProcess = storageService.loadAsFile(filename);
        String fileExtension = FilenameUtils.getExtension(filename);

        if (fileExtension.equalsIgnoreCase("pdf")) {
            log.info("PDF file found!");
            tempName.setTempFileName(filename);
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

    @Override
    public void processPdfFile(File fileToProcess) {

        try {
            wordsPrintingService.getWordsLocations(fileToProcess);
        } catch (IOException e) {
            log.error("Exception: getting words locations from PDF failed.");
        }
        List<RectangleBox> temporaryListFromPdf = rectangleParsingService.parseRectangleBoxList(RectangleBoxLists.getRectangleBoxListOriginal());
        rectanglesHandlingController.setRectObject(temporaryListFromPdf);

        log.info("Rectangles extracted from text to be sent to the page: " + temporaryListFromPdf.toString());

        Runnable r = () -> {
            log.info("Trying to find images embedded in PDF file: starting...");
            try {
                imageLocationsExtractionService.extractImages(fileToProcess);

                List<RectangleBox> temporaryListFromImage = rectangleParsingService.parseRectangleBoxList(RectangleBoxLists.getRectangleBoxListOriginal());
                rectanglesHandlingController.setRectObject(temporaryListFromImage);

                log.info("Additional rectangles extracted from image(s) to be sent sent to the page: " + temporaryListFromImage.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        new Thread(r).start();
    }


    @Override
    public void processImageFile(File fileToProcess) {
        try {
            File imagePdfFile = imageToPdfConversionService.createPdfFromSingleImage(fileToProcess, fileToProcess.getName());
            tempName.setTempFileName(imagePdfFile.getName());
            storageService.storeAsFile(imagePdfFile);

            Runnable r = () -> {
                log.info("OCR processing: starting...");
                boolean ocrSuccessful = ocrService.doOcrOnSingleImageFile(fileToProcess, Ratio.getRatio());
                if (ocrSuccessful) {
                    log.info("OCR processing: done");
                    List<RectangleBox> temporaryList = rectangleParsingService.parseRectangleBoxList(RectangleBoxLists.getRectangleBoxListOriginal());
                    rectanglesHandlingController.setRectObject(temporaryList);
                    log.info("Rectangles extracted from single image to be sent to the page: " + temporaryList.toString());
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
