package com.github.gserej.anonymizationtool.services;

import java.io.File;
import java.io.IOException;

public interface WordsPrintingService {

    void getWordsLocations(File file) throws IOException;

    void drawBoxesAroundMarkedWords(File file) throws IOException;
}
