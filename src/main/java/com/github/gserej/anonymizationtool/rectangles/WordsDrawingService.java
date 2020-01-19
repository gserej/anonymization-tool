package com.github.gserej.anonymizationtool.rectangles;

import java.io.File;
import java.util.UUID;

public interface WordsDrawingService {

    void drawBoxesAroundMarkedWords(File file, UUID uuid) throws WordsDrawingException;

}
