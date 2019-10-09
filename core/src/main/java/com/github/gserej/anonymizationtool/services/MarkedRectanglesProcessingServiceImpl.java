package com.github.gserej.anonymizationtool.services;

import com.github.gserej.anonymizationtool.model.RectangleBox;
import com.github.gserej.anonymizationtool.filestorage.StorageService;
import com.github.gserej.anonymizationtool.filestorage.TempName;
import com.github.gserej.anonymizationtool.model.RectangleBoxLists;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class MarkedRectanglesProcessingServiceImpl implements MarkedRectanglesProcessingService {

    @Getter
    @Setter
    private static List<String> tempImagesList;
    @Autowired
    private StorageService storageService;
    @Autowired
    private TempName tempName;
    private ImageToPdfConversionService imageToPdfConversionService;

    public MarkedRectanglesProcessingServiceImpl(ImageToPdfConversionService imageToPdfConversionService) {
        this.imageToPdfConversionService = imageToPdfConversionService;
    }


    @Override
    public void processReceivedRectangleList(List<RectangleBox> rectangleBoxesMarked) {

        RectangleBoxLists.setRectangleBoxListMarked(rectangleBoxesMarked);

        log.info("Marked rectangles received from the page: " + rectangleBoxesMarked.toString());

        File fileToProcess = storageService.loadAsFile(tempName.getTempFileName());
        log.info(fileToProcess.getName());

        try {
            WordsPrinterDrawer.drawLocations(fileToProcess);

            log.info(tempImagesList.toString());
            List<File> imageFilesList = new ArrayList<>();
            for (String s : tempImagesList) {
                imageFilesList.add(storageService.loadAsFile(s));
            }

            String pathToDonePdf = imageToPdfConversionService.createPdfFromMultipleImages(imageFilesList,
                    tempName.getTempFileName(), fileToProcess);
            storageService.storeAsFile(new File(pathToDonePdf));
            tempName.setTempFileName(null);
        } catch (
                IOException e) {
            log.error("Error processing file" + e);
        }
    }


}
