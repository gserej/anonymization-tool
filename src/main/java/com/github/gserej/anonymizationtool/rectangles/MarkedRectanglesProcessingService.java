package com.github.gserej.anonymizationtool.rectangles;

import com.github.gserej.anonymizationtool.rectangles.model.RectangleBox;

import java.util.List;

public interface MarkedRectanglesProcessingService {

    void processReceivedRectangleList(List<RectangleBox> rectangleBoxesMarked);

}
