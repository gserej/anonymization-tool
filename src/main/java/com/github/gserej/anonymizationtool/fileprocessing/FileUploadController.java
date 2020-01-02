package com.github.gserej.anonymizationtool.fileprocessing;

import com.github.gserej.anonymizationtool.filestorage.StorageFileNotFoundException;
import com.github.gserej.anonymizationtool.filestorage.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.util.UUID;

@Slf4j
@Controller
public class FileUploadController {

    private final StorageService storageService;
    private final FileProcessingService fileProcessingService;


    @Autowired
    public FileUploadController(StorageService storageService, FileProcessingService fileProcessingService) {
        this.storageService = storageService;
        this.fileProcessingService = fileProcessingService;
    }

    @GetMapping("/")
    public String getMainPage() {
        return "pageviewer.html";
    }

    @ResponseBody
    @GetMapping("/api/startover/")
    public String startOver() {
        storageService.deleteAll();
        return "ok";
    }

    @ResponseBody
    @GetMapping("/api/files/{uuid}")
    public String getFileLocation(@PathVariable("uuid") UUID uuid) {
        return storageService.loadAll(uuid).map(
                path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
                        "serveFile", uuid, path.getFileName().toString())
                        .build().toString())
                .filter(f -> f.endsWith(".pdf"))
                .findFirst().orElse(null);
    }

    @ResponseBody
    @GetMapping("api/files/{uuid}/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable("uuid") UUID uuid, @PathVariable("filename") String filename) {

        Resource file = storageService.loadAsResource(filename, uuid);

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @PostMapping("/api/files/{uuid}")
    public String handleFileUpload(@RequestParam("file") MultipartFile multipartFile, @PathVariable("uuid") UUID uuid) {
        storageService.store(multipartFile, uuid);

        fileProcessingService.processUploadedFile(multipartFile.getOriginalFilename(), uuid);

        return "redirect:/";
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound() {
        return ResponseEntity.notFound().build();
    }

}
