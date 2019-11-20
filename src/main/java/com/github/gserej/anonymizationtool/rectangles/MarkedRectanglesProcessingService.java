package com.github.gserej.anonymizationtool.rectangles;

import com.github.gserej.anonymizationtool.rectangles.model.RectangleBox;

import java.util.Set;

public interface MarkedRectanglesProcessingService {

    void processReceivedRectangleSet(Set<RectangleBox> rectangleBoxesMarked);

}
