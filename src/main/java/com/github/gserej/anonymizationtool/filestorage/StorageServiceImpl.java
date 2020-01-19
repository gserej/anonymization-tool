package com.github.gserej.anonymizationtool.filestorage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class StorageServiceImpl implements StorageService {

    private final Path rootLocation;

    @Autowired
    public StorageServiceImpl(StorageProperties properties) {
        this.rootLocation = Paths.get(properties.getLocation());
    }

    @Override
    public void store(MultipartFile file, UUID uuid) throws StorageCannotSaveFileException {
        String filename = file.getOriginalFilename();

        if (filename != null) {
            filename = StringUtils.cleanPath(filename);
        } else {
            throw new StorageCannotSaveFileException("Cannot get filename from file!");
        }

        if (file.isEmpty()) {
            throw new StorageCannotSaveFileException("Failed to store empty file " + filename);
        }
        if (filename.contains("..")) {
            throw new StorageCannotSaveFileException(
                    "Cannot store file with relative path outside current directory "
                            + filename);
        }
        try (InputStream inputStream = file.getInputStream()) {
            createUuidFolder(uuid);
            Files.copy(inputStream, this.rootLocation.resolve(uuid + "/" + filename),
                    StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            throw new StorageCannotSaveFileException("Failed to store file " + filename, e);
        }
    }

    @Override
    public void storeAsFile(File file, UUID uuid) throws StorageCannotSaveFileException {
        String filename = file.getName();
        try {
            Files.copy(new FileInputStream(file), this.rootLocation.resolve(uuid + "/" + filename),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new StorageCannotSaveFileException("Failed to store file " + filename, e);
        }
    }

    @Override
    public Stream<Path> loadAll(UUID uuid) {
        try {
            return Files.walk(Paths.get(this.rootLocation.toString(), uuid.toString()), 1)
                    .filter(path -> !path.equals(this.rootLocation))
                    .map(this.rootLocation::relativize);
        } catch (IOException e) {
            throw new StorageException("Failed to read stored files", e);
        }

    }

    @Override
    public Path load(String filename, UUID uuid) {
        return rootLocation.resolve(uuid + "/" + filename);
    }

    @Override
    public Resource loadAsResource(String filename, UUID uuid) throws StorageFileNotFoundException {
        try {
            Path file = load(filename, uuid);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new StorageFileNotFoundException(
                        "Could not read file: " + filename);

            }
        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new StorageException("Could not initialize main storage", e);
        }
    }

    @Override
    public void createUuidFolder(UUID uuid) {
        try {
            Files.createDirectories(Paths.get(this.rootLocation.toString(), uuid.toString()));
        } catch (IOException e) {
            throw new StorageException("Could not initialize UUID folder", e);
        }
    }
}
