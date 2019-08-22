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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.GenericValidator;
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

@Slf4j
public class PrintDrawLocations extends PDFTextStripper {
    private static final int SCALE = 5;
    private final String filename;
    private final PDDocument document;
    private BufferedImage image;
    private AffineTransform flipAT;
    private AffineTransform rotateAT;
    private AffineTransform transAT;
    private Graphics2D g2d;


    public PrintDrawLocations(PDDocument document, String filename) throws IOException {
        this.document = document;
        this.filename = filename;
    }


    public static void PrintDrawLocation(File file) throws IOException {

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

        String imageFilename;
        imageFilename = "test.png";
        int pt = imageFilename.lastIndexOf('.');
        imageFilename = imageFilename.substring(0, pt) + "-marked-" + (page + 1) + ".png";

        ImageIO.write(image, "png", new File("markedFiles/" + imageFilename));
    }

    /**
     * Override the default functionality of PDFTextStripper.
     */
    @Override
    protected void writeString(String string, List<TextPosition> textPositions) throws IOException {
        String wordSeparator = getWordSeparator();
        List<TextPosition> word = new ArrayList<>();
        boolean ocrDone = FileUploadController.isOcrDone();

        if (ocrDone) {
            printWord(word, true);
        } else {
            for (TextPosition text : textPositions) {
                String thisChar = text.getUnicode();
                if (thisChar != null && thisChar.length() >= 1) {
                    if (!thisChar.equals(wordSeparator)) {
                        word.add(text);
                    } else if (!word.isEmpty()) {
                        printWord(word, false);
                        word.clear();
                    }
                }
            }
            if (!word.isEmpty()) {
                printWord(word, false);
                word.clear();
            }
        }
    }

    private void printWord(List<TextPosition> word, boolean imgType) throws IOException {
        if (!imgType) {
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
//        System.out.println(builder.toString());
            if (DataTypeValidators.isValidNIP(singleWord) ||
                    DataTypeValidators.isValidPesel(singleWord) ||
                    DataTypeValidators.isValidREGON(singleWord) ||
                    GenericValidator.isDate(singleWord, null) ||
                    singleWord.equals("PX031608")) {

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

                g2d.setColor(Color.blue);
//        System.out.println(s.getBounds2D());
                RectangleBox rectangleBox = new RectangleBox(false, (float) s.getBounds2D().getX(),
                        (float) s.getBounds2D().getY(),
                        (float) s.getBounds2D().getWidth(),
                        (float) s.getBounds2D().getHeight(),
                        1,
                        singleWord);


                RectangleBoxList.rectangleBoxList.add(rectangleBox);
                g2d.draw(s);
            }
        } else {
            TextPosition text = word.get(0);
            String singleWord = "";
            for (int i = 0; i < RectangleBoxList.rectangleBoxList.size(); i++) {
                singleWord = RectangleBoxList.rectangleBoxList.get(i).getWord();

                if (true) {


                    AffineTransform at = text.getTextMatrix().createAffineTransform();

                    Rectangle2D.Float rect = new Rectangle2D.Float(RectangleBoxList.rectangleBoxList.get(i).getX(),
                            RectangleBoxList.rectangleBoxList.get(i).getY(),
                            RectangleBoxList.rectangleBoxList.get(i).getW(),
                            RectangleBoxList.rectangleBoxList.get(i).getH());
                    Shape s = at.createTransformedShape(rect);
                    s = flipAT.createTransformedShape(s);
                    s = rotateAT.createTransformedShape(s);

                    g2d.setColor(Color.blue);
                    log.info(s.toString());
                    g2d.draw(s);

                }
            }
        }
    }
}