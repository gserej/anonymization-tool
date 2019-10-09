package com.github.gserej.anonymizationtool.services;

import java.io.File;
import java.util.Map;

public interface OCRService {

    boolean doOcrOnSingleFile(File imageFile, float ratio);

    void doOcrOnMultipleFiles(File imageFile, Map imagePositionAndSize);
}
