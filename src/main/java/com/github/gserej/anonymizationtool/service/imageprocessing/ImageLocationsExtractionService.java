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
 */
package com.github.gserej.anonymizationtool.service.imageprocessing;

import com.github.gserej.anonymizationtool.exceptions.ImageExtractionException;
import com.github.gserej.anonymizationtool.service.filestorage.StorageProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.contentstream.PDFStreamEngine;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
public class ImageLocationsExtractionService extends PDFStreamEngine {
    private static Path rootLocation;

    private final OCRService ocrService;

    @Autowired
    public ImageLocationsExtractionService(StorageProperties properties, OCRService ocrService) {
        rootLocation = Paths.get(properties.getLocation());
        this.ocrService = ocrService;
    }

    public void extractImages(File file, UUID uuid) throws ImageExtractionException {

        try (PDDocument document = PDDocument.load(file)) {
            ImageLocationsExtractionStreamEngine imageLocationsExtractionStreamEngine = new ImageLocationsExtractionStreamEngine(rootLocation, ocrService, uuid);
            imageLocationsExtractionStreamEngine.init();
            int pageNum = 1;
            //noinspection ResultOfMethodCallIgnored
            new File(rootLocation + "/" + uuid + "/extractedImages").mkdirs();
            for (PDPage page : document.getPages()) {
                imageLocationsExtractionStreamEngine.setPageNum(pageNum);
                imageLocationsExtractionStreamEngine.setPageHeight(page.getMediaBox().getHeight());
                imageLocationsExtractionStreamEngine.processPage(page);
                pageNum++;
            }
        } catch (IOException e) {
            throw new ImageExtractionException();
        }
    }
}
