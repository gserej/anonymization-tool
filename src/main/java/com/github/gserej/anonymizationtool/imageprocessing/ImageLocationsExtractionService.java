package com.github.gserej.anonymizationtool.imageprocessing;

import java.io.File;
import java.io.IOException;

public interface ImageLocationsExtractionService {
    void extractImages(File file) throws IOException;
}
