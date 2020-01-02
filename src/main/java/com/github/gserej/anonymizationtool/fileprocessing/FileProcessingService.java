package com.github.gserej.anonymizationtool.fileprocessing;

import java.util.UUID;

public interface FileProcessingService {

    void processUploadedFile(String filename, UUID uuid);

}
