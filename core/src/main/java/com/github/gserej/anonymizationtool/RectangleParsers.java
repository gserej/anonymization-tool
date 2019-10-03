package com.github.gserej.anonymizationtool;

import com.github.gserej.anonymizationtool.util.NumberTypeValidators;
import org.apache.commons.validator.GenericValidator;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RectangleParsers {

    static List<RectangleBox> parseRectangleBoxList(List<RectangleBox> rectangleBoxList) {

        for (RectangleBox rectangleBox : rectangleBoxList) {
            String word = rectangleBox.getWord();
            if (NumberTypeValidators.isValidPesel(word)) {
                rectangleBox.setTypeOfData(2);
                addRectangleToNewList(rectangleBox);
            } else if (NumberTypeValidators.isValidNIP(word)) {
                rectangleBox.setTypeOfData(3);
                addRectangleToNewList(rectangleBox);
            } else if (NumberTypeValidators.isValidREGON(word)) {
                rectangleBox.setTypeOfData(4);
                addRectangleToNewList(rectangleBox);
            } else if (GenericValidator.isDate(word, null)) {
                rectangleBox.setTypeOfData(8);
                addRectangleToNewList(rectangleBox);
            } else if (word.equalsIgnoreCase("Lorem")) {
                addRectangleToNewList(rectangleBox);
            }
        }
        return RectangleBoxLists.rectangleBoxListParsed;
    }

    private static void addRectangleToNewList(RectangleBox rectangleBox) {
        if (!RectangleBoxLists.rectangleBoxListParsed.contains(rectangleBox)) {
            RectangleBoxLists.rectangleBoxListParsed.add(rectangleBox);
        }
    }
}
