package com.github.gserej.anonymizationtool.imageprocessing;

import com.github.gserej.anonymizationtool.imageprocessing.model.EmbeddedImageProperties;

import java.io.File;

public interface OCRService {

    boolean doOcrOnSingleImageFile(File imageFile, float ratio);

    void doOcrOnEmbeddedImageFiles(File imageFile, EmbeddedImageProperties embeddedImageProperties);
}
