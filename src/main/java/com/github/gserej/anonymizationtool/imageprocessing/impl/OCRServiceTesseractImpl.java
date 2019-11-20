package com.github.gserej.anonymizationtool.imageprocessing.impl;

import com.github.gserej.anonymizationtool.imageprocessing.OCRService;
import com.github.gserej.anonymizationtool.imageprocessing.model.EmbeddedImageProperties;
import com.github.gserej.anonymizationtool.rectangles.RectangleBoxSets;
import com.github.gserej.anonymizationtool.rectangles.model.RectangleBox;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.Word;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class OCRServiceTesseractImpl implements OCRService {

    private RectangleBoxSets rectangleBoxSets;

    private final int level = ITessAPI.TessPageIteratorLevel.RIL_WORD;
    @Value("${tessdata.path}")
    String tessdataPathString;

    public OCRServiceTesseractImpl(RectangleBoxSets rectangleBoxSets) {
        this.rectangleBoxSets = rectangleBoxSets;
    }

    private ITesseract initOcr() {
        ITesseract instance = new Tesseract();
        instance.setTessVariable("user_defined_dpi", "70");
        try {
            String resourcePath = ResourceUtils.getFile(tessdataPathString).getAbsolutePath();
            instance.setDatapath(resourcePath);
        } catch (FileNotFoundException e) {
            log.error("Tessdata File not found: " + e.getMessage());
            return null;
        }
        return instance;
    }

    @Override
    public boolean doOcrOnSingleImageFile(File imageFile, float imageRatio) {

        ITesseract instance = initOcr();
        if (instance != null) {
            try {
                BufferedImage bi = ImageIO.read(imageFile);

                List<Word> wordList = instance.getWords(bi, level);

                for (Word word : wordList) {
                    RectangleBox rectangleBox = new RectangleBox(false,
                            false,
                            false,
                            imageRatio * (float) word.getBoundingBox().getX(),
                            imageRatio * (float) word.getBoundingBox().getY(),
                            imageRatio * (float) word.getBoundingBox().getWidth(),
                            imageRatio * (float) word.getBoundingBox().getHeight(),
                            1, word.getText(), 1);
                    rectangleBoxSets.addRectangle(rectangleBox);
                }
                return true;
            } catch (NullPointerException e) {
                log.error("NullPointerException. Couldn't find a words in image: " + e);
            } catch (IOException e) {
                log.error("IO Exception: " + e);
            }
        }
        return false;
    }

    @Override
    public void doOcrOnEmbeddedImageFiles(File imageFile, EmbeddedImageProperties embeddedImageProperties) {

        ITesseract instance = initOcr();

        float positionX = embeddedImageProperties.getPositionX();
        float positionY = embeddedImageProperties.getPositionY();
        float sizeX = embeddedImageProperties.getSizeX();
        float sizeY = embeddedImageProperties.getSizeY();
        int pageNum = embeddedImageProperties.getPageNumber();
        float pageHeight = embeddedImageProperties.getPageHeight();
        if (instance != null) {
            try {
                BufferedImage bi = ImageIO.read(imageFile);
                List<Word> wordList = instance.getWords(bi, level);
                for (Word word : wordList) {
                    RectangleBox rectangleBox = new RectangleBox(false,
                            false,
                            false,
                            positionX + (float) word.getBoundingBox().getX() * sizeX / bi.getWidth(),
                            -positionY + pageHeight - sizeY + (float) word.getBoundingBox().getY() * sizeY / bi.getHeight(),
                            (float) word.getBoundingBox().getWidth() * sizeX / bi.getWidth(),
                            (float) word.getBoundingBox().getHeight() * sizeY / bi.getHeight(),
                            1, word.getText(), Math.round(pageNum));
                    rectangleBoxSets.addRectangle(rectangleBox);
                }
            } catch (IOException e) {
                log.error("IO exception: " + e);
            }
        }
    }
}
