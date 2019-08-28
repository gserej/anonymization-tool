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
package com.github.gserej.anonymizationtool;

import com.github.gserej.anonymizationtool.storage.StorageService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.fontbox.util.BoundingBox;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType3Font;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.apache.pdfbox.util.Matrix;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class PrintDrawLocations extends PDFTextStripper {
    private static final int SCALE = 5;

    private static List<String> tempImagesList = new ArrayList<>();
    private final String filename;

    private final PDDocument document;
    private AffineTransform flipAT;
    private AffineTransform rotateAT;
    private Graphics2D g2d;

    @Setter
    private static String rootLocation;

    @Setter
    private static StorageService storageService;

    @Setter
    @Getter
    private static int pageNumber;

    @Getter
    @Setter
    private boolean readyToDraw;


    private PrintDrawLocations(PDDocument document, String filename, boolean readyToDraw) throws IOException {
        this.document = document;
        this.filename = filename;
        this.readyToDraw = readyToDraw;
    }


    static void PrintDrawLocation(File file, boolean readyToDraw) throws IOException {

        try (PDDocument document = PDDocument.load(file)) {

            PrintDrawLocations stripper = new PrintDrawLocations(document, file.getName(), readyToDraw);
            stripper.setSortByPosition(true);
            for (int page = 0; page < document.getNumberOfPages(); ++page) {
                setPageNumber(page + 1);
                stripper.stripPage(page);
            }
            FileUploadController.setTempImagesList(tempImagesList);

        } catch (NullPointerException e) {
            e.printStackTrace();
        }


    }

    private void stripPage(int page) throws IOException {
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        BufferedImage image = pdfRenderer.renderImage(page, SCALE);

        PDPage pdPage = document.getPage(page);

        // flip y-axis
        flipAT = new AffineTransform();
        flipAT.translate(0, pdPage.getBBox().getHeight());
        flipAT.scale(1, -1);

        // page may be rotated
        rotateAT = new AffineTransform();
        int rotation = pdPage.getRotation();
        if (rotation != 0) {
            PDRectangle mediaBox = pdPage.getMediaBox();
            switch (rotation) {
                case 90:
                    rotateAT.translate(mediaBox.getHeight(), 0);
                    break;
                case 270:
                    rotateAT.translate(0, mediaBox.getWidth());
                    break;
                case 180:
                    rotateAT.translate(mediaBox.getWidth(), mediaBox.getHeight());
                    break;
                default:
                    break;
            }
            rotateAT.rotate(Math.toRadians(rotation));
        }


        g2d = image.createGraphics();
        g2d.setStroke(new BasicStroke(0.1f));
        g2d.scale(SCALE, SCALE);

        setStartPage(page + 1);
        setEndPage(page + 1);

        Writer dummy = new OutputStreamWriter(new ByteArrayOutputStream());
        writeText(document, dummy);

        if (readyToDraw) {
            drawWordImage();
        }

        g2d.dispose();

        String imageFilename = filename;
        int pt = imageFilename.lastIndexOf('.');
        imageFilename = imageFilename.substring(0, pt) + "-marked-" + (page + 1) + ".png";

        if (readyToDraw) {
            new File("/tmpImages/" + imageFilename).mkdirs();
            File file = new File("/tmpImages/" + imageFilename);
            ImageIO.write(image, "png", file);

            tempImagesList.add(file.getName());

            storageService.storeAsFile(file);

        }
    }

    private void drawWordImage() {
        Matrix matrix = new Matrix();
        AffineTransform at = matrix.createAffineTransform();
        for (int i = 0; i < RectangleBoxLists.rectangleBoxListMarked.size(); i++) {
            Rectangle2D.Float rect = new Rectangle2D.Float(RectangleBoxLists.rectangleBoxListMarked.get(i).getX(),
                    RectangleBoxLists.rectangleBoxListMarked.get(i).getY(),
                    RectangleBoxLists.rectangleBoxListMarked.get(i).getW(),
                    RectangleBoxLists.rectangleBoxListMarked.get(i).getH());


            Shape s = at.createTransformedShape(rect);
            s = rotateAT.createTransformedShape(s);
            g2d.setColor(Color.black);
            g2d.fill(s);

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


        // in blue:
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
        s = rotateAT.createTransformedShape(s);


        RectangleBox rectangleBox = new RectangleBox(false, (float) s.getBounds2D().getX(),
                (float) s.getBounds2D().getY(),
                (float) s.getBounds2D().getWidth(),
                (float) s.getBounds2D().getHeight(),
                1, singleWord, page);


        RectangleBoxLists.rectangleBoxList.add(rectangleBox);

    }
}