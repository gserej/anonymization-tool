package com.github.gserej.anonymizationtool.services;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class NumberTypeValidationServiceImplTest {

    private NumberTypeValidationService numberTypeValidationService;

    @Before
    public void init() {
        numberTypeValidationService = new NumberTypeValidationServiceImpl();
    }

    @Test
    public void isValidPesel() {
        Assert.assertTrue(numberTypeValidationService.isValidPesel("02210313718"));
        Assert.assertFalse(numberTypeValidationService.isValidPesel("02210313717"));
    }

    @Test
    public void isValidNIP() {
        Assert.assertTrue(numberTypeValidationService.isValidNIP("5095608177"));
        Assert.assertFalse(numberTypeValidationService.isValidNIP("5095608172"));
    }

    @Test
    public void isValidREGON() {
        Assert.assertTrue(numberTypeValidationService.isValidREGON("375965280"));
        Assert.assertFalse(numberTypeValidationService.isValidREGON("375965281"));

    }
}