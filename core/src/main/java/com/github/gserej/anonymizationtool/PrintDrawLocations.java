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

import org.apache.fontbox.util.BoundingBox;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType3Font;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PrintDrawLocations extends PDFTextStripper {
    private BufferedImage image;
    private AffineTransform flipAT;
    private AffineTransform rotateAT;
    private AffineTransform transAT;
    private final String filename;
    static final int SCALE = 4;
    private Graphics2D g2d;
    private final PDDocument document;

    public PrintDrawLocations(PDDocument document, String filename) throws IOException {
        this.document = document;
        this.filename = filename;
    }

    public static void PrintDrawLocation(File file) throws IOException {

        ClassLoader classLoader = ClassLoader.getSystemClassLoader();

        try (
                PDDocument document = PDDocument.load(file)
        ) {
            PrintDrawLocations stripper = new PrintDrawLocations(document, file.getName());
            stripper.setSortByPosition(true);
            for (int page = 0; page < document.getNumberOfPages(); ++page) {
                stripper.stripPage(page);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

    }

    private void stripPage(int page) throws IOException {
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        image = pdfRenderer.renderImage(page, SCALE);

        PDPage pdPage = document.getPage(page);
        PDRectangle cropBox = pdPage.getCropBox();

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

        // cropbox
        transAT = AffineTransform.getTranslateInstance(-cropBox.getLowerLeftX(), cropBox.getLowerLeftY());

        g2d = image.createGraphics();
        g2d.setStroke(new BasicStroke(0.1f));
        g2d.scale(SCALE, SCALE);

        setStartPage(page + 1);
        setEndPage(page + 1);

        Writer dummy = new OutputStreamWriter(new ByteArrayOutputStream());
        writeText(document, dummy);

        g2d.dispose();

        String imageFilename = filename;
        imageFilename = "test.png";
        int pt = imageFilename.lastIndexOf('.');
        imageFilename = imageFilename.substring(0, pt) + "-marked-" + (page + 1) + ".png";

        ImageIO.write(image, "png", new File(imageFilename));
    }

    /**
     * Override the default functionality of PDFTextStripper.
     */
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
                    printWord(word);
                    word.clear();
                }
            }
        }
        if (!word.isEmpty()) {
            printWord(word);
            word.clear();
        }
    }


    private void printWord(List<TextPosition> word) throws IOException {
        StringBuilder builder = new StringBuilder();
        float rectWidth = 0.0f;

        TextPosition text = word.get(0);

        PDFont font = text.getFont();
        BoundingBox bbox = font.getBoundingBox();
        float xadvance = 0.0f;

        for (TextPosition letter : word) {
            builder.append(letter.getUnicode());
            rectWidth += letter.getWidthDirAdj();
            xadvance += font.getWidth(letter.getCharacterCodes()[0]);
        }
        // red
        AffineTransform at = text.getTextMatrix().createAffineTransform();
        Rectangle2D.Float rect = new Rectangle2D.Float(0, 0,
                rectWidth / text.getTextMatrix().getScalingFactorX(),
                text.getHeightDir() / text.getTextMatrix().getScalingFactorY());
        Shape s = at.createTransformedShape(rect);
        s = flipAT.createTransformedShape(s);
        s = rotateAT.createTransformedShape(s);
        g2d.setColor(Color.red);
//        g2d.draw(s);
        System.out.println(builder.toString());


        // in blue:
        rect = new Rectangle2D.Float(0, bbox.getLowerLeftY() + bbox.getHeight() * 0.05f, xadvance, bbox.getHeight() * 0.85f);
        if (font instanceof PDType3Font) {
            // bbox and font matrix are unscaled
            at.concatenate(font.getFontMatrix().createAffineTransform());
        } else {
            // bbox and font matrix are already scaled to 1000
            at.scale(1 / 1000f, 1 / 1000f);
        }
        s = at.createTransformedShape(rect);
        s = flipAT.createTransformedShape(s);
        s = rotateAT.createTransformedShape(s);

        g2d.setColor(Color.blue);
        System.out.println(s.getBounds2D());
        Rectangle rectangle = new Rectangle(false, (float) s.getBounds2D().getX(),
                (float) s.getBounds2D().getY(),
                (float) s.getBounds2D().getWidth(),
                (float) s.getBounds2D().getHeight());

        g2d.draw(s);

    }

}
