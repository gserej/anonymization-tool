package com.github.gserej.anonymizationtool.imageprocessing;

import java.io.File;
import java.util.UUID;

public interface ImageLocationsExtractionService {
    void extractImages(File file, UUID uuid) throws ImageExtractionException;
}
