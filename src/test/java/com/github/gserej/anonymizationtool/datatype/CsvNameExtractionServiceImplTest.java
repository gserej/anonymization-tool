package com.github.gserej.anonymizationtool.datatype;

import com.github.gserej.anonymizationtool.datacategory.CsvNameExtractionService;
import com.github.gserej.anonymizationtool.datacategory.impl.CsvNameExtractionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvNameExtractionServiceImplTest {


    private CsvNameExtractionService csvNameExtractionService;

    @BeforeEach
    void setUp() {
        csvNameExtractionService = new CsvNameExtractionServiceImpl();
    }

    @Test
    void isCommonPolishLastNameFound() {
        assertTrue(csvNameExtractionService.isPolishFirstOrLastName("Kowalski"));
    }

    @Test
    void isNoNamesFound() {
        assertFalse(csvNameExtractionService.isPolishFirstOrLastName("AAAAAAAAAAA"));
    }
}
