package com.github.gserej.anonymizationtool.Tess4JExample;

import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

public class TesseractExample {

    private static final Logger log = LoggerFactory.getLogger(TesseractExample.class);

    public static void main(String[] args) {
        File imageFile = new File("C:\\Users\\Grzesiek\\Desktop\\javaprograms\\anonymization-tool\\console\\src\\main\\resources\\scanned.png");
        ITesseract instance = new Tesseract();  // JNA Interface Mapping
        // ITesseract instance = new Tesseract1(); // JNA Direct Mapping
        instance.setDatapath("C:\\Users\\Grzesiek\\Desktop\\javaprograms\\anonymization-tool\\console\\src\\main\\resources\\tessdata"); // path to tessdata directory

        try {
            String result = instance.doOCR(imageFile);
            //  System.out.println(result);
        } catch (TesseractException e) {
            System.err.println(e.getMessage());
        }


        try {
            BufferedImage bi = ImageIO.read(imageFile);
            int level = ITessAPI.TessPageIteratorLevel.RIL_SYMBOL;

            List<Rectangle> result = instance.getSegmentedRegions(bi, level);
            for (int i = 0; i < result.size(); i++) {
                Rectangle rect = result.get(i);
                log.info(String.format("Box[%d]: x=%d, y=%d, w=%d, h=%d", i, rect.x, rect.y, rect.width, rect.height));
            }
        } catch (Exception e) {

        }
    }
}