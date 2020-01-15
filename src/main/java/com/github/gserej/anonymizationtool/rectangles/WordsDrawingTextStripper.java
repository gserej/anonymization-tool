package com.github.gserej.anonymizationtool.rectangles;

import com.github.gserej.anonymizationtool.document.Document;
import com.github.gserej.anonymizationtool.document.DocumentRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.util.Matrix;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
class WordsDrawingTextStripper extends PDFTextStripper {

    private static final int SCALE = 8;
    private final Path rootLocation;
    private final DocumentRepository documentRepository;

    public WordsDrawingTextStripper(Path rootLocation, DocumentRepository documentRepository) throws IOException {
        this.rootLocation = rootLocation;
        this.documentRepository = documentRepository;
    }

    public void stripPage(int page, PDDocument document, UUID uuid) throws IOException {
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
        File temporaryFile = new File(rootLocation + "/" + uuid.toString() + "/tempImages/" + imageFilename);
        if (!temporaryFile.mkdirs())
            log.error("couldn't create /tempImages/ folder");
        ImageIO.write(image, "png", temporaryFile);
        List<String> imageList;
        if (documentInfo.getImageList() == null) {
            imageList = new ArrayList<>();
        } else {
            imageList = documentInfo.getImageList();
        }
        imageList.add(temporaryFile.getName());
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
