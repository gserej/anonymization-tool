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
package com.github.gserej.anonymizationtool.rectangles;

import com.github.gserej.anonymizationtool.document.DocumentRepository;
import com.github.gserej.anonymizationtool.filestorage.StorageProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
public class WordsDrawingServiceImpl implements WordsDrawingService {

    private static Path rootLocation;
    private final DocumentRepository documentRepository;

    @Autowired
    public WordsDrawingServiceImpl(StorageProperties properties, DocumentRepository documentRepository) {
        rootLocation = Paths.get(properties.getLocation());
        this.documentRepository = documentRepository;
    }

    @Override
    public void drawBoxesAroundMarkedWords(File file, UUID uuid) throws IOException {
        try (PDDocument pdDocument = PDDocument.load(file)) {
            WordsDrawingTextStripper wordsDrawingTextStripper = new WordsDrawingTextStripper(rootLocation, documentRepository);
            wordsDrawingTextStripper.setSortByPosition(true);
            for (int page = 0; page < pdDocument.getNumberOfPages(); ++page) {
                wordsDrawingTextStripper.stripPage(page, pdDocument, uuid);
            }
        } catch (NullPointerException e) {
            log.error("Null pointer exception loading PDF file " + e);
        }
    }

}

