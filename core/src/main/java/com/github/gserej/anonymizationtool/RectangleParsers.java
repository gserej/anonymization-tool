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
            if (NumberTypeValidators.isValidNIP(word) ||
                    NumberTypeValidators.isValidPesel(word) ||
                    NumberTypeValidators.isValidREGON(word) ||
                    GenericValidator.isDate(word, null) ||
                    word.equals("PX031608") ||
                    word.equals("Lorem") ||
                    word.equals("000100700006176")) {

                if (!RectangleBoxLists.rectangleBoxListParsed.contains(rectangleBox)) {
                    RectangleBoxLists.rectangleBoxListParsed.add(rectangleBox);
                }
            }

        }
        return RectangleBoxLists.rectangleBoxListParsed;
    }
}
