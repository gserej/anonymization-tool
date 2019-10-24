package com.github.gserej.anonymizationtool.datatype;

public interface NumberTypeValidationService {
    boolean isValidPesel(String pesel);

    boolean isValidNIP(String nip);

    boolean isValidREGON(String regon);

}
