package com.github.gserej.anonymizationtool.datacategory.impl;

import com.github.gserej.anonymizationtool.datacategory.CsvNameExtractionService;
import com.github.gserej.anonymizationtool.datacategory.NumberTypeValidationService;
import com.github.gserej.anonymizationtool.datacategory.PhoneNumberValidationService;
import com.github.gserej.anonymizationtool.datacategory.RectangleParsingService;
import com.github.gserej.anonymizationtool.rectangles.model.RectangleBox;
import org.apache.commons.validator.GenericValidator;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class RectangleParsingServiceImpl implements RectangleParsingService {

    private NumberTypeValidationService numberTypeValidationService;
    private CsvNameExtractionService csvNameExtractionService;
    private PhoneNumberValidationService phoneNumberValidationService;

    public RectangleParsingServiceImpl(NumberTypeValidationService numberTypeValidationService,
                                       CsvNameExtractionService csvNameExtractionService,
                                       PhoneNumberValidationService phoneNumberValidationService) {
        this.numberTypeValidationService = numberTypeValidationService;
        this.csvNameExtractionService = csvNameExtractionService;
        this.phoneNumberValidationService = phoneNumberValidationService;
    }

    private Set<RectangleBox> rectangleBoxSetParsed = new HashSet<>();

    @Override
    public Set<RectangleBox> parseRectangleBoxSet(Set<RectangleBox> rectangleSetToParse) {

        for (RectangleBox rectangleBox : rectangleSetToParse) {
            if (!rectangleBox.isParsed()) {
                String word = rectangleBox.getWord();
                if (word.length() > 2) {
                    if (numberTypeValidationService.isValidPesel(word)) {
                        rectangleBox.setTypeOfData(2);
                        addRectangleToNewSet(rectangleBox);
                    } else if (numberTypeValidationService.isValidNIP(word)) {
                        rectangleBox.setTypeOfData(3);
                        addRectangleToNewSet(rectangleBox);
                    } else if (numberTypeValidationService.isValidREGON(word)) {
                        rectangleBox.setTypeOfData(4);
                        addRectangleToNewSet(rectangleBox);
                    } else if (GenericValidator.isDate(word, null)) {
                        rectangleBox.setTypeOfData(8);
                        addRectangleToNewSet(rectangleBox);
                    } else if (phoneNumberValidationService.isValidPolishPhoneNumber(word)) {
                        rectangleBox.setTypeOfData(6);
                        addRectangleToNewSet(rectangleBox);
                    } else if (csvNameExtractionService.isPolishFirstOrLastName(word)) {
                        rectangleBox.setTypeOfData(5);
                        addRectangleToNewSet(rectangleBox);
                    } else if (word.equalsIgnoreCase("Lorem")) {
                        addRectangleToNewSet(rectangleBox);
                    }
                }
            }
        }

        return rectangleBoxSetParsed;
    }

    private void addRectangleToNewSet(RectangleBox rectangleBox) {
        rectangleBox.setWord("null");
        rectangleBoxSetParsed.add(rectangleBox);
    }
}
