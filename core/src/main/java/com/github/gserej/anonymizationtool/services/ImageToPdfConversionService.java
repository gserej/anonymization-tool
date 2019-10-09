package com.github.gserej.anonymizationtool.services;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface ImageToPdfConversionService {

    File createPdfFromSingleImage(File imageFile, String fileName) throws IOException;

    String createPdfFromMultipleImages(List<File> imageFiles, String fileName, File originalDocument) throws IOException;

}
