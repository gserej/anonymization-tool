package com.github.gserej.anonymizationtool;
import com.github.gserej.anonymizationtool.storage.StorageProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@Service
class CreatePdfFromImage {

    private static Path rootLocation;

    @Autowired
    public CreatePdfFromImage(StorageProperties properties) {
        rootLocation = Paths.get(properties.getLocation());
    }

    // creates a Pdf file from a image
    static String createPdfFromSingleImage(File imageFile, String fileName) throws IOException {
        new File(rootLocation + "/tempPdfLocation").mkdirs();

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
                TesseractOCR.setRatio(ratio);
                contents.drawImage(pdImage, page.getCropBox().getWidth() - ratio * width, page.getCropBox().getHeight() - ratio * height, ratio * width, ratio * height);
            }
            String pdfPath = rootLocation + "/tempPdfLocation/" + FilenameUtils.removeExtension(fileName) + ".pdf";
            doc.save(pdfPath);
            return pdfPath;
        }
    }

    static String createPdfFromMultipleImages(List<File> imageFiles, String fileName, File originalDocument) throws IOException {
        new File(rootLocation + "/processedPdf").mkdirs();

        try (PDDocument doc = PDDocument.load(originalDocument)) {

            int i = 0;
            for (File imageFile : imageFiles) {
                PDPage page = doc.getPage(i);
                PDImageXObject pdImage = PDImageXObject.createFromFile(imageFile.toString(), doc);
                try (PDPageContentStream contents = new PDPageContentStream(doc, page)) {
                    contents.drawImage(pdImage, 0, 0, page.getCropBox().getWidth(), page.getCropBox().getHeight());
                }
                i++;
            }
            String pdfPath = rootLocation + "/processedPdf/" + FilenameUtils.removeExtension(fileName) + ".pdf";
            doc.save(pdfPath);
            return pdfPath;
        }
    }

}
