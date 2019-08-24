package com.github.gserej.anonymizationtool;

import com.github.gserej.anonymizationtool.storage.StorageProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
class CreatePdfFromImage {

    private static Path rootLocation;

    public CreatePdfFromImage(StorageProperties properties) {
        rootLocation = Paths.get(properties.getLocation());
    }

    static String createPdfFromImage(File imageFile, String fileName) throws IOException {
        new File(rootLocation + "/tmp").mkdir();
        String pdfPath = rootLocation + "/tmp/" + FilenameUtils.removeExtension(fileName) + ".pdf";
//        log.info(fileName);
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);

            // createFromFile is the easiest way with an image file
            // if you already have the image in a BufferedImage,
            // call LosslessFactory.createFromImage() instead
            PDImageXObject pdImage = PDImageXObject.createFromFile(imageFile.toString(), doc);

            try (PDPageContentStream contents = new PDPageContentStream(doc, page)) {
                float width = pdImage.getWidth();
                float height = pdImage.getHeight();
                float widthRatio = page.getCropBox().getWidth() / width;
                float heightRatio = page.getCropBox().getHeight() / height;
                float ratio = Math.min(widthRatio, heightRatio);
                TesseractOCR.setRatio(ratio);
                contents.drawImage(pdImage, page.getCropBox().getWidth() - ratio * width, page.getCropBox().getHeight() - ratio * height, ratio * width, ratio * height);
            }

            doc.save(pdfPath);

            return pdfPath;
        }
    }

}
