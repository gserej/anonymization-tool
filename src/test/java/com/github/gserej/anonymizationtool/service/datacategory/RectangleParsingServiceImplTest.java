package com.github.gserej.anonymizationtool.service.datacategory;

import com.github.gserej.anonymizationtool.models.RectangleBox;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RectangleParsingServiceImplTest {

    CsvNameValidationService csvNameValidationService = new CsvNameValidationService();
    NumberTypeValidationService numberTypeValidationService = new NumberTypeValidationService();
    PhoneNumberValidationService phoneNumberValidationService = new PhoneNumberValidationService();
    RectangleParsingService rectangleParsingService = new RectangleParsingService(numberTypeValidationService,
            csvNameValidationService,
            phoneNumberValidationService);

    @Test
    void shouldParseRectangleBoxSet() {

        RectangleBox rectangleBox1 = new RectangleBox(12, 10, 2, 3, 1, "Kowalski", 1);
        RectangleBox rectangleBox2 = new RectangleBox(256, 110, 7, 5, 1, "asdasasddsa", 2);
        RectangleBox rectangleBox3 = new RectangleBox(314, 157, 8, 60, 1, "554778962", 3);
        Set<RectangleBox> rectangleBoxSet = new HashSet<>();
        rectangleBoxSet.add(rectangleBox1);
        rectangleBoxSet.add(rectangleBox2);
        rectangleBoxSet.add(rectangleBox3);

        Set<RectangleBox> parsedRectangles = rectangleParsingService.parseRectangleBoxSet(rectangleBoxSet);

        assertTrue(parsedRectangles.contains(rectangleBox1));
        assertFalse(parsedRectangles.contains(rectangleBox2));
        assertTrue(parsedRectangles.contains(rectangleBox3));
    }
}
