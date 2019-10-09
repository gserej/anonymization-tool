package com.github.gserej.anonymizationtool.services;

import com.github.gserej.anonymizationtool.model.RectangleBox;

import java.util.List;

public interface MarkedRectanglesProcessingService {

    void processReceivedRectangleList(List<RectangleBox> rectangleBoxesMarked);

}
