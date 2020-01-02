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
package com.github.gserej.anonymizationtool.rectangles;

import com.github.gserej.anonymizationtool.filestorage.Document;
import com.github.gserej.anonymizationtool.filestorage.DocumentRepository;
import com.github.gserej.anonymizationtool.filestorage.StorageProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
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
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class WordsDrawingServiceImpl extends PDFTextStripper implements WordsDrawingService {
    private static final int SCALE = 8;
    private static Path rootLocation;
    private DocumentRepository documentRepository;

    @Autowired
    public WordsDrawingServiceImpl(StorageProperties properties, DocumentRepository documentRepository) throws IOException {
        rootLocation = Paths.get(properties.getLocation());
        this.documentRepository = documentRepository;
    }

    @Override
    public void drawBoxesAroundMarkedWords(File file, UUID uuid) throws IOException {
        try (PDDocument document = PDDocument.load(file)) {
            setSortByPosition(true);
            for (int page = 0; page < document.getNumberOfPages(); ++page) {
//                int pageNumber= (page + 1);
                stripPage(page, document, uuid);
            }
        } catch (NullPointerException e) {
            log.error("Null pointer exception loading PDF file " + e);
        }
    }

    private void stripPage(int page, PDDocument document, UUID uuid) throws IOException {
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        BufferedImage image = pdfRenderer.renderImage(page, SCALE);
        PDPage pdPage = document.getPage(page);

        AffineTransform flipAT = new AffineTransform();
        flipAT.translate(0, pdPage.getBBox().getHeight());
        flipAT.scale(1, -1);
        Graphics2D g2d = image.createGraphics();
        g2d.setStroke(new BasicStroke(0.1f));
        g2d.scale(SCALE, SCALE);

        setStartPage(page + 1);
        setEndPage(page + 1);

        Writer dummy = new OutputStreamWriter(new ByteArrayOutputStream());
        writeText(document, dummy);

        drawWordImage(page + 1, uuid, g2d);
        Document documentInfo = documentRepository.findById(uuid).orElseThrow();
        String imageFilename = documentInfo.getDocumentName();
        int pt = imageFilename.lastIndexOf('.');
        imageFilename = imageFilename.substring(0, pt) + "-marked-" + (page + 1) + ".png";
        if (!new File(rootLocation + "/tempImages/" + imageFilename).mkdirs())
            log.error("couldn't create /tempImages/ folder");
        File file = new File(rootLocation + "/tempImages/" + imageFilename);
        ImageIO.write(image, "png", file);

        List<String> imageList = documentInfo.getImageList();
        imageList.add(file.getName());
        documentInfo.setImageList(imageList);
        documentRepository.save(documentInfo);

        g2d.dispose();
    }

    private void drawWordImage(int page, UUID uuid, Graphics2D g2d) {
        Matrix matrix = new Matrix();
        AffineTransform at = matrix.createAffineTransform();
        Document document = documentRepository.findById(uuid).orElseThrow();
        Set<RectangleBox> rectangleBoxes = document.getMarkedRectangles();
        for (RectangleBox rectangleBox : rectangleBoxes) {
            if (rectangleBox.getPage() == page) {
                Rectangle2D.Float rect = new Rectangle2D.Float(
                        rectangleBox.getX(),
                        rectangleBox.getY(),
                        rectangleBox.getW(),
                        rectangleBox.getH());

                Shape s = at.createTransformedShape(rect);
                g2d.setColor(Color.black);
                g2d.fill(s);
            }
        }
    }
}
