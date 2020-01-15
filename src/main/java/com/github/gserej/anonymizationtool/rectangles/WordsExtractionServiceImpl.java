package com.github.gserej.anonymizationtool.rectangles;

import com.github.gserej.anonymizationtool.filestorage.Document;
import com.github.gserej.anonymizationtool.filestorage.DocumentRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.fontbox.util.BoundingBox;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType3Font;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.util.List;
import java.util.*;

@Slf4j
@Service
public class WordsExtractionServiceImpl implements WordsExtractionService {

    private final DocumentRepository documentRepository;

    public WordsExtractionServiceImpl(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @Override
    public void getWordsLocations(File file, UUID uuid) throws IOException {
        try (PDDocument document = PDDocument.load(file)) {
            if (!document.isEncrypted()) {
                PDFTextStripper stripper = new PrintTextLocations(uuid, documentRepository);
                stripper.setSortByPosition(true);
                for (int page = 0; page < document.getNumberOfPages(); ++page) {
                    stripPage(page, stripper, document);
                }
            }
        } catch (NullPointerException e) {
            log.error("Null pointer exception loading PDF file " + e);
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

class PrintTextLocations extends PDFTextStripper {
    private final UUID uuid;
    private final DocumentRepository documentRepository;

    public PrintTextLocations(UUID uuid, DocumentRepository documentRepository) throws IOException {
        this.uuid = uuid;
        this.documentRepository = documentRepository;
    }

    @Override
    protected void writeString(String string, java.util.List<TextPosition> textPositions) throws IOException {
        String wordSeparator = getWordSeparator();
        java.util.List<TextPosition> word = new ArrayList<>();

        Document document = documentRepository.findById(uuid).orElseThrow();
        Set<RectangleBox> originalRectangleSet = document.getOriginalRectangles();
        if (originalRectangleSet == null) {
            originalRectangleSet = new HashSet<>();
        }

        int pageNumber = super.getCurrentPageNo();
        for (TextPosition text : textPositions) {
            String thisChar = text.getUnicode();
            if (thisChar != null && thisChar.length() >= 1) {
                if (!thisChar.equals(wordSeparator)) {
                    word.add(text);
                } else if (!word.isEmpty()) {
                    originalRectangleSet.add(printWord(word, pageNumber));
                    word.clear();
                }
            }
        }
        if (!word.isEmpty()) {
            originalRectangleSet.add(printWord(word, pageNumber));
            word.clear();
        }

        document.setOriginalRectangles(originalRectangleSet);
        documentRepository.save(document);

    }

    private RectangleBox printWord(List<TextPosition> word, int page) throws IOException {
        StringBuilder builder = new StringBuilder();
        TextPosition text = word.get(0);

        PDFont font = text.getFont();
        BoundingBox bbox = font.getBoundingBox();
        float xadvance = 0.0f;

        for (TextPosition letter : word) {
            builder.append(letter.getUnicode());
            xadvance += font.getWidth(letter.getCharacterCodes()[0]);
        }
        String singleWord = builder.toString();
        AffineTransform at = text.getTextMatrix().createAffineTransform();

        Rectangle2D.Float rect = new Rectangle2D.Float(0, bbox.getUpperRightY() - bbox.getHeight() * 0.05f,
                xadvance, bbox.getHeight() * 0.85f);
        if (font instanceof PDType3Font) {
            at.concatenate(font.getFontMatrix().createAffineTransform());
        } else {
            at.scale(1 / 1000f, 1 / 1000f);
        }
        Shape s = at.createTransformedShape(rect);
        AffineTransform flipAT = new AffineTransform();
        s = flipAT.createTransformedShape(s);

        return new RectangleBox(false,
                false,
                false,
                (float) s.getBounds2D().getX(),
                text.getPageHeight() - (float) s.getBounds2D().getY(),
                (float) s.getBounds2D().getWidth(),
                (float) s.getBounds2D().getHeight(),
                1, singleWord, page);

    }
}
