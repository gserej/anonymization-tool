package com.github.gserej.anonymizationtool.PDFBoxExamples;

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

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
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

/**
 * This is an example on how to get some x/y coordinates of text and to show them in a rendered
 * image.
 *
 * @author Ben Litchfield
 * @author Tilman Hausherr
 */
public class DrawTest extends PDFTextStripper {
    private BufferedImage image;
    private AffineTransform flipAT;
    private AffineTransform rotateAT;
    private AffineTransform transAT;
    private final String filename;
    static final int SCALE = 4;
    private Graphics2D g2d;
    private final PDDocument document;

    /**
     * Instantiate a new PDFTextStripper object.
     *
     * @param document
     * @param filename
     * @throws IOException If there is an error loading the properties.
     */
    public DrawTest(PDDocument document, String filename) throws IOException {
        this.document = document;
        this.filename = filename;
    }

    /**
     * This will print the documents data.
     *
     * @param args The command line arguments.
     * @throws IOException If there is an error parsing the document.
     */
    public static void main(String[] args) throws IOException {


        try (PDDocument document = PDDocument.load(new File(
                "C:\\Users\\Grzesiek\\Desktop\\javaprograms\\anonymization-tool\\console\\src\\main\\resources\\pdf-sample.pdf"))) {
            DrawTest stripper = new DrawTest(document,
                    "C:\\Users\\Grzesiek\\Desktop\\javaprograms\\anonymization-tool\\console\\src\\main\\resources\\pdf-sample.pdf");
            stripper.setSortByPosition(true);

            for (int page = 0; page < document.getNumberOfPages(); ++page) {
                stripper.stripPage(page);
            }
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


        String imageFilename = filename;
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
            if (thisChar != null) {
                if (thisChar.length() >= 1) {
                    if (!thisChar.equals(wordSeparator)) {
                        word.add(text);
                    } else if (!word.isEmpty()) {
                        printWord(word);
                        word.clear();
                    }
                }
            }

//            System.out.println("String[" + text.getXDirAdj() + ","
//                    + text.getYDirAdj() + "]" + text.getUnicode());

            // glyph space -> user space
            // note: text.getTextMatrix() is *not* the Text Matrix, it's the Text Rendering Matrix
            //  AffineTransform at = text.getTextMatrix().createAffineTransform();

            // in red:
            // show rectangles with the "height" (not a real height, but used for text extraction
            // heuristics, it is 1/2 of the bounding box height and starts at y=0)

//            Rectangle2D.Float rect = new Rectangle2D.Float(0, 0,
//                    text.getWidthDirAdj() / text.getTextMatrix().getScalingFactorX(),
//                    text.getHeightDir() / text.getTextMatrix().getScalingFactorY());
//            Shape s = at.createTransformedShape(rect);
//            s = flipAT.createTransformedShape(s);
//            s = rotateAT.createTransformedShape(s);
//            g2d.setColor(Color.red);
//            g2d.draw(s);


        }
        if (!word.isEmpty()) {
            printWord(word);
            word.clear();
        }
    }


    private void printWord(List<TextPosition> word) {
        Rectangle2D boundingBox = null;
        StringBuilder builder = new StringBuilder();

        for (TextPosition text : word) {
            Rectangle2D.Float box = new Rectangle2D.Float(text.getXDirAdj(), text.getYDirAdj(),
                    text.getWidthDirAdj() / text.getTextMatrix().getScalingFactorX(),
                    text.getHeightDir() / text.getTextMatrix().getScalingFactorY());


            if (boundingBox == null) {
                boundingBox = box;
            } else {
                boundingBox.add(box);
            }
            builder.append(text.getUnicode());

        }
        System.out.println(builder.toString() + " [(X=" + boundingBox.getX() + ",Y=" + boundingBox.getY()
                + "]");

        Rectangle2D rect = new Rectangle2D.Float((float) boundingBox.getX(), (float) boundingBox.getY(),
                (float) boundingBox.getWidth(), (float) boundingBox.getHeight());

//        AffineTransform at = text.getTextMatrix().createAffineTransform();
//        Shape s = at.createTransformedShape(rect);
//        Shape s = flipAT.createTransformedShape(rect);
//        s = rotateAT.createTransformedShape(s);
        g2d.setColor(Color.red);
        g2d.draw(rect);
    }

    /**
     * This will print the usage for this document.
     */
    private static void usage() {
        System.err.println("Usage: java " + DrawTest.class.getName() + " <input-pdf>");
    }
}
