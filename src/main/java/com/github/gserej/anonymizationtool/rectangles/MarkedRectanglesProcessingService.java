package com.github.gserej.anonymizationtool.rectangles;

import com.github.gserej.anonymizationtool.document.DocumentNotFoundException;
import com.github.gserej.anonymizationtool.filestorage.StorageCannotSaveFileException;
import com.github.gserej.anonymizationtool.imageprocessing.ImageToPdfConversionException;

import java.util.Set;
import java.util.UUID;

public interface MarkedRectanglesProcessingService {

    void processReceivedRectangleSet(Set<RectangleBox> rectangleBoxesMarked, UUID uuid)
            throws DocumentNotFoundException, StorageCannotSaveFileException, WordsDrawingException,
            ImageToPdfConversionException;

}
