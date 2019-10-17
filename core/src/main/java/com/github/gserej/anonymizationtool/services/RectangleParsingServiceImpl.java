package com.github.gserej.anonymizationtool.services;

import com.github.gserej.anonymizationtool.model.RectangleBox;
import com.github.gserej.anonymizationtool.model.RectangleBoxLists;
import org.apache.commons.validator.GenericValidator;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RectangleParsingServiceImpl implements RectangleParsingService {

    private NumberTypeValidationService numberTypeValidationService;

    public RectangleParsingServiceImpl(NumberTypeValidationService numberTypeValidationService) {
        this.numberTypeValidationService = numberTypeValidationService;
    }

    @Override
    public List<RectangleBox> parseRectangleBoxList() {

        List<RectangleBox> rectangleBoxList = RectangleBoxLists.getRectangleBoxListOriginal();

        for (RectangleBox rectangleBox : rectangleBoxList) {
            String word = rectangleBox.getWord();
            if (numberTypeValidationService.isValidPesel(word)) {
                rectangleBox.setTypeOfData(2);
                addRectangleToNewList(rectangleBox);
            } else if (numberTypeValidationService.isValidNIP(word)) {
                rectangleBox.setTypeOfData(3);
                addRectangleToNewList(rectangleBox);
            } else if (numberTypeValidationService.isValidREGON(word)) {
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

    private void addRectangleToNewList(RectangleBox rectangleBox) {
        if (!RectangleBoxLists.rectangleBoxListParsed.contains(rectangleBox)) {
            RectangleBoxLists.rectangleBoxListParsed.add(rectangleBox);
        }
    }
}
