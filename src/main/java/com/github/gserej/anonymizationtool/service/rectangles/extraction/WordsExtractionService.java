package com.github.gserej.anonymizationtool.service.rectangles.extraction;

import com.github.gserej.anonymizationtool.exceptions.WordsExtractionException;
import com.github.gserej.anonymizationtool.repositories.DocumentRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.awt.geom.AffineTransform;
import java.io.*;
import java.util.UUID;

@Slf4j
@Service
public class WordsExtractionService {

    private final DocumentRepository documentRepository;

    public WordsExtractionService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public void getWordsLocations(File file, UUID uuid) throws WordsExtractionException {
        try (PDDocument document = PDDocument.load(file)) {
            if (!document.isEncrypted()) {
                PDFTextStripper stripper = new WordsExtractionTextStripper(uuid, documentRepository);
                stripper.setSortByPosition(true);
                for (int page = 0; page < document.getNumberOfPages(); ++page) {
                    stripPage(page, stripper, document);
                }
            }
        } catch (IOException e) {
            throw new WordsExtractionException();
        }
    }

    private void stripPage(int page, PDFTextStripper stripper, PDDocument document) throws IOException {
        stripper.setStartPage(page + 1);
        PDPage pdPage = document.getPage(page);
        AffineTransform flipAT = new AffineTransform();
        flipAT.translate(0, pdPage.getBBox().getHeight());
        flipAT.scale(1, -1);

        Writer dummy = new OutputStreamWriter(new ByteArrayOutputStream());
        stripper.setEndPage(page + 1);
        stripper.writeText(document, dummy);
    }

}

