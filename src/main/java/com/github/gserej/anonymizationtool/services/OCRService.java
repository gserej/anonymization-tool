package com.github.gserej.anonymizationtool.services;

import com.github.gserej.anonymizationtool.model.EmbeddedImageProperties;

import java.io.File;

public interface OCRService {

    boolean doOcrOnSingleImageFile(File imageFile, float ratio);

    void doOcrOnEmbeddedImageFiles(File imageFile, EmbeddedImageProperties embeddedImageProperties);
}
