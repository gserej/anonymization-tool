package com.github.gserej.anonymizationtool.service.datacategory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PhoneNumberValidationServiceTest {


    private PhoneNumberValidationService phoneNumberValidationService;

    @BeforeEach
    void init() {
        phoneNumberValidationService = new PhoneNumberValidationService();
    }


    @Test
    void isValidPolishPhoneNumber() {
        assertTrue(phoneNumberValidationService.isValidPolishPhoneNumber("883700000"));
    }

    @Test
    void isNotValidPolishPhoneNumber() {
        assertFalse(phoneNumberValidationService.isValidPolishPhoneNumber("12458765456456"));
    }
}
