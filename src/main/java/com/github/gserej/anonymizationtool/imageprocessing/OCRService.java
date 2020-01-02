package com.github.gserej.anonymizationtool.imageprocessing;

import com.github.gserej.anonymizationtool.imageprocessing.model.EmbeddedImageProperties;

import java.io.File;
import java.util.UUID;

public interface OCRService {

    void doOcrOnSingleImageFile(File imageFile, float ratio, UUID uuid);

    void doOcrOnEmbeddedImageFiles(File imageFile, EmbeddedImageProperties embeddedImageProperties, UUID uuid);
}
