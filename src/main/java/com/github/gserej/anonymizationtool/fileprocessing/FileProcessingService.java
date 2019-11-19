package com.github.gserej.anonymizationtool.fileprocessing;

import com.github.gserej.anonymizationtool.rectangles.model.RectangleBox;

import java.util.List;

public interface FileProcessingService {

    boolean processUploadedFile(String filename);

    List<RectangleBox> getRectList();

}
