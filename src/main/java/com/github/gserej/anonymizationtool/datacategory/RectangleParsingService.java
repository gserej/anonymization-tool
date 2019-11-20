package com.github.gserej.anonymizationtool.datacategory;

import com.github.gserej.anonymizationtool.rectangles.model.RectangleBox;

import java.util.Set;

public interface RectangleParsingService {

    Set<RectangleBox> parseRectangleBoxSet(Set<RectangleBox> rectangleSetToParse);

}
