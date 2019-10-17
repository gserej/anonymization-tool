/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.gserej.anonymizationtool.services;

import com.github.gserej.anonymizationtool.filestorage.StorageProperties;
import com.github.gserej.anonymizationtool.filestorage.TemporaryImageList;
import com.github.gserej.anonymizationtool.model.RectangleBox;
import com.github.gserej.anonymizationtool.model.RectangleBoxLists;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.fontbox.util.BoundingBox;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType3Font;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.apache.pdfbox.util.Matrix;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class WordsPrintingServiceImpl extends PDFTextStripper implements WordsPrintingService {
    private static final int SCALE = 8;

    private static String filename;
    private static PDDocument document;
    private AffineTransform flipAT;
    private Graphics2D g2d;
    private static Path rootLocation;


    @Setter
    @Getter
    private static int pageNumber;

    @Getter
    @Setter
    private static boolean readyToDraw;

    @Autowired
    public WordsPrintingServiceImpl(StorageProperties properties) throws IOException {
        rootLocation = Paths.get(properties.getLocation());
    }

    private WordsPrintingServiceImpl(PDDocument document, String filename) throws IOException {
        WordsPrintingServiceImpl.document = document;
        WordsPrintingServiceImpl.filename = filename;
    }

    private static void setUpStripper(File file, PDDocument document) throws IOException {
        WordsPrintingServiceImpl stripper = new WordsPrintingServiceImpl(document, file.getName());
        stripper.setSortByPosition(true);
        for (int page = 0; page < document.getNumberOfPages(); ++page) {
            setPageNumber(page + 1);
            stripper.stripPage(page);
        }
    }

    @Override
    public void getWordsLocations(File file) throws IOException {
        try (PDDocument document = PDDocument.load(file)) {
            setReadyToDraw(false);
            setUpStripper(file, document);
        } catch (NullPointerException e) {
            log.error("Null pointer exception loading PDF file " + e);
            e.printStackTrace();
        }
    }

    @Override
    public void drawBoxesAroundMarkedWords(File file) throws IOException {
        try (PDDocument document = PDDocument.load(file)) {
            setReadyToDraw(true);
            setUpStripper(file, document);

        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void stripPage(int page) throws IOException {
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        BufferedImage image = pdfRenderer.renderImage(page, SCALE);

        PDPage pdPage = document.getPage(page);

        flipAT = new AffineTransform();
        flipAT.translate(0, pdPage.getBBox().getHeight());
        flipAT.scale(1, -1);
        g2d = image.createGraphics();
        g2d.setStroke(new BasicStroke(0.1f));
        g2d.scale(SCALE, SCALE);

        setStartPage(page + 1);
        setEndPage(page + 1);

        Writer dummy = new OutputStreamWriter(new ByteArrayOutputStream());
        writeText(document, dummy);

        if (readyToDraw) {
            drawWordImage(page + 1);
            String imageFilename = filename;
            int pt = imageFilename.lastIndexOf('.');
            imageFilename = imageFilename.substring(0, pt) + "-marked-" + (page + 1) + ".png";
            if (!new File(rootLocation + "/tempImages/" + imageFilename).mkdirs())
                log.error("couldn't create /tempImages/ folder");
            File file = new File(rootLocation + "/tempImages/" + imageFilename);
            ImageIO.write(image, "png", file);
            TemporaryImageList.tempImagesList.add(file.getName());

        }
        g2d.dispose();
    }

    private void drawWordImage(int page) {
        Matrix matrix = new Matrix();
        AffineTransform at = matrix.createAffineTransform();
        for (int i = 0; i < RectangleBoxLists.rectangleBoxListMarked.size(); i++) {
            if (RectangleBoxLists.rectangleBoxListMarked.get(i).getPage() == page) {
                Rectangle2D.Float rect = new Rectangle2D.Float(RectangleBoxLists.rectangleBoxListMarked.get(i).getX(),
                        RectangleBoxLists.rectangleBoxListMarked.get(i).getY(),
                        RectangleBoxLists.rectangleBoxListMarked.get(i).getW(),
                        RectangleBoxLists.rectangleBoxListMarked.get(i).getH());

                Shape s = at.createTransformedShape(rect);
                g2d.setColor(Color.black);
                g2d.fill(s);
            }
        }
    }

    @Override
    protected void writeString(String string, List<TextPosition> textPositions) throws IOException {
        String wordSeparator = getWordSeparator();
        List<TextPosition> word = new ArrayList<>();

        for (TextPosition text : textPositions) {
            String thisChar = text.getUnicode();
            if (thisChar != null && thisChar.length() >= 1) {
                if (!thisChar.equals(wordSeparator)) {
                    word.add(text);
                } else if (!word.isEmpty()) {
                    printWord(word, getPageNumber());
                    word.clear();
                }
            }
        }
        if (!word.isEmpty()) {
            printWord(word, getPageNumber());
            word.clear();
        }
    }

    private void printWord(List<TextPosition> word, int page) throws IOException {
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

        Rectangle2D.Float rect = new Rectangle2D.Float(0, bbox.getLowerLeftY() + bbox.getHeight() * 0.05f,
                xadvance, bbox.getHeight() * 0.85f);
        if (font instanceof PDType3Font) {
            // bbox and font matrix are unscaled
            at.concatenate(font.getFontMatrix().createAffineTransform());
        } else {
            // bbox and font matrix are already scaled to 1000
            at.scale(1 / 1000f, 1 / 1000f);
        }
        Shape s = at.createTransformedShape(rect);
        s = flipAT.createTransformedShape(s);

        RectangleBox rectangleBox = new RectangleBox(false,
                false,
                (float) s.getBounds2D().getX(),
                (float) s.getBounds2D().getY(),
                (float) s.getBounds2D().getWidth(),
                (float) s.getBounds2D().getHeight(),
                1, singleWord, page);

        RectangleBoxLists.rectangleBoxListOriginal.add(rectangleBox);

    }
}