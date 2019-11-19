package com.github.gserej.anonymizationtool.imageprocessing;

import com.github.gserej.anonymizationtool.filestorage.DocumentMetaInfo;
import com.github.gserej.anonymizationtool.filestorage.StorageProperties;
import com.github.gserej.anonymizationtool.imageprocessing.model.Ratio;
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
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ImageToPdfConversionServiceImpl implements ImageToPdfConversionService {

    private final Path rootLocation;
    private DocumentMetaInfo documentMetaInfo;


    @Autowired
    public ImageToPdfConversionServiceImpl(StorageProperties properties, DocumentMetaInfo documentMetaInfo) {
        rootLocation = Paths.get(properties.getLocation());
        this.documentMetaInfo = documentMetaInfo;
    }

    @Override
    public File createPdfFromSingleImage(File imageFile, String fileName) throws IOException {

        if (!new File(rootLocation + "/tempPdfLocation").mkdirs())
            log.info("A new temporary folder hasn't been created.");

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
                new Ratio(ratio);
                contents.drawImage(pdImage,
                        page.getCropBox().getWidth() - ratio * width,
                        page.getCropBox().getHeight() - ratio * height,
                        ratio * width,
                        ratio * height);
            }
            String pdfPath = rootLocation + "/tempPdfLocation/" + FilenameUtils.removeExtension(fileName) + ".pdf";
            doc.save(pdfPath);
            return new File(pdfPath);
        }
    }

    @Override
    public String createPdfFromMultipleImages(String fileName, File originalDocument) throws IOException {

        if (!new File(rootLocation + "/processedPdf").mkdirs())
            log.info("A new temporary folder hasn't been created.");

        try (PDDocument doc = PDDocument.load(originalDocument)) {

            List<String> imagesNames = documentMetaInfo.getImageList();
            List<File> imageFiles = new ArrayList<>();

            for (String imageName : imagesNames) {

                File file = new File(rootLocation + "/tempImages/" + imageName);
                imageFiles.add(file);
            }

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
