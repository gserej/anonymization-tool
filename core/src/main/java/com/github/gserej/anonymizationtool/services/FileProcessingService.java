package com.github.gserej.anonymizationtool.services;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public interface FileProcessingService {

    boolean processUploadedFile(MultipartFile file);

    void processPdfFile(File fileToProcess, MultipartFile file);

    void processImageFile(File fileToProcess);

}
