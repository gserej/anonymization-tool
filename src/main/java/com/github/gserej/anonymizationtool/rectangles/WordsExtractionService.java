package com.github.gserej.anonymizationtool.rectangles;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public interface WordsExtractionService {

    void getWordsLocations(File file, UUID uuid) throws IOException;

}
