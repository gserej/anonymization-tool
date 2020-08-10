/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Modifications copyright (C) 2020 gserej
 */
package com.github.gserej.anonymizationtool.service.imageprocessing;

import com.github.gserej.anonymizationtool.models.EmbeddedImageProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.contentstream.PDFStreamEngine;
import org.apache.pdfbox.contentstream.operator.DrawObject;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.operator.OperatorName;
import org.apache.pdfbox.contentstream.operator.state.*;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Slf4j
class ImageLocationsExtractionStreamEngine extends PDFStreamEngine {
    private static Path rootLocation;
    private final OCRService ocrService;
    private final UUID uuid;
    @Setter
    @Getter
    private int pageNum;
    @Setter
    @Getter
    private float pageHeight;

    public ImageLocationsExtractionStreamEngine(Path rootLocation, OCRService ocrService, UUID uuid) {
        ImageLocationsExtractionStreamEngine.rootLocation = rootLocation;
        this.ocrService = ocrService;
        this.uuid = uuid;
    }

    void init() {
        addOperator(new Concatenate());
        addOperator(new DrawObject());
        addOperator(new SetGraphicsStateParameters());
        addOperator(new Save());
        addOperator(new Restore());
        addOperator(new SetMatrix());
    }

    @Override
    protected void processOperator(Operator operator, List<COSBase> operands) throws IOException {
        String operation = operator.getName();
        if (OperatorName.DRAW_OBJECT.equals(operation)) {
            COSName objectName = (COSName) operands.get(0);
            PDXObject xobject = getResources().getXObject(objectName);
            if (xobject instanceof PDImageXObject) {
                int num = 0;
                File imgFile = new File(rootLocation + "/" + uuid + "/extractedImages/" + objectName.getName() + "-0" + ".png");

                while (imgFile.exists()) {
                    imgFile = new File(rootLocation + "/" + uuid + "/extractedImages/" + objectName.getName() + "-" + (num++) + ".png");
                }
                ImageIO.write(((PDImageXObject) xobject).getImage(), "png", imgFile);

                Matrix ctmNew = getGraphicsState().getCurrentTransformationMatrix();
                float imageXScale = ctmNew.getScalingFactorX();
                float imageYScale = ctmNew.getScalingFactorY();

                EmbeddedImageProperties embeddedImageProperties = new EmbeddedImageProperties(
                        ctmNew.getTranslateX(),
                        ctmNew.getTranslateY(),
                        imageXScale,
                        imageYScale,
                        getPageNum(),
                        getPageHeight());

                log.info("Starting OCR process on: " + imgFile.getName());
                ocrService.doOcrOnEmbeddedImageFiles(imgFile, embeddedImageProperties, uuid);

            } else if (xobject instanceof PDFormXObject) {
                PDFormXObject form = (PDFormXObject) xobject;
                showForm(form);
            }
        } else {
            super.processOperator(operator, operands);
        }
    }
}
