package com.github.gserej.anonymizationtool.services;

import com.github.gserej.anonymizationtool.model.RectangleBox;
import com.github.gserej.anonymizationtool.model.RectangleBoxLists;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.Word;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class OCRServiceTesseractImpl implements OCRService {

    private static ITesseract initOcr() {
        ITesseract instance = new Tesseract();  // JNA Interface Mapping
        // ITesseract instance = new Tesseract1(); // JNA Direct Mapping
        try {
            String resourcePath = ResourceUtils.getFile("classpath:tessdata").getAbsolutePath();
            instance.setDatapath(resourcePath); // path to tessdata directory

        } catch (FileNotFoundException e) {
            log.error("File not found: " + e.getMessage());
            return null;
        }
        return instance;
    }

    @Override
    public boolean doOcrOnSingleFile(File imageFile, float ratio) {
        ITesseract instance = initOcr();
        if (instance != null) {
            try {
                BufferedImage bi = ImageIO.read(imageFile);
                int level = ITessAPI.TessPageIteratorLevel.RIL_WORD;
                List<Word> wordList = instance.getWords(bi, level);

                for (Word word : wordList) {
                    RectangleBox rectangleBox = new RectangleBox(false,
                            ratio * (float) word.getBoundingBox().getX(),
                            ratio * (float) word.getBoundingBox().getY(),
                            ratio * (float) word.getBoundingBox().getWidth(),
                            ratio * (float) word.getBoundingBox().getHeight(),
                            1, word.getText(), 1);
                    RectangleBoxLists.rectangleBoxListOriginal.add(rectangleBox);
                }
                return true;
            } catch (IOException e) {
                log.error("Error: " + e);
            }
        }
        return false;
    }

    @Override
    public void doOcrOnMultipleFiles(File imageFile, Map imagePositionAndSize) {
        ITesseract instance = initOcr();

        float positionX = (float) imagePositionAndSize.get("Position X");
        float positionY = (float) imagePositionAndSize.get("Position Y");
        float sizeX = (float) imagePositionAndSize.get("Size X");
        float sizeY = (float) imagePositionAndSize.get("Size Y");
        float pageNum = (float) imagePositionAndSize.get("page");
        float pageHeight = (float) imagePositionAndSize.get("page Height");
        if (instance != null) {
            try {
                BufferedImage bi = ImageIO.read(imageFile);
                int level = ITessAPI.TessPageIteratorLevel.RIL_WORD;
                List<Word> wordList = instance.getWords(bi, level);

                for (Word word : wordList) {
                    RectangleBox rectangleBox = new RectangleBox(false,
                            positionX + (float) word.getBoundingBox().getX() * sizeX / bi.getWidth(),
                            -positionY + pageHeight - sizeY + (float) word.getBoundingBox().getY() * sizeY / bi.getHeight(),
                            (float) word.getBoundingBox().getWidth() * sizeX / bi.getWidth(),
                            (float) word.getBoundingBox().getHeight() * sizeY / bi.getHeight(),
                            1, word.getText(), Math.round(pageNum));
                    RectangleBoxLists.rectangleBoxListOriginal.add(rectangleBox);
                }
            } catch (IOException e) {
                log.error("Error: " + e);
            }
        }
    }
}