package com.github.gserej.anonymizationtool.imageprocessing;

import java.io.File;
import java.io.IOException;

public interface ImageToPdfConversionService {

    File createPdfFromSingleImage(File imageFile, String fileName) throws IOException;

    String createPdfFromMultipleImages(String fileName, File originalDocument) throws IOException;

}
