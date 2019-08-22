package com.github.gserej.anonymizationtool;

import com.github.gserej.anonymizationtool.storage.StorageFileNotFoundException;
import com.github.gserej.anonymizationtool.storage.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
public class FileUploadController {

    private final StorageService storageService;

    private static boolean ocrDone = false;
    private static Object modelObject;

    static boolean isOcrDone() {
        return ocrDone;
    }

    public static Object getModelObject() {
        return modelObject;
    }

    public static void setModelObject(Object modelObject) {
        FileUploadController.modelObject = modelObject;
    }

    @Autowired
    public FileUploadController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/")
    public String listUploadedFiles(Model model) throws IOException {


        model.addAttribute("files", storageService.loadAll().map(
                path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
                        "serveFile", path.getFileName().toString()).build().toString()).filter(f -> f.endsWith(".pdf"))
                .collect(Collectors.toList()));

        model.addAttribute("rectListModel", getModelObject());

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
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes, Model model) {

        storageService.store(file);
        redirectAttributes.addFlashAttribute("message",
                "You successfully uploaded " + file.getOriginalFilename() + "!");

        File fileToProcess = storageService.loadAsFile(file.getOriginalFilename());
        try {
            String fileExtension = FilenameUtils.getExtension(fileToProcess.getName());
            if (fileExtension.equals("pdf")) {
                log.info("pdf file found");
                PrintDrawLocations.PrintDrawLocation(fileToProcess);
                setModelObject(RectangleBoxList.getRectangleBoxList());

                log.info(RectangleBoxList.getRectangleBoxList().toString());

            } else if (fileExtension.equals("jpg") || fileExtension.equals("png")) {
                log.info("image file found");
                String pathToImagePdfFile = CreatePdfFromImage.createPdfFromImage(fileToProcess, fileToProcess.getName());


                File imagePdfFile = new File(pathToImagePdfFile);
                FileInputStream input = new FileInputStream(imagePdfFile);
//                log.info(imagePdfFile.getName());
                MultipartFile multipartFile = new MockMultipartFile(imagePdfFile.getName(),
                        imagePdfFile.getName(), "text/plain", IOUtils.toByteArray(input));
                storageService.store(multipartFile);
                File loadedPdfFile = storageService.loadAsFile(multipartFile.getOriginalFilename());

                ocrDone = true;
                Runnable r = () -> {
                    TesseractOCR.imageFileOCR(fileToProcess);
                    log.info("OCR status: done");
                    try {
                        PrintDrawLocations.PrintDrawLocation(loadedPdfFile);
                        log.info("Processing imgPdfFile status: done");
                        setModelObject(RectangleBoxList.getRectangleBoxList());
                        log.info(RectangleBoxList.getRectangleBoxList().toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                };
                new Thread(r).start();


            } else {
                redirectAttributes.addFlashAttribute("message", "You uploaded the file with a wrong file extension.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return "redirect:/";
    }

    @PostMapping(value = "/api")
    @ResponseBody
    public String postJson(@RequestBody List<RectangleBox> rectangleBoxes, HttpServletRequest request) {
        log.info(rectangleBoxes.toString());
        return null;
    }


    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

}
