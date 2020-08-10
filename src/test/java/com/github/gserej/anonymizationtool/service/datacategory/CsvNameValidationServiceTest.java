package com.github.gserej.anonymizationtool.service.datacategory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvNameValidationServiceTest {


    private CsvNameValidationService csvNameValidationService;

    @BeforeEach
    void setUp() {
        csvNameValidationService = new CsvNameValidationService();
    }

    @Test
    void isCommonPolishLastNameFound1() {
        assertTrue(csvNameValidationService.isPolishFirstOrLastName("Kowalski"));
    }

    @Test
    void isCommonPolishLastNameFound2() {
        assertTrue(csvNameValidationService.isPolishFirstOrLastName("Nowak"));
    }

    @Test
    void isCommonPolishLastNameLowerCaseNotFound() {
        assertFalse(csvNameValidationService.isPolishFirstOrLastName("nowak"));
    }

    @Test
    void isNoNamesNotFound() {
        assertFalse(csvNameValidationService.isPolishFirstOrLastName("AAAAAAAAAAA"));
    }
}
