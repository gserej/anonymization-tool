package com.github.gserej.anonymizationtool.fileprocessing;

import com.github.gserej.anonymizationtool.rectangles.model.RectangleBox;

import java.util.Set;

public interface FileProcessingService {

    boolean processUploadedFile(String filename);

    Set<RectangleBox> getRectSet();

}
