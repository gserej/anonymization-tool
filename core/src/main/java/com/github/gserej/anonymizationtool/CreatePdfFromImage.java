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

    // creates a pdf file from a image
    static String createPdfFromSingleImage(File imageFile, String fileName) throws IOException {
        new File(rootLocation + "/tempPdfLocation").mkdirs();
        String pdfPath = rootLocation + "/tempPdfLocation/" + FilenameUtils.removeExtension(fileName) + ".pdf";
        try (PDDocument doc = new PDDocument()) {
            createPdfPage(doc, imageFile);
            doc.save(pdfPath);
            return pdfPath;
        }
    }

    static String createPdfFromMultipleImages(List<File> imageFiles, String fileName) throws IOException {
        new File(rootLocation + "/processedPdf").mkdirs();

        try (PDDocument doc = new PDDocument()) {
            for (File imageFile : imageFiles) {
                createPdfPage(doc, imageFile);

            }
            String pdfPath = rootLocation + "/processedPdf/" + FilenameUtils.removeExtension(fileName) + ".pdf";
            doc.save(pdfPath);
            return pdfPath;
        }
    }

    private static void createPdfPage(PDDocument doc, File imageFile) throws IOException {
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
    }
}
