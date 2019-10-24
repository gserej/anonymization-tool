package com.github.gserej.anonymizationtool.rectangles;

import com.github.gserej.anonymizationtool.filestorage.StorageService;
import com.github.gserej.anonymizationtool.filestorage.TempName;
import com.github.gserej.anonymizationtool.filestorage.TemporaryImageList;
import com.github.gserej.anonymizationtool.imageprocessing.ImageToPdfConversionService;
import com.github.gserej.anonymizationtool.rectangles.model.RectangleBox;
import com.github.gserej.anonymizationtool.rectangles.model.RectangleBoxLists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class MarkedRectanglesProcessingServiceImpl implements MarkedRectanglesProcessingService {


    private final StorageService storageService;
    private final TempName tempName;
    private ImageToPdfConversionService imageToPdfConversionService;
    private WordsPrintingService wordsPrintingService;


    public MarkedRectanglesProcessingServiceImpl(ImageToPdfConversionService imageToPdfConversionService, WordsPrintingService wordsPrintingService, StorageService storageService, TempName tempName) {
        this.imageToPdfConversionService = imageToPdfConversionService;
        this.wordsPrintingService = wordsPrintingService;
        this.storageService = storageService;
        this.tempName = tempName;
    }


    @Override
    public void processReceivedRectangleList(List<RectangleBox> rectangleBoxesMarked) {

        log.info("Marked rectangles received from the page: " + rectangleBoxesMarked.toString());
        RectangleBoxLists.setRectangleBoxListMarked(rectangleBoxesMarked);

        File pdfFileToProcess = storageService.loadAsFile(tempName.getTempFileName());
        log.info("Loaded PDF file: " + pdfFileToProcess.getName());

        try {
            wordsPrintingService.drawBoxesAroundMarkedWords(pdfFileToProcess);
        } catch (IOException e) {
            log.error("Error drawing boxes around words" + e);
        }
        try {
            log.info("List of images: " + TemporaryImageList.getTempImagesList().toString());
            String pathToProcessedPdf = imageToPdfConversionService.createPdfFromMultipleImages(
                    tempName.getTempFileName(), pdfFileToProcess);

            storageService.storeAsFile(new File(pathToProcessedPdf));
            log.info("Document ready to download.");
        } catch (IOException e) {
            log.error("Error creating PDF file from images.");
        } finally {
            tempName.setTempFileName(null);
        }
    }
}
