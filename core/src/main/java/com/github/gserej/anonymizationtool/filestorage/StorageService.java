package com.github.gserej.anonymizationtool.filestorage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;
import java.util.stream.Stream;

public interface StorageService {

    void init();

    void store(MultipartFile file);

    void storeAsFile(File file);

    Stream<Path> loadAll();

    Path load(String filename);

    Resource loadAsResource(String filename);

    File loadAsFile(String filename);

    void deleteAll();

}