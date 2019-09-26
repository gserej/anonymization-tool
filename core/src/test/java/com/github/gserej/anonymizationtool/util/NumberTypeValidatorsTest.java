package com.github.gserej.anonymizationtool.util;

import org.junit.Assert;
import org.junit.Test;

public class NumberTypeValidatorsTest {

    @Test
    public void isValidPesel() {
        Assert.assertTrue(NumberTypeValidators.isValidPesel("02210313718"));
        Assert.assertFalse(NumberTypeValidators.isValidPesel("02210313717"));
    }

    @Test
    public void isValidNIP() {
        Assert.assertTrue(NumberTypeValidators.isValidNIP("5095608177"));
        Assert.assertFalse(NumberTypeValidators.isValidNIP("5095608172"));
    }

    @Test
    public void isValidREGON() {
        Assert.assertTrue(NumberTypeValidators.isValidREGON("375965280"));
        Assert.assertFalse(NumberTypeValidators.isValidREGON("375965281"));

    }
}