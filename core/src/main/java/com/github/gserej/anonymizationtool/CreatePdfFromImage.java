package com.github.gserej.anonymizationtool;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.File;
import java.io.IOException;

@Slf4j
class CreatePdfFromImage {

    static String createPdfFromImage(File imageFile, String fileName) throws IOException {

        new File("temp-storage").mkdir();
        String pdfPath = "temp-storage/" + FilenameUtils.removeExtension(fileName) + ".pdf";
        log.info(fileName);
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);

            // createFromFile is the easiest way with an image file
            // if you already have the image in a BufferedImage,
            // call LosslessFactory.createFromImage() instead
            PDImageXObject pdImage = PDImageXObject.createFromFile(imageFile.toString(), doc);

            // draw the image at full size at (x=20, y=20)
            try (PDPageContentStream contents = new PDPageContentStream(doc, page)) {
                float w;
                float h;
                if (page.getCropBox().getWidth() < pdImage.getWidth())
                    w = page.getCropBox().getWidth();
                else {
                    w = pdImage.getWidth();
                }

                if (page.getCropBox().getHeight() < pdImage.getHeight())
                    h = page.getCropBox().getHeight();
                else {
                    h = pdImage.getHeight();
                }

                contents.drawImage(pdImage, 0, 0, w, h);
            }

            doc.save(pdfPath);

            return pdfPath;
        }
    }

}
