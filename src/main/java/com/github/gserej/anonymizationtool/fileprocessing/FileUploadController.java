package com.github.gserej.anonymizationtool.fileprocessing;

import com.github.gserej.anonymizationtool.filestorage.StorageFileNotFoundException;
import com.github.gserej.anonymizationtool.filestorage.StorageService;
import com.github.gserej.anonymizationtool.messages.MessageService;
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
    private final MessageService messageService;

    @Autowired
    public FileUploadController(StorageService storageService, FileProcessingService fileProcessingService, MessageService messageService) {
        this.storageService = storageService;
        this.fileProcessingService = fileProcessingService;
        this.messageService = messageService;
    }

    @GetMapping("/")
    public String getMainPage() {
        return "pageviewer.html";
    }

    @ResponseBody
    @GetMapping("/api/startover")
    public String startOver() {
        messageService.setCurrentMessage(messageService.getEMPTY_MESSAGE());
        storageService.deleteAll();
        return "ok";
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

        fileProcessingService.processUploadedFile(multipartFile.getOriginalFilename());

        return "redirect:/";
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound() {
        return ResponseEntity.notFound().build();
    }

}
