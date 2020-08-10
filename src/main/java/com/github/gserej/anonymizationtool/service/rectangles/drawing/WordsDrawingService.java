package com.github.gserej.anonymizationtool.service.rectangles.drawing;

import com.github.gserej.anonymizationtool.exceptions.WordsDrawingException;
import com.github.gserej.anonymizationtool.repositories.DocumentRepository;
import com.github.gserej.anonymizationtool.service.filestorage.StorageProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
public class WordsDrawingService {

    private static Path rootLocation;
    private final DocumentRepository documentRepository;

    @Autowired
    public WordsDrawingService(StorageProperties properties, DocumentRepository documentRepository) {
        rootLocation = Paths.get(properties.getLocation());
        this.documentRepository = documentRepository;
    }

    public void drawBoxesAroundMarkedWords(File file, UUID uuid) throws WordsDrawingException {
        try (PDDocument pdDocument = PDDocument.load(file)) {
            WordsDrawingTextStripper wordsDrawingTextStripper = new WordsDrawingTextStripper(rootLocation, documentRepository);
            wordsDrawingTextStripper.setSortByPosition(true);
            for (int page = 0; page < pdDocument.getNumberOfPages(); ++page) {
                wordsDrawingTextStripper.stripPage(page, pdDocument, uuid);
            }
        } catch (IOException e) {
            throw new WordsDrawingException();
        }
    }
}

