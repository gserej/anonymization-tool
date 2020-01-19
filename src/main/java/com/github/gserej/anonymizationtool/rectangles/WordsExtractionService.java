package com.github.gserej.anonymizationtool.rectangles;

import java.io.File;
import java.util.UUID;

public interface WordsExtractionService {

    void getWordsLocations(File file, UUID uuid) throws WordsExtractionException;

}
