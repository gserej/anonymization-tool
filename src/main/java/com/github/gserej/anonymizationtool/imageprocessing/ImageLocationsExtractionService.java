package com.github.gserej.anonymizationtool.imageprocessing;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public interface ImageLocationsExtractionService {
    void extractImages(File file, UUID uuid) throws IOException;
}
