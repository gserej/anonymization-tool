package com.github.gserej.anonymizationtool.datacategory.impl;

import com.github.gserej.anonymizationtool.datacategory.CsvNameExtractionService;
import com.github.gserej.anonymizationtool.datacategory.NumberTypeValidationService;
import com.github.gserej.anonymizationtool.datacategory.PhoneNumberValidationService;
import com.github.gserej.anonymizationtool.datacategory.RectangleParsingService;
import com.github.gserej.anonymizationtool.rectangles.RectangleBox;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.GenericValidator;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class RectangleParsingServiceImpl implements RectangleParsingService {

    private final NumberTypeValidationService numberTypeValidationService;
    private final CsvNameExtractionService csvNameExtractionService;
    private final PhoneNumberValidationService phoneNumberValidationService;


    public RectangleParsingServiceImpl(NumberTypeValidationService numberTypeValidationService,
                                       CsvNameExtractionService csvNameExtractionService,
                                       PhoneNumberValidationService phoneNumberValidationService) {
        this.numberTypeValidationService = numberTypeValidationService;
        this.csvNameExtractionService = csvNameExtractionService;
        this.phoneNumberValidationService = phoneNumberValidationService;
    }

    @Override
    public Set<RectangleBox> parseRectangleBoxSet(Set<RectangleBox> rectangleSetToParse) {

        Set<RectangleBox> rectangleBoxSetParsed = new HashSet<>();

        for (RectangleBox rectangleBox : rectangleSetToParse) {
            if (!rectangleBox.isParsed()) {
                String word = rectangleBox.getWord();
                if (word.length() > 2) {
                    if (numberTypeValidationService.isValidPesel(word)) {
                        rectangleBox.setTypeOfData(2);
                        rectangleBoxSetParsed.add(rectangleBox);
                    } else if (numberTypeValidationService.isValidNIP(word)) {
                        rectangleBox.setTypeOfData(3);
                        rectangleBoxSetParsed.add(rectangleBox);
                    } else if (numberTypeValidationService.isValidREGON(word)) {
                        rectangleBox.setTypeOfData(4);
                        rectangleBoxSetParsed.add(rectangleBox);
                    } else if (GenericValidator.isDate(word, null)) {
                        rectangleBox.setTypeOfData(8);
                        rectangleBoxSetParsed.add(rectangleBox);
                    } else if (StringUtils.isNumeric(word)) {
                        if (phoneNumberValidationService.isValidPolishPhoneNumber(word)) {
                            rectangleBox.setTypeOfData(6);
                            rectangleBoxSetParsed.add(rectangleBox);
                        }
                    } else if (csvNameExtractionService.isPolishFirstOrLastName(word)) {
                        rectangleBox.setTypeOfData(5);
                        rectangleBoxSetParsed.add(rectangleBox);
                    } else if (word.equalsIgnoreCase("Lorem")) {
                        rectangleBoxSetParsed.add(rectangleBox);
                    }
                }
            }
        }

        return rectangleBoxSetParsed;
    }

}
