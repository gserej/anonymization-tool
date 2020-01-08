package com.github.gserej.anonymizationtool.imageprocessing;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public interface ImageToPdfConversionService {

    File createPdfFromSingleImage(File imageFile, String fileName, UUID uuid) throws IOException;

    String createPdfFromMultipleImages(String fileName, File originalDocument, UUID uuid) throws IOException;

}
