package com.github.gserej.anonymizationtool.service.datacategory;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;


@Service
public class NumberTypeValidationService {

    boolean isValidPesel(String pesel) {
        pesel = pesel.trim();
        if (pesel.length() != 11) return false;
        if (!StringUtils.isNumeric(pesel)) {
            return false;
        }

        int[] weights = {1, 3, 7, 9, 1, 3, 7, 9, 1, 3};
        int checksumNumber = Integer.parseInt(pesel.substring(10, 11));

        int sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += Integer.parseInt(pesel.substring(i, i + 1)) * weights[i];
        }
        sum %= 10;
        sum = 10 - sum;
        sum %= 10;

        return (sum == checksumNumber);
    }

    boolean isValidNIP(String nip) {
        int nipLength = nip.length();
        if (nipLength != 10) {
            return false;
        }
        if (!StringUtils.isNumeric(nip)) {
            return false;
        }
        int[] weights = {6, 5, 7, 2, 3, 4, 5, 6, 7};
        int checksumNumber = Integer.parseInt(nip.substring(nipLength - 1));
        return (calculateChecksum(nip, nipLength, weights) == checksumNumber);
    }

    boolean isValidREGON(String regon) {
        int regonLength = regon.length();
        if (!((regonLength == 9) || (regonLength == 14))) {
            return false;
        }
        if (!StringUtils.isNumeric(regon)) {
            return false;
        }
        int[] weights = {8, 9, 2, 3, 4, 5, 6, 7};
        int[] weights14 = {2, 4, 8, 5, 0, 9, 7, 3, 6, 1, 2, 4, 8};
        if (regonLength == 14) {
            weights = weights14;
        }
        int checksumNumber = Integer.parseInt(regon.substring(regonLength - 1));
        int calculatedChecksum = calculateChecksum(regon, regonLength, weights);
        if (calculatedChecksum == 10) {
            calculatedChecksum = 0;
        }
        return (calculatedChecksum == checksumNumber);
    }

    private int calculateChecksum(String stringNumber, int length, int[] weights) {
        int sum = 0;
        for (int i = 0; i < length - 1; i++) {
            sum += Integer.parseInt(String.valueOf(stringNumber.charAt(i))) * weights[i];
        }
        return sum % 11;
    }
}
