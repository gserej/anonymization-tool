package com.github.gserej.anonymizationtool.datacategory;

import com.github.gserej.anonymizationtool.datacategory.impl.CsvNameExtractionServiceImpl;
import com.github.gserej.anonymizationtool.datacategory.impl.NumberTypeValidationServiceImpl;
import com.github.gserej.anonymizationtool.datacategory.impl.PhoneNumberValidationServiceImpl;
import com.github.gserej.anonymizationtool.datacategory.impl.RectangleParsingServiceImpl;
import com.github.gserej.anonymizationtool.rectangles.RectangleBox;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RectangleParsingServiceImplTest {

    CsvNameExtractionServiceImpl csvNameExtractionService = new CsvNameExtractionServiceImpl();
    NumberTypeValidationServiceImpl numberTypeValidationService = new NumberTypeValidationServiceImpl();
    PhoneNumberValidationServiceImpl phoneNumberValidationService = new PhoneNumberValidationServiceImpl();
    RectangleParsingServiceImpl rectangleParsingService = new RectangleParsingServiceImpl(numberTypeValidationService,
            csvNameExtractionService,
            phoneNumberValidationService);

    @Test
    void shouldParseRectangleBoxSet() {

        RectangleBox rectangleBox1 = new RectangleBox(false, false, false, 12, 10, 2, 3,
                1, "Kowalski", 1);
        RectangleBox rectangleBox2 = new RectangleBox(false, false, false, 256, 110, 7, 5,
                1, "asdasasddsa", 2);
        RectangleBox rectangleBox3 = new RectangleBox(false, false, false, 314, 157, 8, 60,
                1, "554778962", 3);
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
