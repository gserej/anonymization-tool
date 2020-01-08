package com.github.gserej.anonymizationtool.rectangles;

import java.util.Set;
import java.util.UUID;

public interface MarkedRectanglesProcessingService {

    void processReceivedRectangleSet(Set<RectangleBox> rectangleBoxesMarked, UUID uuid);

}
