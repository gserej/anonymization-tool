package com.github.gserej.anonymizationtool;

import net.sourceforge.tess4j.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

class TesseractOCR {

    private static final Logger log = LoggerFactory.getLogger(TesseractOCR.class);

    static void imageFileOCR(File imageFile) {

        ITesseract instance = new Tesseract();  // JNA Interface Mapping
        // ITesseract instance = new Tesseract1(); // JNA Direct Mapping

        try {
            String resourcePath = ResourceUtils.getFile("classpath:tessdata").getAbsolutePath();
            System.out.println(resourcePath);
            instance.setDatapath(resourcePath); // path to tessdata directory
            System.out.println("Success");
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
        BufferedImage bi;

        try {
            bi = ImageIO.read(imageFile);

            int level = ITessAPI.TessPageIteratorLevel.RIL_WORD;
            List<Word> wordList = instance.getWords(bi, level);
//            List<RectangleBox> rectangleListFromOCR = new ArrayList<>();

            for (Word word : wordList) {
                log.info(word.getText());
                log.info(word.getBoundingBox().toString());
//                rectangleListFromOCR.add();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}