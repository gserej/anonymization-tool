package com.github.gserej.anonymizationtool.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvExtractionServiceImplTest {


    private CsvExtractionService csvExtractionService;

    @BeforeEach
    void setUp() {
        csvExtractionService = new CsvExtractionServiceImpl();
    }

    @Test
    void isCommonPolishLastNameFound() {
        assertTrue(csvExtractionService.isPolishFirstOrLastName("Kowalski"));
    }

    @Test
    void isNoNamesFound() {
        assertFalse(csvExtractionService.isPolishFirstOrLastName("AAAAAAAAAAA"));
    }
}