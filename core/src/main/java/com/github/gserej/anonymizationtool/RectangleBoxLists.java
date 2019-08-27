package com.github.gserej.anonymizationtool;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.GenericValidator;

import java.util.ArrayList;
import java.util.List;

@Slf4j
class RectangleBoxLists {

    @Getter
    @Setter
    static List<RectangleBox> rectangleBoxList = new ArrayList<>();

    @Getter
    @Setter
    static List<RectangleBox> rectangleBoxListParsed = new ArrayList<>();

    @Getter
    @Setter
    static List<RectangleBox> rectangleBoxListMarked = new ArrayList<>();

    static List<RectangleBox> parseRectangleBoxList(List<RectangleBox> rectangleBoxList) {
        for (RectangleBox rectangleBox : rectangleBoxList) {
            String word = rectangleBox.getWord();
            if (DataTypeValidators.isValidNIP(word) ||
                    DataTypeValidators.isValidPesel(word) ||
                    DataTypeValidators.isValidREGON(word) ||
                    GenericValidator.isDate(word, null) ||
                    word.equals("PX031608") ||
                    word.equals("Lorem") ||
                    word.equals("000100700006176")) {
                rectangleBoxListParsed.add(rectangleBox);
            }

        }
        return rectangleBoxListParsed;
    }

}

