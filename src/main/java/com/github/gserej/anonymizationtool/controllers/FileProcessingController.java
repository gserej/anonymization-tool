package com.github.gserej.anonymizationtool.controllers;

import com.github.gserej.anonymizationtool.exceptions.*;
import com.github.gserej.anonymizationtool.service.fileprocessing.FileProcessingService;
import com.github.gserej.anonymizationtool.service.filestorage.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.io.FileNotFoundException;
import java.util.UUID;

@Slf4j
@Controller
public class FileProcessingController {

    private final StorageService storageService;
    private final FileProcessingService fileProcessingService;

    @Autowired
    public FileProcessingController(StorageService storageService, FileProcessingService fileProcessingService) {
        this.storageService = storageService;
        this.fileProcessingService = fileProcessingService;
    }

    @PostMapping("/api/files")
    public String handleFileUpload(@RequestParam("file") MultipartFile multipartFile, @RequestParam UUID uuid)
            throws FileProcessingWrongExtensionException, StorageCannotSaveFileException, ImageToPdfConversionException, WordsExtractionException {
        fileProcessingService.processUploadedFile(multipartFile, uuid);
        return "redirect:/";
    }

    @ResponseBody
    @GetMapping("/api/files/{uuid}")
    public String getFileLocation(@PathVariable("uuid") UUID uuid) throws FileNotFoundException {
        storageService.createUuidFolder(uuid);
        return storageService.loadAll(uuid).map(
                path -> MvcUriComponentsBuilder.fromMethodName(FileProcessingController.class,
                        "serveFile", uuid, path.getFileName().toString())
                        .build().toString())
                .filter(f -> f.endsWith(".pdf"))
                .findFirst().orElseThrow(FileNotFoundException::new);
    }

    @ResponseBody
    @GetMapping("api/files/{uuid}/{filename}")
    public ResponseEntity<Resource> serveFile(@PathVariable("uuid") UUID uuid, @PathVariable("filename") String filename)
            throws StorageFileNotFoundException {
        Resource file = storageService.loadAsResource(filename, uuid);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound() {
        return ResponseEntity.notFound().build();
    }

}
