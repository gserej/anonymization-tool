package com.github.gserej.anonymizationtool.datacategory;

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
    void isCommonPolishLastNameFound1() {
        assertTrue(csvNameExtractionService.isPolishFirstOrLastName("Kowalski"));
    }

    @Test
    void isCommonPolishLastNameFound2() {
        assertTrue(csvNameExtractionService.isPolishFirstOrLastName("Nowak"));
    }

    @Test
    void isCommonPolishLastNameLoweCase() {
        assertFalse(csvNameExtractionService.isPolishFirstOrLastName("nowak"));
    }

    @Test
    void isNoNamesFound() {
        assertFalse(csvNameExtractionService.isPolishFirstOrLastName("AAAAAAAAAAA"));
    }
}
