package com.github.gserej.anonymizationtool.services;

import java.io.File;

public interface FileProcessingService {

    boolean processUploadedFile(String filename);

    void processPdfFile(File fileToProcess);

    void processImageFile(File fileToProcess);

}
