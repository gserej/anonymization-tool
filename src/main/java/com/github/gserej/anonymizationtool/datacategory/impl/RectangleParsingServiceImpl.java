package com.github.gserej.anonymizationtool.datacategory.impl;

import com.github.gserej.anonymizationtool.datacategory.CsvNameExtractionService;
import com.github.gserej.anonymizationtool.datacategory.NumberTypeValidationService;
import com.github.gserej.anonymizationtool.datacategory.PhoneNumberValidationService;
import com.github.gserej.anonymizationtool.datacategory.RectangleParsingService;
import com.github.gserej.anonymizationtool.rectangles.model.RectangleBox;
import org.apache.commons.validator.GenericValidator;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

    private List<RectangleBox> rectangleBoxListParsed = new ArrayList<>();

    @Override
    public List<RectangleBox> parseRectangleBoxList(List<RectangleBox> rectangleListToParse) {

        for (RectangleBox rectangleBox : rectangleListToParse) {
            if (!rectangleBox.isParsed()) {
                String word = rectangleBox.getWord();
                if (word.length() > 2) {
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
                    } else if (phoneNumberValidationService.isValidPolishPhoneNumber(word)) {
                        rectangleBox.setTypeOfData(6);
                        addRectangleToNewList(rectangleBox);
                    } else if (csvNameExtractionService.isPolishFirstOrLastName(word)) {
                        rectangleBox.setTypeOfData(5);
                        addRectangleToNewList(rectangleBox);
                    } else if (word.equalsIgnoreCase("Lorem")) {
                        addRectangleToNewList(rectangleBox);
                    }
                }
            }
        }

        return rectangleBoxListParsed;
    }

    private void addRectangleToNewList(RectangleBox rectangleBox) {
        if (!rectangleBoxListParsed.contains(rectangleBox)) {
            rectangleBoxListParsed.add(rectangleBox);
        }
    }
}