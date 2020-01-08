package com.github.gserej.anonymizationtool.filestorage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;
import java.util.stream.Stream;

public interface StorageService {

    void init();

    void createUuidFolder(UUID uuid);

    void store(MultipartFile file, UUID uuid);

    void storeAsFile(File file, UUID uuid);

    Stream<Path> loadAll(UUID uuid);

    Path load(String filename, UUID uuid);

    Resource loadAsResource(String filename, UUID uuid);

    void deleteAll();

}
