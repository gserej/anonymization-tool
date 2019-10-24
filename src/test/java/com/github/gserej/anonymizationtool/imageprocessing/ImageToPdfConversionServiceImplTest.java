package com.github.gserej.anonymizationtool.imageprocessing;

import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

class ImageToPdfConversionServiceImplTest {


    @Test
    void createPdfFromSingleImage() throws IOException {

        String resourceName = "images/loren.PNG";
        ClassLoader classLoader = getClass().getClassLoader();
        File imageFile = new File(classLoader.getResource(resourceName).getFile());

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            PDImageXObject pdImage = PDImageXObject.createFromFile(imageFile.toString(), doc);
            try (PDPageContentStream contents = new PDPageContentStream(doc, page)) {
                float width = pdImage.getWidth();
                float height = pdImage.getHeight();
                float widthRatio = page.getCropBox().getWidth() / width;
                float heightRatio = page.getCropBox().getHeight() / height;
                float ratio = Math.min(widthRatio, heightRatio);
                contents.drawImage(pdImage,
                        page.getCropBox().getWidth() - ratio * width,
                        page.getCropBox().getHeight() - ratio * height,
                        ratio * width,
                        ratio * height);
            }
            String pdfPath = FilenameUtils.removeExtension(imageFile.getName()) + ".pdf";
//            doc.save(pdfPath);


        }
    }

    @Test
    void createPdfFromMultipleImages() {
    }
}