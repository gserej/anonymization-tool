package com.github.gserej.anonymizationtool.service.datacategory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NumberTypeValidationServiceTest {

    private NumberTypeValidationService numberTypeValidationService;

    @BeforeEach
    void init() {
        numberTypeValidationService = new NumberTypeValidationService();
    }

    @Test
    void isValidPesel() {
        assertTrue(numberTypeValidationService.isValidPesel("02210313718"));
        assertFalse(numberTypeValidationService.isValidPesel("02210313717"));
    }

    @Test
    void isValidNIP() {
        assertTrue(numberTypeValidationService.isValidNIP("5095608177"));
        assertFalse(numberTypeValidationService.isValidNIP("5095608172"));
    }

    @Test
    void isValidREGON() {
        assertTrue(numberTypeValidationService.isValidREGON("375965280"));
        assertFalse(numberTypeValidationService.isValidREGON("375965281"));
    }
}
