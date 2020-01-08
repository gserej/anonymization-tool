package com.github.gserej.anonymizationtool.datacategory;

import com.github.gserej.anonymizationtool.datacategory.impl.NumberTypeValidationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NumberTypeValidationServiceImplTest {

    private NumberTypeValidationService numberTypeValidationService;

    @BeforeEach
    void init() {
        numberTypeValidationService = new NumberTypeValidationServiceImpl();
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
