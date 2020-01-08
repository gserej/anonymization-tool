package com.github.gserej.anonymizationtool.datacategory.impl;

import com.github.gserej.anonymizationtool.datacategory.PhoneNumberValidationService;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PhoneNumberValidationServiceImpl implements PhoneNumberValidationService {

    private PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

    @Override
    public boolean isValidPolishPhoneNumber(String phoneNumber) {
        try {
            Phonenumber.PhoneNumber polishNumberProto = phoneUtil.parse(phoneNumber, "PL");
            return phoneUtil.isValidNumber(polishNumberProto);
        } catch (NumberParseException e) {
            log.error("NumberParseException" + e);
            return false;
        }
    }
}
