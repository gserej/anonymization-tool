package com.github.gserej.anonymizationtool.imageprocessing;

import java.io.File;
import java.util.UUID;

public interface ImageToPdfConversionService {

    File createPdfFromSingleImage(File imageFile, String fileName, UUID uuid) throws ImageToPdfConversionException;

    String createPdfFromMultipleImages(String fileName, File originalDocument, UUID uuid) throws ImageToPdfConversionException;

}
