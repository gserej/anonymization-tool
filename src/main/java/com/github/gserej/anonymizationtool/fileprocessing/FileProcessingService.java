package com.github.gserej.anonymizationtool.fileprocessing;

import com.github.gserej.anonymizationtool.filestorage.StorageCannotSaveFileException;
import com.github.gserej.anonymizationtool.imageprocessing.ImageToPdfConversionException;
import com.github.gserej.anonymizationtool.rectangles.WordsExtractionException;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface FileProcessingService {

    void processUploadedFile(MultipartFile multipartFile, UUID uuid) throws FileProcessingWrongExtensionException, StorageCannotSaveFileException, ImageToPdfConversionException, WordsExtractionException;

}
