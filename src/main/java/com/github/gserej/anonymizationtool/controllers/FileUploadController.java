package com.github.gserej.anonymizationtool.controllers;

import com.github.gserej.anonymizationtool.filestorage.StorageFileNotFoundException;
import com.github.gserej.anonymizationtool.filestorage.StorageService;
import com.github.gserej.anonymizationtool.services.FileProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.stream.Collectors;

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
    public String listUploadedPdfFile(Model model) {
        model.addAttribute("files", storageService.loadAll().map(
                path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
                        "serveFile", path.getFileName().toString())
                        .build().toString())
                .filter(f -> f.endsWith(".pdf"))
                .limit(1)
                .collect(Collectors.toList()));

        return "pageviewer";
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

        Resource file = storageService.loadAsResource(filename);

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @PostMapping("/")
    public String handleFileUpload(@RequestParam("file") MultipartFile multipartFile,
                                   RedirectAttributes redirectAttributes) {
        storageService.store(multipartFile);

        boolean wrongExtension = fileProcessingService.processUploadedFile(multipartFile.getOriginalFilename());
        if (wrongExtension) {
            redirectAttributes.addFlashAttribute("message",
                    "You have uploaded the file with a wrong file extension.");
        } else {
            redirectAttributes.addFlashAttribute("message",
                    "You successfully uploaded " + multipartFile.getOriginalFilename() + "!");
        }
        return "redirect:/";
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound() {
        return ResponseEntity.notFound().build();
    }

}