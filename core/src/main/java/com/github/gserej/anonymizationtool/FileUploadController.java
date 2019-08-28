package com.github.gserej.anonymizationtool;

import com.github.gserej.anonymizationtool.storage.StorageFileNotFoundException;
import com.github.gserej.anonymizationtool.storage.StorageService;
import lombok.Getter;
import lombok.Setter;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
public class FileUploadController {

    private final StorageService storageService;

    @Setter
    @Getter
    private static Object modelObject;

    @Setter
    @Getter
    private static String tempName;

    @Getter
    @Setter
    private static List<String> tempImagesList;

    @Autowired
    public FileUploadController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/")
    public String listUploadedFiles(Model model) {

        model.addAttribute("files", storageService.loadAll().map(
                path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
                        "serveFile", path.getFileName().toString()).build().toString()).filter(f -> f.endsWith(".pdf"))
                .collect(Collectors.toList()));

        model.addAttribute("rectListModel", getModelObject());

        return "pageviewer";
    }


    @ModelAttribute("rectListModel")
    private Object rectListModel() {
        return getModelObject();
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
                                   RedirectAttributes redirectAttributes) {

        PrintDrawLocations.setStorageService(storageService);
        storageService.store(file);
        redirectAttributes.addFlashAttribute("message",
                "You successfully uploaded " + file.getOriginalFilename() + "!");

        File fileToProcess = storageService.loadAsFile(file.getOriginalFilename());
        try {
            String fileExtension = FilenameUtils.getExtension(fileToProcess.getName());
            if (fileExtension.equals("pdf")) {
                log.info("pdf file found");
                setTempName(file.getOriginalFilename());
                PrintDrawLocations.PrintDrawLocation(fileToProcess, false);
                PrintImageLocations.imageLocations(fileToProcess);
                setModelObject(RectangleBoxLists.parseRectangleBoxList(RectangleBoxLists.getRectangleBoxList()));
                log.info(RectangleBoxLists.getRectangleBoxListParsed().toString());

            } else if (fileExtension.equals("jpg") || fileExtension.equals("png")) {
                log.info("image file found");

                String pathToImagePdfFile = CreatePdfFromImage.createPdfFromSingleImage(fileToProcess, fileToProcess.getName());

                File imagePdfFile = new File(pathToImagePdfFile);
                MultipartFile multipartFile = new MockMultipartFile(imagePdfFile.getName(),
                        imagePdfFile.getName(), "text/plain", IOUtils.toByteArray(new FileInputStream(imagePdfFile)));
                storageService.store(multipartFile);
                setTempName(multipartFile.getOriginalFilename());

                Runnable r = () -> {
                    TesseractOCR.imageFileOCR(fileToProcess, true, null);
                    log.info("OCR processing: done");
                    setModelObject(RectangleBoxLists.parseRectangleBoxList(RectangleBoxLists.getRectangleBoxList()));
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
    public String postJson(@RequestBody List<RectangleBox> rectangleBoxesMarked) {
        RectangleBoxLists.setRectangleBoxListMarked(rectangleBoxesMarked);
        log.info(rectangleBoxesMarked.toString());

        File fileToProcess = storageService.loadAsFile(getTempName());
        try {
            PrintDrawLocations.PrintDrawLocation(fileToProcess, true);
            log.info(tempImagesList.toString());
            List<File> imageFilesList = new ArrayList<>();
            String processedImageFileName = null;
            for (String s : tempImagesList) {
                File processedImageFile = storageService.loadAsFile(s);
                processedImageFileName = processedImageFile.getName();
                imageFilesList.add(processedImageFile);
            }

            String pathToDonePdf = CreatePdfFromImage.createPdfFromMultipleImages(imageFilesList, processedImageFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "redirect:/";
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

}
