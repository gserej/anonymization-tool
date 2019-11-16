package com.github.gserej.anonymizationtool.fileprocessing;

import com.github.gserej.anonymizationtool.rectangles.model.RectangleBox;

import java.io.File;
import java.util.List;

public interface FileProcessingService {

    boolean processUploadedFile(String filename);

    void processPdfFile(File fileToProcess);

    void processImageFile(File fileToProcess);

    List<RectangleBox> getRectList();

}
