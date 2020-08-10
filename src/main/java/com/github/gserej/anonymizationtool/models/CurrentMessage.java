package com.github.gserej.anonymizationtool.models;

public enum CurrentMessage {

    EMPTY_MESSAGE(1, ""),
    WRONG_EXTENSION(2, "You have uploaded the file with a wrong file extension."),
    SUCCESSFUL_UPLOAD(3, "You have successfully uploaded "),
    DOWNLOAD_LINK_READY(4, "Your file was converted, click the link below to download it.");


    private final Integer key;
    private final String value;

    CurrentMessage(Integer key, String value) {
        this.key = key;
        this.value = value;
    }

    public Integer getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
