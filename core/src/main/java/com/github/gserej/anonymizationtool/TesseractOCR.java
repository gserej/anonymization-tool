package com.github.gserej.anonymizationtool;

import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.Word;
import org.springframework.util.ResourceUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

@Slf4j
class TesseractOCR {

    static boolean imageFileOCR(File imageFile) {

        ITesseract instance = new Tesseract();  // JNA Interface Mapping
        // ITesseract instance = new Tesseract1(); // JNA Direct Mapping
        try {
            String resourcePath = ResourceUtils.getFile("classpath:tessdata").getAbsolutePath();
            log.info(resourcePath);
            instance.setDatapath(resourcePath); // path to tessdata directory
            log.info("Success");
        } catch (FileNotFoundException e) {
            log.error(e.getMessage());
            return false;
        }
        BufferedImage bi;

        try {
            bi = ImageIO.read(imageFile);

            int level = ITessAPI.TessPageIteratorLevel.RIL_WORD;
            List<Word> wordList = instance.getWords(bi, level);

            for (Word word : wordList) {
//                log.info(word.getText());
//                log.info(word.getBoundingBox().toString());

                RectangleBox rectangleBox = new RectangleBox(false,
                        (float) word.getBoundingBox().getX(),
                        (float) word.getBoundingBox().getY(),
                        (float) word.getBoundingBox().getWidth(),
                        (float) word.getBoundingBox().getHeight(),
                        1);
                RectangleBoxList.rectangleBoxList.add(rectangleBox);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }
}