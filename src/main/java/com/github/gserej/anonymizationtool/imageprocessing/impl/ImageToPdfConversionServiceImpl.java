package com.github.gserej.anonymizationtool.imageprocessing.impl;

import com.github.gserej.anonymizationtool.filestorage.Document;
import com.github.gserej.anonymizationtool.filestorage.DocumentRepository;
import com.github.gserej.anonymizationtool.filestorage.StorageProperties;
import com.github.gserej.anonymizationtool.imageprocessing.ImageToPdfConversionService;
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
import java.util.UUID;

@Slf4j
@Service
public class ImageToPdfConversionServiceImpl implements ImageToPdfConversionService {

    private final Path rootLocation;
    private DocumentRepository documentRepository;


    @Autowired
    public ImageToPdfConversionServiceImpl(StorageProperties properties, DocumentRepository documentRepository) {
        rootLocation = Paths.get(properties.getLocation());
        this.documentRepository = documentRepository;
    }

    @Override
    public File createPdfFromSingleImage(File imageFile, String fileName, UUID uuid) throws IOException {

        if (!new File(rootLocation + "/" + uuid + "/tempPdfLocation").mkdirs())
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

                Document document = new Document(uuid);
                document.setImageRatio(ratio);
                documentRepository.save(document);

                contents.drawImage(pdImage,
                        page.getCropBox().getWidth() - ratio * width,
                        page.getCropBox().getHeight() - ratio * height,
                        ratio * width,
                        ratio * height);
            }
            String pdfPath = rootLocation + "/" + uuid + "/tempPdfLocation/" + FilenameUtils.removeExtension(fileName) + ".pdf";
            doc.save(pdfPath);
            return new File(pdfPath);
        }
    }

    @Override
    public String createPdfFromMultipleImages(String fileName, File originalDocument, UUID uuid) throws IOException {

        if (!new File(rootLocation + "" + uuid + "/processedPdf").mkdirs())
            log.info("A new temporary folder hasn't been created.");

        try (PDDocument doc = PDDocument.load(originalDocument)) {

            Document document = documentRepository.findById(uuid).orElseThrow();

            List<String> imagesNames = document.getImageList();
            List<File> imageFiles = new ArrayList<>();

            for (String imageName : imagesNames) {

                File file = new File(rootLocation + "" + uuid + "/tempImages/" + imageName);
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
            String pdfPath = rootLocation + "" + uuid + "/processedPdf/" + FilenameUtils.removeExtension(fileName) + ".pdf";
            doc.save(pdfPath);
            return pdfPath;
        }
    }

}
