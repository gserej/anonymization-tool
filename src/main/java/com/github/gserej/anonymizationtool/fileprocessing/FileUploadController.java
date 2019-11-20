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

@Slf4j
@Controller
public class FileUploadController {

    private final StorageService storageService;
    private final FileProcessingService fileProcessingService;

    private String message;

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
    @GetMapping("/api/message")
    public String getMessage() {
        return this.message;
    }

    @ResponseBody
    @GetMapping("/api/files")
    public String getFileLocation() {
        return storageService.loadAll().map(
                path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
                        "serveFile", path.getFileName().toString())
                        .build().toString())
                .filter(f -> f.endsWith(".pdf"))
                .findFirst().orElse(null);
    }


    @ResponseBody
    @GetMapping("/files/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

        Resource file = storageService.loadAsResource(filename);

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @PostMapping("/")
    public String handleFileUpload(@RequestParam("file") MultipartFile multipartFile) {
        storageService.store(multipartFile);

        boolean wrongExtension = fileProcessingService.processUploadedFile(multipartFile.getOriginalFilename());
        if (wrongExtension) {
            message = "You have uploaded the file with a wrong file extension.";
        } else {
            message = "You successfully uploaded " + multipartFile.getOriginalFilename() + "!";
        }
        return "redirect:/";
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound() {
        return ResponseEntity.notFound().build();
    }

}
