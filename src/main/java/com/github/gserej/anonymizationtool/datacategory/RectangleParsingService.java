package com.github.gserej.anonymizationtool.datacategory;

import com.github.gserej.anonymizationtool.rectangles.model.RectangleBox;

import java.util.List;

public interface RectangleParsingService {

    List<RectangleBox> parseRectangleBoxList(List<RectangleBox> rectangleListToParse);

}
