package com.github.gserej.anonymizationtool.services;

public interface NumberTypeValidationService {
    boolean isValidPesel(String pesel);

    boolean isValidNIP(String nip);

    boolean isValidREGON(String regon);

}
