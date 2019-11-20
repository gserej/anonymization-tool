package com.github.gserej.anonymizationtool.datacategory;

import com.github.gserej.anonymizationtool.datacategory.impl.PhoneNumberValidationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PhoneNumberValidationServiceImplTest {


    private PhoneNumberValidationService phoneNumberValidationService;

    @BeforeEach
    void init() {
        phoneNumberValidationService = new PhoneNumberValidationServiceImpl();
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
